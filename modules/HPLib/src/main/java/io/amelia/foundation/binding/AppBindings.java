package io.amelia.foundation.binding;

import com.sun.istack.internal.NotNull;
import io.amelia.config.ConfigNode;
import io.amelia.config.ConfigRegistry;
import io.amelia.events.EventDispatcher;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.facades.FacadePriority;
import io.amelia.foundation.facades.events.FacadeRegisterEvent;
import io.amelia.foundation.facades.interfaces.FacadeService;
import io.amelia.lang.ApplicationException;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppBindings
{
	private static final Map<Class<? extends FacadeService>, List<RegisteredFacade<? extends FacadeService>>> providers = new HashMap<>();
	private static BindingBase bindings = new BindingBase( "" );

	protected static BindingBase getBinding( String path )
	{
		return bindings.getChild( path, true );
	}

	public static <T extends FacadeService> T getFacade( Class<T> serviceClass )
	{
		return getFacadeRegistration( serviceClass ).map( registeredFacade -> ( T ) registeredFacade.getInstance() ).orElse( null );
	}

	/**
	 * Queries for a facade registration. This will return if no facade has been registered.
	 *
	 * @param <T>    The facade interface
	 * @param facade The facade interface
	 * @return provider registration or null
	 */
	@SuppressWarnings( "unchecked" )
	public static <T extends FacadeService> Optional<RegisteredFacade<? extends FacadeService>> getFacadeRegistration( @NotNull Class<T> facade )
	{
		synchronized ( providers )
		{
			return providers.computeIfAbsent( facade, ( k ) -> new ArrayList<>() ).stream().findFirst();
		}
	}

	/**
	 * Get registrations for facade class.
	 *
	 * @param <T>    The facade
	 * @param facade The facade class
	 * @return a stream of registrations
	 */
	@SuppressWarnings( "unchecked" )
	public static <T extends FacadeService> Stream<RegisteredFacade<T>> getFacadeRegistrations( @NotNull Class<T> facade )
	{
		synchronized ( providers )
		{
			return providers.computeIfAbsent( facade, ( k ) -> new ArrayList<>() ).stream().map( r -> ( RegisteredFacade<T> ) r );
		}
	}

	@SuppressWarnings( "unchecked" )
	public static <T extends FacadeService> Stream<T> getFacades( @NotNull Class<?> service ) throws ClassCastException
	{
		synchronized ( providers )
		{
			return providers.entrySet().stream().filter( e -> e.getKey().isAssignableFrom( service ) ).flatMap( e -> e.getValue().stream() ).map( p -> ( T ) p.getInstance() );
		}
	}

	public static Set<Class<? extends FacadeService>> getKnownFacades()
	{
		return providers.keySet();
	}

	public static GenericBinding getReference( String prefix, String path )
	{
		GenericBinding binding = new GenericBinding();

		binding.supplier = GenericBinding::new;
		binding.prefix = Namespace.parseString( prefix );
		binding.binding = getBinding( path );

		return binding;
	}

	/**
	 * Returns a subclass BindingReference.
	 *
	 * @param bindingSupplier The subclass supplier. Using BindingSubclass::create is recommended.
	 * @param prefix          The path prefix, used to create jailed namespaces.
	 * @param path            The path to amend to the jailed namespace.
	 * @param <T>             The subclass extending our BindingReference.
	 * @return Instance of the BindingReference
	 */
	public static <T extends BindingReference> T getReference( Supplier<T> bindingSupplier, String prefix, String path )
	{
		@NotNull
		T bindingReference = bindingSupplier.get();

		bindingReference.supplier = bindingSupplier;
		bindingReference.prefix = Namespace.parseString( prefix );
		bindingReference.binding = getBinding( prefix + "." + path );

		return bindingReference;
	}

	public static GenericBinding getReference( String path )
	{
		return getReference( "", path );
	}

	public static <R, T extends FacadeService> R ifFacadePresent( Class<T> serviceClass, Function<T, R> consumer )
	{
		return getFacadeRegistration( serviceClass ).map( registeredFacade -> consumer.apply( ( T ) registeredFacade.getInstance() ) ).orElse( null );
	}

	public static void init()
	{
		// Load Builtin Facades First

		// Load Facades from Config

		ConfigNode facades = ConfigRegistry.getChild( "bindings.facades" );
		facades.getChildren().forEach( c ->
		{
			if ( c.hasChild( "class" ) && c.hasChild( "priority" ) )
			{
				Class<FacadeService> facadeClass = c.getStringAsClass( "class", FacadeService.class );
				FacadePriority priority = c.getEnum( "priority", FacadePriority.class );

				registerFacade( facadeClass, priority, () -> Objs.initClass( facadeClass ) );
			}
			else
				Kernel.L.warning( "We found malformed arguments in the facade config for key -> " + c.getName() );
		} );
	}

	/**
	 * Returns whether a facade has been registered
	 *
	 * @param <T>     facade
	 * @param service facade to check
	 * @return true if and only if the facade is registered
	 */
	public static <T> boolean isFacadeRegistered( @NotNull Class<T> service )
	{
		synchronized ( providers )
		{
			return providers.containsKey( service );
		}
	}

	public static <T extends FacadeService> void registerFacade( Class<T> facadeClass, FacadePriority priority, Supplier<T> serviceSupplier )
	{
		RegisteredFacade<T> registeredFacade = new RegisteredFacade<>( facadeClass, priority, serviceSupplier );

		synchronized ( providers )
		{
			List<RegisteredFacade<? extends FacadeService>> priorityList = providers.computeIfAbsent( facadeClass, ( k ) -> new ArrayList<>() );

			// Insert the provider into the collection, much more efficient big O than sort
			int position = Collections.binarySearch( priorityList, registeredFacade );
			if ( position < 0 )
				priorityList.add( -( position + 1 ), registeredFacade );
			else
				priorityList.add( position, registeredFacade );
		}

		EventDispatcher.callEvent( new FacadeRegisterEvent<T>( registeredFacade ) );
	}

	public static class BindingsLookup<T>
	{
		private Class<?> aClass = null;
		private String key = null;

		private BindingsLookup()
		{

		}

		public List<T> asList()
		{
			return asStream().collect( Collectors.toList() );
		}

		public Stream<T> asStream()
		{
			return child().collect( v -> aClass == null ? v.fetch() : v.fetch( aClass ) ).flatMap( s -> s ).map( o -> ( T ) o );
		}

		private BindingBase child()
		{
			return ( key == null ? bindings : bindings.getChild( key, true ) );
		}

		public Stream<T> collect( Function<Object, T> function )
		{
			return child().collect( v -> v.fetch( o -> aClass == null || o.getClass() == aClass ? function.apply( o ) : null ) ).flatMap( s -> s );
		}

		private <C> BindingsLookup<C> copy()
		{
			BindingsLookup<C> bindingsLookup = new BindingsLookup<>();
			bindingsLookup.key = key;
			bindingsLookup.aClass = aClass;
			return bindingsLookup;
		}

		public <C> BindingsLookup<C> filterClass( Class<C> aClass )
		{
			BindingsLookup<C> bindingsLookup = copy();
			bindingsLookup.aClass = aClass;
			return bindingsLookup;
		}

		public BindingsLookup<T> filterKey( String key )
		{
			BindingsLookup<T> bindingsLookup = copy();
			bindingsLookup.key = key;
			return bindingsLookup;
		}
	}

	/**
	 * @param <T> Facade Interface
	 */
	public static class RegisteredFacade<T extends FacadeService> implements Comparable<RegisteredFacade<?>>
	{
		private final Class<T> facadeClass;
		private final FacadePriority priority;
		private final Supplier<T> supplier;
		private T instance = null;

		public RegisteredFacade( @NotNull Class<T> facadeClass, @NotNull FacadePriority priority, @NotNull Supplier<T> supplier )
		{
			this.facadeClass = facadeClass;
			this.supplier = supplier;
			this.priority = priority;
		}

		@Override
		public int compareTo( RegisteredFacade<?> o )
		{
			if ( priority.ordinal() == o.getPriority().ordinal() )
				return 0;
			else
				return priority.ordinal() < o.getPriority().ordinal() ? 1 : -1;
		}

		public void destoryInstance() throws ApplicationException
		{
			// TODO Run exception through exception handler
			if ( instance != null )
				instance.onDestory();
			instance = null;
		}

		public T getInstance()
		{
			if ( instance == null )
				instance = supplier.get();
			return instance;
		}

		public FacadePriority getPriority()
		{
			return priority;
		}

		public Class<T> getServiceClass()
		{
			return facadeClass;
		}
	}
}
