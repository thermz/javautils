package eu.thermz.java.di;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that represent a group of bindings between interface - implementations.
 * The client is the <b>Injector</b> class. 
 * It uses Object-scoping to call bind method from outside.
 * <br/>
 * Usage:<br/>
 * <pre>{@code
 * new BindingModule(){{
 *   bind(MyInterface.class, MyImplementationClass.class);
 *   bind(AnotherInterface.class, AnotherImplementation.class);
 *  ...
 * }}}
 * </pre>
 * @author riccardo
 */
public class BindingModule {
	
	private final Map<Class<?>,Class<?>> implementations = new HashMap<Class<?>,Class<?>>();
	
	/**
	 * Bind the service interface or abstract class to a service class.
	 * A new serviceClass object will be created via reflection (empty constructor).
	 * 
	 * @param <T> The type of service
	 * @param serviceInterface The interface of service
	 * @param serviceClass The actual class that implements the service object
	 */
	protected <T> void bind(Class<T> serviceInterface, Class<? extends T> serviceClass ) {
		implementations.put(serviceInterface, serviceClass);
	}
	
	@SuppressWarnings("unchecked")
	public <T,S extends T> Class<S> getImplementationOf(Class<T> clazz){
		return (Class<S>)implementations.get(clazz);
	}
}
