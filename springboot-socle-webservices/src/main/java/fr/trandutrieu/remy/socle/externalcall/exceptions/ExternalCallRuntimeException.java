package fr.trandutrieu.remy.socle.externalcall.exceptions;

public class ExternalCallRuntimeException extends RuntimeException{

	public ExternalCallRuntimeException(String string) {
		super(string);
	}

	public ExternalCallRuntimeException(String string, Throwable exception) {
		super(string, exception);
	}
	
	private static final long serialVersionUID = 1L;

}
