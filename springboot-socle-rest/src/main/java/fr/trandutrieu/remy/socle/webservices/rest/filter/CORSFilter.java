package fr.trandutrieu.remy.socle.webservices.rest.filter;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * Created by A381363 on 01/07/2017.
 */

@Provider
@Priority(FilterPriority.CORS)
public class CORSFilter implements ContainerResponseFilter {
    private final static String ACCESS_CONTROL_ALLOW_CREDENTIALS = "true";
    private final static String ACCESS_CONTROL_ALLOW_METHODS = "*";
    private final static String ACCESS_CONTROL_ALLOW_ORIGIN = "*";
    private final static String ACCESS_CONTROL_ALLOW_HEADERS = "Content-Type";


    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", ACCESS_CONTROL_ALLOW_CREDENTIALS);
        responseContext.getHeaders().add("Access-Control-Allow-Methods", ACCESS_CONTROL_ALLOW_METHODS);
        responseContext.getHeaders().add("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
        responseContext.getHeaders().add("Access-Control-Allow-Headers", ACCESS_CONTROL_ALLOW_HEADERS);
        
    }
}
