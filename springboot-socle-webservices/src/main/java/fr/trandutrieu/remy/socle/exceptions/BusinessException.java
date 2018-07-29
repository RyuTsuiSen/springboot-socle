package fr.trandutrieu.remy.socle.exceptions;

public final class BusinessException extends Exception{
	private static final long serialVersionUID = 1L;
	private CodeErreurItf<?, ?> codeErreur;

	BusinessException() {}

	BusinessException(CodeErreurItf<?, ? > codeErreur, Throwable throwable) {
		super(throwable);
		this.codeErreur = codeErreur;
	}
	
	
	public CodeErreurItf<?, ?> getCodeErreur() {
		return codeErreur;
	}


	public static class BusinessExceptionBuilder{
		
		private Throwable throwable;
		private CodeErreurItf<?, ?> codeErreur;
		
		public BusinessExceptionBuilder(CodeErreurItf<?, ?> codeErreur) {
			this.codeErreur = codeErreur;
		}

		public static BusinessExceptionBuilder instance(CodeErreurItf<?, ?> codeErreur) {
			return new BusinessExceptionBuilder(codeErreur);
		}
		
		public BusinessException build() {
			BusinessException exception = new BusinessException(this.codeErreur, this.throwable);
			return exception;
		}
		
		public BusinessExceptionBuilder withThrowable(Throwable throwable) {
			this.throwable = throwable;
			return this;
		}
	}

}
