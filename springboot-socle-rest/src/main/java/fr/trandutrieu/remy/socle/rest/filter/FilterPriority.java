package fr.trandutrieu.remy.socle.rest.filter;

enum FilterPriority {
	;
	// IN - REQUEST
	public static final int AUDIT_IN = 1000;	
	public static final int HEADERS_CHECKING = 2000;
	public static final int AUTHENTICATION = 3000;
	public static final int AUTHORIZATION = 4000;


	// OUT - RESPONSE
	public static final int CORS = 10000;
	public static final int AUDIT_OUT = 10002;

}
