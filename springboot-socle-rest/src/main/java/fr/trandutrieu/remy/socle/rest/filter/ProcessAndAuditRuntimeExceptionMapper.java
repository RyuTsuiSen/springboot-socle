package fr.trandutrieu.remy.socle.rest.filter;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.netflix.hystrix.exception.HystrixRuntimeException;

import fr.trandutrieu.remy.socle.audit.Audit;
import fr.trandutrieu.remy.socle.audit.Audit.Level;
import fr.trandutrieu.remy.socle.exceptions.Error;
import fr.trandutrieu.remy.socle.inout.BusinessMessage.BusinessMessageBuilder;


@Provider
public class ProcessAndAuditRuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
	
	@Context
    UriInfo uriInfo;
	
	private static final String EXCEPTION_FILTER = "EXCEPTION FILTER";
	
	@Override
    public Response toResponse(RuntimeException t) {
		if (t instanceof HystrixRuntimeException) {
			if(t.getCause() instanceof TimeoutException) {
				Audit.trace(Level.ERROR, EXCEPTION_FILTER, "Detail erreur", t.getCause());
				return Response.status(Response.Status.REQUEST_TIMEOUT)
						.entity(BusinessMessageBuilder.instance()
								.code(Error.ERROR_TIMEOUT.getErrorCode().name())
								.message(Error.ERROR_TIMEOUT.getErrorCode().getLabel()).build())
						.type(MediaType.APPLICATION_JSON + ";charset=utf-8")
						.build();
				
			}
			else {
				Audit.trace(Level.ERROR, EXCEPTION_FILTER, "Detail erreur", t.getCause());
				if(t.getCause().getMessage().equals("Hystrix circuit short-circuited and is OPEN")) {
					return Response.status(Response.Status.SERVICE_UNAVAILABLE)
							.entity(BusinessMessageBuilder.instance()
									.code(Error.ERROR_UNVAILABLE.getErrorCode().name())
									.message(Error.ERROR_UNVAILABLE.getErrorCode().getLabel()).build())
							.type(MediaType.APPLICATION_JSON + ";charset=utf-8")
							.build();
				}
				else {
					return Response.status(Response.Status.SERVICE_UNAVAILABLE)
							.entity(BusinessMessageBuilder.instance()
									.code(Error.ERROR_EXTERNAL_CALL.getErrorCode().name())
									.message(Error.ERROR_EXTERNAL_CALL.getErrorCode().getLabel()).build())
							.type(MediaType.APPLICATION_JSON + ";charset=utf-8")
							.build();
				}
			}
		}
		else if (t instanceof ClientErrorException || t instanceof RedirectionException || t instanceof ServerErrorException || t instanceof WebApplicationException){
			String uri = Optional.ofNullable(uriInfo).map(u->u.getRequestUri()).map(u->u.toString()).orElse("http://unknown_resource");
			WebApplicationException webApplicationException = (WebApplicationException) t;
    		Response response = webApplicationException.getResponse();
    		if (response != null){
    			Audit.trace(Level.ERROR, EXCEPTION_FILTER, "Detail erreur " + response.getStatusInfo().getStatusCode() + " " + response.getStatusInfo().getReasonPhrase() + " uri = " + uri, webApplicationException.getCause());
    		}
    		else{
    			Audit.trace(Level.ERROR, EXCEPTION_FILTER, "Detail erreur rest", webApplicationException.getCause());
    			response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
    					.entity(BusinessMessageBuilder.instance()
    							.code(Error.ERROR_SERVER.getErrorCode().name())
    							.message(Error.ERROR_SERVER.getErrorCode().getLabel()).build())
    					.type(MediaType.APPLICATION_JSON + ";charset=utf-8")
    					.build();
    		}
    		return response;
    	}
		else {
			String uri = Optional.ofNullable(uriInfo).map(u->u.getRequestUri()).map(u->u.toString()).orElse("http://unknown_resource");
			Audit.trace(Level.ERROR, EXCEPTION_FILTER, "Detail erreur runtime", t);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(BusinessMessageBuilder.instance()
							.code(Error.ERROR_SERVER.getErrorCode().name())
							.message(Error.ERROR_SERVER.getErrorCode().getLabel()).build() + " uri = " + uri)
					.type(MediaType.APPLICATION_JSON + ";charset=utf-8")
					.build();
		
		}
	}
}
