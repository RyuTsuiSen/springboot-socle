package fr.trandutrieu.remy.socle.webservices.rest.resources;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixKey;

@Component
@Path("adapters")
public class MappersResource {
	@GET
	public Response listerAdapters() {
		
		Collection<HystrixCommandMetrics> instances = HystrixCommandMetrics.getInstances();
		StringBuilder sb = new StringBuilder();
		for(HystrixCommandMetrics instance : instances) {
			sb.append(getName(instance.getCommandKey())).append(";").append(getName(instance.getCommandGroup())).append(";").append(getName(instance.getThreadPoolKey())).append("\n");
		}
		
		return Response.ok(sb.toString(), MediaType.TEXT_PLAIN).build();
	}
	
	private String getName(HystrixKey key) {
		return key.name();
	}
}
