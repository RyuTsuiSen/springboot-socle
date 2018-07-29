package fr.trandutrieu.remy.socle.rest.filter;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import fr.trandutrieu.remy.socle.audit.Audit;
import fr.trandutrieu.remy.socle.audit.Audit.Level;
import fr.trandutrieu.remy.socle.exceptions.Error;
import fr.trandutrieu.remy.socle.inout.BusinessMessage.BusinessMessageBuilder;

@Provider
@Priority(FilterPriority.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter{

	private static final String AUTHORIZATION = "AUTHORIZATION";
	
	@Override
	public void filter(ContainerRequestContext requestContext) {
		Instant start = Instant.now();
		
		boolean authentication = checkAuthorization();
		
		if (!authentication) {
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
					.entity(BusinessMessageBuilder.instance()
							.code(Error.AUTHORIZATION_ERROR.getErrorCode().name())
							.message(Error.AUTHORIZATION_ERROR.getErrorCode().getLabel()).build())
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
					.build());
		}
		else {
			Duration duration = Duration.between(start, Instant.now());
			Audit.trace(Level.INFO, AUTHORIZATION, "Authorization OK execTime = " + duration.toMillis() + "ms");
		}
	}
	
	private boolean checkAuthorization() {
		return true;
	}
}
