package fr.trandutrieu.remy.socle.webservices.rest.filter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import fr.trandutrieu.remy.socle.WhoAmI;
import fr.trandutrieu.remy.socle.audit.Audit;
import fr.trandutrieu.remy.socle.audit.Audit.Level;
import fr.trandutrieu.remy.socle.context.ContextBean;
import fr.trandutrieu.remy.socle.context.ContextManager;

@Provider
@Priority(FilterPriority.AUDIT_IN)
public class AuditInResourceFilter implements ContainerRequestFilter {
	public static final String PATTERN_REST_SERVICE_ID = "^/[is]md/[a-z_-]+(/v([2-9]{1}|[1-9][0-9]{1,10}))?/[a-z_-]+(/\\{\\}/[a-z_-]+)*$";
	
	private static final String IN_SERVICE = "SERVICE IN";
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		ContextBean bean = initializeContextBean(requestContext);
		addContextBeanByHeaderRequired(requestContext, bean);
		initMDC();
		Audit.trace(Level.INFO, IN_SERVICE, "");
	}
	
	private void addContextBeanByHeaderRequired(ContainerRequestContext mc, ContextBean bean) {
		final Map<String, List<String>> http_headers = mc.getHeaders();
		String internalId = UUID.randomUUID().toString() + "#";
		bean.setConversationID(!StringUtils.isEmpty(getCorrelationId(http_headers)) ? internalId+getCorrelationId(http_headers) : internalId+"correlationIdMissing");
		bean.setCaller(!StringUtils.isEmpty(getUsername(http_headers)) ? getUsername(http_headers) :"unknown");
		ContextManager.set(bean);
	}
	
	private String getCorrelationId(final Map<String, List<String>> http_headers) {

		return http_headers.containsKey("correlationId") ? http_headers.get("correlationId").get(0) : null;
	}
	
	private String getUsername(final Map<String, List<String>> http_headers) {
		String username = http_headers.containsKey("username") ? http_headers.get("username").get(0) : null;
		if (username == null) {
			String authorization = http_headers.containsKey("Authorization") ? http_headers.get("Authorization").get(0) : null;
			username = decode(authorization);
		}
		return username;
	}
	
	private String decode(final String encoded) {
		if(encoded == null ) {
			return null;
		}
		
		if (!encoded.startsWith("Basic")) {
			return null;
		}
		
        String base64Credentials = encoded.substring("Basic".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), Charset.forName("UTF-8"));
        final String[] values = credentials.split(":",2);
        return values[0];
    }
	
	private ContextBean initializeContextBean(ContainerRequestContext mc) {
		ContextBean bean = new ContextBean();
		bean.setStart(Instant.now());
		String requestedService =  getRequestedURI(mc);
		String requestedOperation =  getRequestedOperation(mc);
		bean.setRequestedOperation(requestedOperation);
		bean.setRequestedService(requestedService);
		bean.setVersionService(WhoAmI.getInstance().name+"v"+WhoAmI.getInstance().version+"#"+WhoAmI.getInstance().socleVersion);
		return bean;
	}
	
	private String getRequestedURI(ContainerRequestContext requestContext) {
		UriInfo uriInfo = requestContext.getUriInfo();
		String requestURIPath = (uriInfo != null && uriInfo.getRequestUri() != null && uriInfo.getRequestUri().getPath() != null) ? uriInfo.getRequestUri().getPath() : "";
		if (!StringUtils.isEmpty(requestURIPath)){
			requestURIPath = rewriteRequestedUri(requestURIPath);
		}
		return requestURIPath;
	}

	private String getRequestedOperation(ContainerRequestContext requestContext) {
		return requestContext.getMethod();
	}
	
	
	private String rewriteRequestedUri(String requestedURI){
		boolean versionIsPresent = false;
		String varStr = "{}";
		String pathSEP = "/";
		StringBuilder newRequestURIPath = new StringBuilder();
		StringTokenizer sk = new StringTokenizer(requestedURI, pathSEP);
		int index = 0;
		while (sk.hasMoreTokens()) {
			if (index == 0 || index == 1) { // it is the context root or module name
				newRequestURIPath.append(pathSEP).append(sk.nextToken());
			}
			else if (index == 2){
				String version = sk.nextToken();
				boolean v = StringUtils.startsWith(version, "v");
				String substringAfter = StringUtils.substringAfter(version, "v");
				newRequestURIPath.append(pathSEP).append(version);
				if (v && StringUtils.isNumeric(substringAfter)) {
					int numVersion = Integer.parseInt(substringAfter);
					if (numVersion >= 2) {
						versionIsPresent = true;
					}
				}
			}
			else if ((versionIsPresent && index % 2 == 1) 
					|| (!versionIsPresent && index % 2 == 0)) { //ressource name
				newRequestURIPath.append(pathSEP).append(sk.nextToken());
			} else { //id field
				
				newRequestURIPath.append(pathSEP);
				
				String maybeVerbeAction = sk.nextToken();
				if (!sk.hasMoreTokens() && validateRestService(newRequestURIPath.toString() + maybeVerbeAction)) {
					newRequestURIPath.append(maybeVerbeAction);
				}
				else {
					newRequestURIPath.append(varStr);
				}
				
			}
			index++;
		}
		return newRequestURIPath.toString();
	}
	
	
	private boolean validateRestService(String serviceID) { 
		String varStr = "{}";
		String pathSEP = "/";
		if (!Pattern.matches(PATTERN_REST_SERVICE_ID, serviceID)) {
			String serviceIDrewrite = StringUtils.substringBeforeLast(serviceID, "/");
			String action = StringUtils.substringAfterLast(serviceID, "/");
			
			if (StringUtils.equalsIgnoreCase(varStr, action) || StringUtils.endsWith(serviceID, pathSEP) || !Pattern.matches(PATTERN_REST_SERVICE_ID, serviceIDrewrite)) {
				return false;
			}

		} 
		return true;
	}
	
	private void initMDC() {
		MDC.put("conversationId", ContextManager.get().getConversationID());
		MDC.put("serviceName", ContextManager.get().getRequestedService());
		MDC.put("operationName", ContextManager.get().getRequestedOperation());
		MDC.put("version", ContextManager.get().getVersionService());
		MDC.put("consumerName", ContextManager.get().getCaller());
	}
}
