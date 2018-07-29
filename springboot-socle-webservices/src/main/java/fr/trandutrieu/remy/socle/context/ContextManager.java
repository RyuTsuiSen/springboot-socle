package fr.trandutrieu.remy.socle.context;

public class ContextManager {
	
	private static final InheritableThreadLocal<ContextBean> context = new InheritableThreadLocal<>(); ;
	
	public static ContextBean get() {
		return context.get();
	}
	
	public static void set(ContextBean bean){
		context.set(bean);
	}
	
	public static void remove(){
		context.remove();
	}
}
