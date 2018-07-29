package fr.trandutrieu.remy.socle.rest.filter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.MDC;

import fr.trandutrieu.remy.socle.audit.Audit;
import fr.trandutrieu.remy.socle.audit.Audit.Level;
import fr.trandutrieu.remy.socle.context.ContextManager;

@Provider
@Priority(FilterPriority.AUDIT_OUT)
public class AuditOutResourceFilter implements ContainerResponseFilter {


	private static final String OUT_SERVICE = "OUT SERVICE";

	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
		String httpReturnCode = "HTTPCode="+response.getStatusInfo().getStatusCode()+", Reason="+response.getStatusInfo().getReasonPhrase();
		Duration duree = Duration.between(ContextManager.get().getStart(), Instant.now());
		
		StringBuilder serviceName = new StringBuilder();
		serviceName.append(ContextManager.get().getRequestedService() + "." + ContextManager.get().getRequestedOperation()).append(':').append(duree.toMillis());
		
		if (Response.Status.Family.SERVER_ERROR.equals(response.getStatusInfo().getFamily())) {
			Audit.trace(Level.INFO, OUT_SERVICE, "execTime = " + Duration.between(ContextManager.get().getStart(), Instant.now()).toMillis() + "ms, " + httpReturnCode);
		}
		else if (Response.Status.Family.CLIENT_ERROR.equals(response.getStatusInfo().getFamily()) || Response.Status.Family.REDIRECTION.equals(response.getStatusInfo().getFamily())){
			Audit.trace(Level.WARNING, OUT_SERVICE, "execTime = " + Duration.between(ContextManager.get().getStart(), Instant.now()).toMillis() + "ms, " + httpReturnCode);
		}else {
			Audit.trace(Level.ERROR, OUT_SERVICE, "execTime = " + Duration.between(ContextManager.get().getStart(), Instant.now()).toMillis() + "ms, " + httpReturnCode);
		}
		clean();
	}
	
	private void clean() {
		ContextManager.remove();
		MDC.clear();
	}

}
