package fr.trandutrieu.remy.socle.externalcall;

import java.util.concurrent.Callable;

import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;

public class ConnectorContextConcurrencyStrategy extends HystrixConcurrencyStrategy {
	@Override
	public ConnectorContextCallable<?> wrapCallable(Callable callable) {
		return new ConnectorContextCallable(callable);
	}
}
