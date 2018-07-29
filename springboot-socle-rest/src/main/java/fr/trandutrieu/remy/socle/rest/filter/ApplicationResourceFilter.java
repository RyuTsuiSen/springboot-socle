package fr.trandutrieu.remy.socle.rest.filter;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import fr.trandutrieu.remy.socle.rest.Resource;

@Provider
public class ApplicationResourceFilter implements DynamicFeature {

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (Resource.class.isAssignableFrom(resourceInfo.getResourceClass())) {
			context.register(RequestHeadersCheckerFilter.class);
			context.register(AuditInResourceFilter.class);
			
			context.register(AuthenticationFilter.class);
			context.register(AuditOutResourceFilter.class);
			context.register(AuthorizationFilter.class);

			context.register(CORSFilter.class);
        }
	}
}
