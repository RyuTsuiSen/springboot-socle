package fr.trandutrieu.remy.socle.rest.filter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import fr.trandutrieu.remy.socle.audit.Audit;
import fr.trandutrieu.remy.socle.audit.Audit.Level;
import fr.trandutrieu.remy.socle.exceptions.BusinessException;
import fr.trandutrieu.remy.socle.inout.BusinessMessage.BusinessMessageBuilder;

@Provider
public class ProcessAndAuditBusinessExceptionMapper implements ExceptionMapper<BusinessException> {

	private static final String EXCEPTION_FILTER = "EXCEPTION HANDLER";
	
	@Override
	public Response toResponse(BusinessException exception) {
		Audit.trace(Level.ERROR, EXCEPTION_FILTER, "Detail erreur", exception);
		return Response.status(Response.Status.BAD_REQUEST)
				.entity(BusinessMessageBuilder.instance()
						.code(exception.getCodeErreur().getLabelErreur().name())
						.message(exception.getCodeErreur().getLabelErreur().getLabel()).build())
				.type(MediaType.APPLICATION_JSON + ";charset=utf-8")
				.build();

	}
}
