package fr.trandutrieu.remy.socle.externalcall;

import java.time.Duration;
import java.time.Instant;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

import fr.trandutrieu.remy.socle.audit.Audit;
import fr.trandutrieu.remy.socle.audit.Audit.Level;
import fr.trandutrieu.remy.socle.externalcall.exceptions.ExternalCallCheckedException;

public abstract class AdapterCall {

	static {
		try{
			HystrixPlugins.getInstance().registerConcurrencyStrategy(new ConnectorContextConcurrencyStrategy());
		}
		catch(IllegalStateException e){
			//logger.error("ANORMAL SAUF POUR LES TESTS UNITAIRES : Ne devrait jamais arriver, la strategie {} a ete defini au lieu de {}", HystrixPlugins.getInstance().getConcurrencyStrategy().getClass().getName(), ConnectorContextConcurrencyStrategy.class.getName(), e);
		}

	}
	private static final String ADAPTER_CALL = "ADAPTER CALL";

	public ExternalCallResponse execute(ExternalCallRequest request, TYPE_APPEL typeAppel) throws ExternalCallCheckedException {
		Instant start = Instant.now();
		
		HystrixRequestContext context = HystrixRequestContext.initializeContext();
		try {
			ExternalCall external = new ExternalCall(typeAppel, this.getClass().getSimpleName());
			ExternalCallResponse response =  external.execute();
			traitement(typeAppel);
			handleCheckedException(typeAppel);
			Duration duration = Duration.between(start, Instant.now());
			Audit.trace(Level.INFO, ADAPTER_CALL, this.getClass().getSimpleName() + " a repondu OK execTime=" + duration.toMillis() + "ms");
			return response;
		}
		catch(ExternalCallCheckedException e) {
			Duration duration = Duration.between(start, Instant.now());
			Audit.trace(Level.ERROR, ADAPTER_CALL, this.getClass().getSimpleName() + " a repondu avec un code erreur execTime=" + duration.toMillis() + "ms", e);
			throw e;
		}
		catch(HystrixRuntimeException e) {
			Duration duration = Duration.between(start, Instant.now());
			Audit.trace(Level.ERROR, ADAPTER_CALL, this.getClass().getSimpleName() + " s'est interrompu execTime=" + duration.toMillis() + "ms");
			throw e;
		}
		finally {
			context.close();
		}
	}

	private void traitement(TYPE_APPEL typeAppel) {
		if (typeAppel.equals(TYPE_APPEL.ERREUR_DEV)) {
			Object objetNull = null;
			objetNull.equals(null);
		}
	}

	protected void handleCheckedException(TYPE_APPEL typeAppel) throws ExternalCallCheckedException{
		if(typeAppel.equals(TYPE_APPEL.CHECKED_EXCEPTION)) {
			ExternalCallCheckedException e = new ExternalCallCheckedException("0008=Personne non trouve");
			throw e;
		}
	}

	public enum TYPE_APPEL{
		OK,
		TIMEOUT,
		EXECUTION_ISSUE,
		CHECKED_EXCEPTION,
		ERREUR_DEV,
	}

}
