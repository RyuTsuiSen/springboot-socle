package fr.trandutrieu.remy.socle.externalcall;

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.MDC;

import fr.trandutrieu.remy.socle.context.ContextBean;
import fr.trandutrieu.remy.socle.context.ContextManager;

public class ConnectorContextCallable<K> implements Callable<K> {

	private final Callable<K> actual;
	private final Map<String, String> parentMDC;
	private final ContextBean parentContextIMD;

	public ConnectorContextCallable(Callable<K> actual) {
		this.actual = actual;
		this.parentMDC = MDC.getCopyOfContextMap();
		this.parentContextIMD = ContextManager.get();
	}

	@Override
	public K call() throws Exception {
		Map<String, String> childMDC = MDC.getCopyOfContextMap();
		ContextBean childContextIMD = ContextManager.get();
		try {
			MDC.setContextMap(parentMDC);
			ContextManager.set(parentContextIMD);
			return actual.call();
		} finally {
			MDC.setContextMap(childMDC);
			ContextManager.set(childContextIMD);
		}
	}
}
