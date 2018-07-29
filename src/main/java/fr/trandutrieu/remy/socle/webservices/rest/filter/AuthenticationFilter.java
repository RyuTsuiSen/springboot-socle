package fr.trandutrieu.remy.socle.webservices.rest.filter;


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
import fr.trandutrieu.remy.socle.webservices.inout.BusinessMessage.BusinessMessageBuilder;

/**
 * Filter for authentication purpose
 *
 * @author Oualid TOUARI
 */
@Provider
@Priority(FilterPriority.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter { 

	private static final String AUTHENTICATION = "AUTHENTICATION";
	/**
	 * @see ContainerRequestFilter#filter(ContainerRequestContext)
	 */
	@Override
	public void filter(ContainerRequestContext containerRequestContext) {
		Instant start = Instant.now();
		
		boolean authentication = checkAuthentication();
		if (!authentication) {
			containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
					.entity(BusinessMessageBuilder.instance()
							.code(Error.AUTHENTICATION_ERROR.getErrorCode().name())
							.message(Error.AUTHENTICATION_ERROR.getErrorCode().getLabel()).build())
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
					.build());
		}
		else {
			Duration duration = Duration.between(start, Instant.now());
			Audit.trace(Level.INFO, AUTHENTICATION, "Authentication OK execTime = " + duration.toMillis() + "ms");
		}
	}
	
	private boolean checkAuthentication() {
		return true;
	}
}