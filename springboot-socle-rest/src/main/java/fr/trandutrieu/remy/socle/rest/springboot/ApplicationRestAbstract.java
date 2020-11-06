package fr.trandutrieu.remy.socle.rest.springboot;

import com.netflix.config.ConfigurationManager;
import fr.trandutrieu.remy.socle.AdapterServlet;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;


@SpringBootApplication
public abstract class ApplicationRestAbstract {

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

	@Bean
	public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> contextRoot() {
		return factory -> factory.setContextPath("/eaas-st-rest-api");
	}
	
    @Bean
    public ServletRegistrationBean<AdapterServlet> myServletRegistration () {
        ServletRegistrationBean<AdapterServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new AdapterServlet());
        srb.setUrlMappings(Arrays.asList("/adapters.stream"));
        return srb;
    }
	
}
