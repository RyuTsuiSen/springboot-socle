package fr.trandutrieu.remy.socle.webservices.soap.handlers;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.cxf.jaxws.handler.soap.SOAPMessageContextImpl;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

import fr.trandutrieu.remy.socle.WhoAmI;
import fr.trandutrieu.remy.socle.audit.Audit;
import fr.trandutrieu.remy.socle.audit.Audit.Level;
import fr.trandutrieu.remy.socle.context.ContextBean;
import fr.trandutrieu.remy.socle.context.ContextManager;
import fr.trandutrieu.remy.socle.exceptions.Error;

public class AuditInOutHandler implements SOAPHandler<SOAPMessageContext > {

	private static final String IN_SERVICE = "SERVICE IN";
	private static final String OUT_SERVICE = "SERVICE OUT";
	private static final String SERVICE = "SERVICE";
	public boolean handleMessage(SOAPMessageContext messageContext) {
		
		SOAPMessageContextImpl mc = (SOAPMessageContextImpl) messageContext;
		
		Boolean outboundProperty = (Boolean) mc.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outboundProperty.booleanValue()) {
			Audit.trace(Level.INFO, OUT_SERVICE, "execTime = " + Duration.between(ContextManager.get().getStart(), Instant.now()).toMillis() + "ms");
			clean();
			return true;
		}
		else {
			init(mc);
			Audit.trace(Level.INFO, IN_SERVICE, "");
			
			String reason = checkHeaders(mc);
			if(StringUtils.isEmpty(reason)) {
				Audit.trace(Level.DEBUG, SERVICE, "Context OK execTime = " +  Duration.between(ContextManager.get().getStart(), Instant.now()).toMillis() + "ms");
				return true;
			}
			else {
				outServiceOutWithErrorInServiceIn(mc, reason);
				Audit.trace(Level.ERROR, OUT_SERVICE, "execTime = " + Duration.between(ContextManager.get().getStart(), Instant.now()).toMillis() + "ms");
				clean();
				return false;
			}
		}

		
		
	}

	private void init(SOAPMessageContextImpl mc) {
		ContextBean bean = initializeContextBean(mc);
		addContextBeanByHeaderRequired(mc, bean);
		initMDC();
	}

	private void addContextBeanByHeaderRequired(SOAPMessageContextImpl mc, ContextBean bean) {
		final Map<String, List<String>> http_headers = (Map<String, List<String>>) mc.get(MessageContext.HTTP_REQUEST_HEADERS);
		String internalId = UUID.randomUUID().toString() + "#";
		bean.setConversationID(!StringUtils.isEmpty(getCorrelationId(http_headers)) ? internalId+getCorrelationId(http_headers) : internalId+"correlationIdMissing");
		bean.setCaller(!StringUtils.isEmpty(getUsername(http_headers)) ? getUsername(http_headers) :"unknown");
		ContextManager.set(bean);
	}

	private ContextBean initializeContextBean(SOAPMessageContextImpl mc) {
		ContextBean bean = new ContextBean();
		bean.setStart(Instant.now());
		QName requestedServiceName =  (QName)mc.get(SOAPMessageContext.WSDL_SERVICE);
		QName requestedOperationName =  (QName)mc.get(SOAPMessageContext.WSDL_OPERATION);
		String requestedService =  (requestedServiceName != null && requestedServiceName.getNamespaceURI() != null) ? (requestedServiceName.getNamespaceURI() + requestedServiceName.getLocalPart()) : null;
		String requestedOperation =  (requestedOperationName != null) ? requestedOperationName.getLocalPart() : null;
		bean.setRequestedOperation(requestedOperation);
		bean.setRequestedService(requestedService);
		bean.setVersionService(WhoAmI.getInstance().name+"v"+WhoAmI.getInstance().version+"#"+WhoAmI.getInstance().socleVersion);
		return bean;
	}

	private String checkHeaders(SOAPMessageContextImpl mc) {
		final Map<String, List<String>> http_headers = (Map<String, List<String>>) mc.get(MessageContext.HTTP_REQUEST_HEADERS);
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
	
	private void outServiceOutWithErrorInServiceIn(final SOAPMessageContextImpl mc, final String reason){
		buildSoapFault(mc, reason);
		Audit.trace(Level.ERROR, SERVICE, reason);
	}
	
	private void clean() {
		ContextManager.remove();
		MDC.clear();
	}
	
	private String getCorrelationId(final Map<String, List<String>> http_headers) {

		return http_headers.containsKey("correlationId") ? http_headers.get("correlationId").get(0) : null;
	}

	private void initMDC() {
		MDC.put("conversationId", ContextManager.get().getConversationID());
		MDC.put("serviceName", ContextManager.get().getRequestedService());
		MDC.put("operationName", ContextManager.get().getRequestedOperation());
		MDC.put("version", ContextManager.get().getVersionService());
		MDC.put("consumerName", ContextManager.get().getCaller());
	}

	private void buildSoapFault(final SOAPMessageContextImpl mc, String reason) {
		try {
			SOAPBody soapBody = mc.getMessage().getSOAPBody();
			soapBody.removeContents();
			soapBody.addFault();
			SOAPFault fault = soapBody.getFault();
			Node soapFaultCode = fault.getFirstChild();
			Node faultCode = soapFaultCode.getFirstChild();

			Node soapFaultString = soapFaultCode.getNextSibling();
			Node faultString = soapFaultString.getFirstChild();
			
			faultCode.setNodeValue(Error.HEADERS_INVALID.getErrorCode().name());
			faultString.setNodeValue(Error.HEADERS_INVALID.getErrorCode().getLabel() + " : " + reason);
		} catch (SOAPException e) {
			Audit.trace(Level.ERROR, SERVICE, "Error parsing XML", e);
		}
	}

	private String getUsername(final Map<String, List<String>> http_headers) {
		String username = http_headers.containsKey("username") ? http_headers.get("username").get(0) : null;
		if (username == null) {
			String authorization = http_headers.containsKey("Authorization") ? http_headers.get("Authorization").get(0) : null;
			username = decode(authorization);
		}
		return username;
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
	
	public Set<QName> getHeaders() {
		return Collections.emptySet();
	}

	public boolean handleFault(SOAPMessageContext messageContext) {
		if (ContextManager.get() != null) {
			Duration duration = Duration.between(ContextManager.get().getStart(), Instant.now());
			Audit.trace(Level.ERROR, OUT_SERVICE, "execTime = " + duration.toMillis() + "ms");
			ContextManager.remove();
			MDC.clear();
		}
		else {
			Audit.trace(Level.ERROR, OUT_SERVICE, "SOAP REQUEST INVALID");
		}
		return true;
	}

	@Override
	public void close(MessageContext context) {
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