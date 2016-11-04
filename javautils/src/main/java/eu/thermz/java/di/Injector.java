package eu.thermz.java.di;

/**
 * 
 * @author riccardo
 */
public class Injector {
	static BindingModule bind = new BindingModule();
	public static void use(BindingModule currentBind){
		bind = currentBind;
	}
	
	public static <T> T inject(Class<T> clazz){
		try {
			return bind.getImplementationOf(clazz).newInstance();
		} catch (Exception ex) {
			throw new RuntimeException("Error injecting dependencies",ex);
		}
	}
	
}
