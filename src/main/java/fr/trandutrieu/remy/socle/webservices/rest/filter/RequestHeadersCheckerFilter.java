package fr.trandutrieu.remy.socle.webservices.rest.filter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringUtils;

import fr.trandutrieu.remy.socle.audit.Audit;
import fr.trandutrieu.remy.socle.audit.Audit.Level;
import fr.trandutrieu.remy.socle.context.ContextManager;
import fr.trandutrieu.remy.socle.exceptions.Error;
import fr.trandutrieu.remy.socle.webservices.inout.BusinessMessage.BusinessMessageBuilder;

/**
 * Filter to validate required technical headers
 *
 * @author Oualid TOUARI
 */
@Provider
@Priority(FilterPriority.HEADERS_CHECKING)
public class RequestHeadersCheckerFilter implements ContainerRequestFilter {

	public static final String PATTERN_REST_SERVICE_ID = "^/[is]md/[a-z_-]+(/v([2-9]{1}|[1-9][0-9]{1,10}))?/[a-z_-]+(/\\{\\}/[a-z_-]+)*$";
	
	private static final String SERVICE = "SERVICE";
	
	@Override
	public void filter(final ContainerRequestContext requestContext) throws IOException {
		
		String reason = checkHeaders(requestContext.getHeaders());
		if(StringUtils.isEmpty(reason)) {
			Audit.trace(Level.DEBUG, SERVICE, "Check Headers OK execTime = " +  Duration.between(ContextManager.get().getStart(), Instant.now()).toMillis() + "ms");
		}
		else {
			Audit.trace(Level.ERROR, SERVICE, reason);
			requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
					.entity(BusinessMessageBuilder.instance()
							.code(Error.HEADERS_INVALID.getErrorCode().name())
							.message(Error.HEADERS_INVALID.getErrorCode().getLabel() + " : " + reason).build())
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
					.build());
		}
	}
	
	private String checkHeaders(final MultivaluedMap<String, String> http_headers) {
		if (StringUtils.isEmpty(getCorrelationId(http_headers))) {
			return "correlationId missing";
		}
		
		if (StringUtils.isEmpty(getUsername(http_headers))) {
			return "username missing";
		}
		
		if (!checkTimestamp(http_headers)) {
			return "timestamp missing or not validate";
		}
		
		return null;
	}
	
	private boolean checkTimestamp(final Map<String, List<String>> http_headers) {
		String timestamp = http_headers.containsKey("timestamp") ? http_headers.get("timestamp").get(0) : null;
		if (timestamp != null) {
			Long timestampLong = Long.parseLong(timestamp);
			Date date = new Date(timestampLong); 
			Instant start = date.toInstant();
			Instant end = Instant.now();
			
			Duration requestTimeStamp = Duration.between(start, end);
			if (requestTimeStamp.getSeconds() > 0 || requestTimeStamp.toMinutes() < 5) {
				return true;
			}
		}
		
		//TODO return false;
		Audit.trace(Level.WARNING, SERVICE, "checkTimestamp disabled");
		return true;
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
	
}
