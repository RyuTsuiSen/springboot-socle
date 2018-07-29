package fr.trandutrieu.remy.socle.webservices.soap.handlers;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;

import org.apache.cxf.jaxws.handler.soap.SOAPMessageContextImpl;
import org.w3c.dom.Node;

import fr.trandutrieu.remy.socle.audit.Audit;
import fr.trandutrieu.remy.socle.audit.Audit.Level;
import fr.trandutrieu.remy.socle.exceptions.Error;



public class AuthorizationHandler implements SOAPHandler<SOAPMessageContextImpl> {

	private static final String AUTHORIZATION = "AUTHORIZATION";
	
	public boolean handleMessage(SOAPMessageContextImpl mc) {
		Boolean outboundProperty = (Boolean) mc.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		
		if (!outboundProperty.booleanValue()) {
			Instant start = Instant.now();
			
			boolean authorizationChecked = checkAuthorization();
			
			if (!authorizationChecked) {
				try {
					SOAPBody soapBody = mc.getMessage().getSOAPBody();
					soapBody.removeContents();
					soapBody.addFault();
					SOAPFault fault = soapBody.getFault();
					Node soapFaultCode = fault.getFirstChild();
					Node faultCode = soapFaultCode.getFirstChild();

					Node soapFaultString = soapFaultCode.getNextSibling();
					Node faultString = soapFaultString.getFirstChild();
					
					faultCode.setNodeValue(Error.AUTHORIZATION_ERROR.getErrorCode().name());
					faultString.setNodeValue(Error.AUTHORIZATION_ERROR.getErrorCode().getLabel());
					
					Audit.trace(Level.ERROR, AUTHORIZATION, "Authorization failed");
				} catch (SOAPException e) {
					Audit.trace(Level.ERROR, AUTHORIZATION, "Authorization failed", e);
				}
				
				return false;
			}
			else {
				Duration duration = Duration.between(start, Instant.now());
				Audit.trace(Level.INFO, AUTHORIZATION, "Authorization OK " + duration.toMillis() + "ms");
			}
			
		}
		return true;
	}

	private boolean checkAuthorization() {
		return true;
	}

	public Set<QName> getHeaders() {
		return Collections.emptySet();
	}

	public boolean handleFault(SOAPMessageContextImpl mc) {
		return true;
	}

	@Override
	public void close(MessageContext context) {
	}
}