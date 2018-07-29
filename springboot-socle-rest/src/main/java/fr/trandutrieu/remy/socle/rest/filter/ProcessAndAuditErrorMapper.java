package fr.trandutrieu.remy.socle.rest.filter;

import java.util.Optional;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import fr.trandutrieu.remy.socle.audit.Audit;
import fr.trandutrieu.remy.socle.audit.Audit.Level;
import fr.trandutrieu.remy.socle.exceptions.Error;
import fr.trandutrieu.remy.socle.inout.BusinessMessage.BusinessMessageBuilder;


@Provider
public class ProcessAndAuditErrorMapper implements ExceptionMapper<Throwable> {
	@Context
    UriInfo uriInfo;
	
	private static final String EXCEPTION_FILTER = "EXCEPTION FILTER";
	@Override
    public Response toResponse(Throwable t) {
		String uri = Optional.ofNullable(uriInfo).map(u->u.getRequestUri()).map(u->u.toString()).orElse("http://unknown_resource");
		Audit.trace(Level.ERROR, EXCEPTION_FILTER, "Detail erreur throwable", t);
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(BusinessMessageBuilder.instance()
						.code(Error.ERROR_SERVER.getErrorCode().name())
						.message(Error.ERROR_SERVER.getErrorCode().getLabel()).build() + " uri = " + uri)
				.type(MediaType.APPLICATION_JSON + ";charset=utf-8")
				.build();
	}
}
