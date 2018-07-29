package fr.trandutrieu.remy.socle.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import fr.trandutrieu.remy.socle.WhoAmI;


@Component
@Path("whoami")
public class WhoAmIResource {
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response whoAmI() {
		return Response.ok(WhoAmI.getInstance()).build();
	}
}
