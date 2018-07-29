package fr.trandutrieu.remy.socle.exceptions;

public enum Error {
	HEADERS_INVALID(ErrorCode.ERR_000),
	ERROR_SERVER(ErrorCode.ERR_001),
	AUTHORIZATION_ERROR(ErrorCode.ERR_003),
	AUTHENTICATION_ERROR(ErrorCode.ERR_004),
	ERROR_EXTERNAL_CALL(ErrorCode.ERR_005),
	ERROR_UNVAILABLE(ErrorCode.ERR_006),
	ERROR_TIMEOUT(ErrorCode.ERR_007);
	
	private ErrorCode errorCode;
	
	Error(ErrorCode errorCode){
		this.errorCode = errorCode;
	}
	
	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public enum ErrorCode{
		ERR_000("Headers incorrectes"),
		ERR_001("Une erreur est survenue, veuillez nous contacter"),
		ERR_003("Authorization error"),
		ERR_004("Authentication error"),
		ERR_005("Une erreur est survenue dans un appel externe, veuillez nous contacter"),
		ERR_006("Service indisponible temporairement, reessayer plus tard"),
		ERR_007("Execution de service trop longue timeout depasse");
		private String label;
		ErrorCode(String label){
			this.label = label;
		}
		public String getLabel() {
			return label;
		}
		
		
	}
}
