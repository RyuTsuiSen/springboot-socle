package fr.trandutrieu.remy.socle.webservices.soap.springboot;

import java.util.Arrays;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.cxf.Bus;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import com.netflix.config.ConfigurationManager;

import fr.trandutrieu.remy.socle.AdapterServlet;

@SpringBootApplication
public abstract class ApplicationWebSOAPAbstract extends SpringBootServletInitializer{
    
	static {
		System.setProperty("archaius.configurationSource.additionalUrls", "file:folder/config.properties");
		System.setProperty("archaius.dynamicPropertyFactory.registerConfigWithJMX", "true");

		
		ConfigurationManager.getConfigInstance().addConfigurationListener(new ConfigurationListener() {
			@Override
			public void configurationChanged(ConfigurationEvent event) {
				if(event.isBeforeUpdate()) {
					AbstractConfiguration manager = ConfigurationManager.getConfigInstance();
					LoggerFactory.getLogger("AUDIT.PROPERTIES").trace("Quelqu'un a change la valeur de " + event.getPropertyName() + " : [old="+ manager.getString(event.getPropertyName(), "ERROR") +"] / [new="+ event.getPropertyValue()+"] ");
				}
			}
		}); 
	}
	
	@Autowired
    protected Bus bus;
    
	
    @Bean
    public ServletRegistrationBean<AdapterServlet> myServletRegistration () {
        ServletRegistrationBean<AdapterServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new AdapterServlet());
        srb.setUrlMappings(Arrays.asList("/adapters.stream"));
        return srb;
    }
}
