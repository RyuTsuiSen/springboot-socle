package fr.trandutrieu.remy.socle.rest;

import fr.trandutrieu.remy.socle.rest.filter.ApplicationResourceFilter;
import fr.trandutrieu.remy.socle.rest.filter.ProcessAndAuditBusinessExceptionMapper;
import fr.trandutrieu.remy.socle.rest.filter.ProcessAndAuditErrorMapper;
import fr.trandutrieu.remy.socle.rest.filter.ProcessAndAuditRuntimeExceptionMapper;
import fr.trandutrieu.remy.socle.rest.resources.MappersResource;
import fr.trandutrieu.remy.socle.rest.resources.WhoAmIResource;
import org.glassfish.jersey.server.ResourceConfig;

public abstract class JerseyConfiguration extends ResourceConfig {
	public JerseyConfiguration() {
		super();
		setUp();
	}

	public void setUp() {
		register(ApplicationResourceFilter.class);
		register(ProcessAndAuditErrorMapper.class);
		register(ProcessAndAuditRuntimeExceptionMapper.class);
		register(ProcessAndAuditBusinessExceptionMapper.class);
		register(WhoAmIResource.class);
		register(MappersResource.class);
		init();
	}

	public abstract void init();
	
}