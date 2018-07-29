package fr.trandutrieu.remy.socle.webservices.inout;

public class BusinessMessage {
	private String code;
	private String message;
	
	private BusinessMessage() {
		
	}
	
	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}



	public static class BusinessMessageBuilder{
		
		private String code;
		private String message;
		
		public static BusinessMessageBuilder instance() {
			return new BusinessMessageBuilder();
		}
		
		public BusinessMessageBuilder() {
		}

		public BusinessMessage build() {
			BusinessMessage business = new BusinessMessage();
			business.code = this.code;
			business.message = this.message;
			return business;
		}
		
		public BusinessMessageBuilder code(String code) {
			this.code = code;
			return this;
		}
		
		public BusinessMessageBuilder message(String message) {
			this.message = message;
			return this;
		}
	}
	
}
