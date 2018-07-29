package fr.trandutrieu.remy.socle.externalcall;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;

import fr.trandutrieu.remy.socle.audit.Audit;
import fr.trandutrieu.remy.socle.audit.Audit.Level;
import fr.trandutrieu.remy.socle.externalcall.AdapterCall.TYPE_APPEL;
import fr.trandutrieu.remy.socle.externalcall.exceptions.ExternalCallRuntimeException;

public class ExternalCall extends HystrixCommand<ExternalCallResponse> {

	private TYPE_APPEL typeAppel;

	protected ExternalCall(TYPE_APPEL typeAppel, String nameCircuit) {
		
		  super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(nameCircuit))
	                .andCommandKey(HystrixCommandKey.Factory.asKey(nameCircuit))
	                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("MAINFRAME"))
		  			.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
					.withExecutionTimeoutInMilliseconds(5000)
					.withCircuitBreakerRequestVolumeThreshold(10)
					.withCircuitBreakerSleepWindowInMilliseconds(15000)
					.withMetricsRollingStatisticalWindowInMilliseconds(30000)));
		
		this.typeAppel = typeAppel;
	}

	@Override
	protected ExternalCallResponse run() throws Exception {
		ExternalCallResponse externalCallResponse = new ExternalCallResponse();
		int nombreAleatoire=0;
		switch(typeAppel) {
		case OK:
		case ERREUR_DEV:
	    	DynamicStringProperty sampleProp = DynamicPropertyFactory.getInstance().getStringProperty("stringprop", "");
	    	externalCallResponse.setReponse(sampleProp.get());
			nombreAleatoire = (int)(Math.random() * 4);
			Thread.sleep(nombreAleatoire*1000);
			break;
		case EXECUTION_ISSUE:
			nombreAleatoire = (int)(Math.random() * 2);
			Thread.sleep(nombreAleatoire*1000);
			HystrixCommandMetrics metrics2 = this.getMetrics();
			Audit.trace(Level.DEBUG, "EXTERNAL_CALL", metrics2.getHealthCounts().toString());
			throw new ExternalCallRuntimeException("Probleme a l'execution de l'appel externe");
		case TIMEOUT:
			nombreAleatoire = 6 + (int)(Math.random() * 5);
			Thread.sleep(nombreAleatoire*1000);
			break;
		default:
			break;

		}

		HystrixCommandMetrics metrics2 = this.getMetrics();
		Audit.trace(Level.DEBUG, "EXTERNAL_CALL", metrics2.getHealthCounts().toString());
		return externalCallResponse;
	}
	
	
	@Override
	protected ExternalCallResponse getFallback() {
		Audit.trace(Level.DEBUG, "EXTERNAL_CALL", "Une solution de repli peut etre faite");
		return super.getFallback();
	}
}
