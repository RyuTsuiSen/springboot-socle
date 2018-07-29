package fr.trandutrieu.remy.socle.context;

import java.time.Instant;

public class ContextBean {
	private String conversationID;
	private Instant start;
	private String caller;
	private String requestedService;
	private String requestedOperation;
	private String versionService;
	private String correlationId;

	public String getConversationID() {
		return conversationID;
	}

	public void setConversationID(String conversationID) {
		this.conversationID = conversationID;
	}

	public Instant getStart() {
		return start;
	}

	public void setStart(Instant start) {
		this.start = start;
	}

	public String getCaller() {
		return caller;
	}

	public void setCaller(String caller) {
		this.caller = caller;
	}

	public String getRequestedService() {
		return requestedService;
	}

	public void setRequestedService(String requestedService) {
		this.requestedService = requestedService;
	}

	public String getRequestedOperation() {
		return requestedOperation;
	}

	public void setRequestedOperation(String requestedOperation) {
		this.requestedOperation = requestedOperation;
	}

	public String getVersionService() {
		return versionService;
	}

	public void setVersionService(String versionService) {
		this.versionService = versionService;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}
}
