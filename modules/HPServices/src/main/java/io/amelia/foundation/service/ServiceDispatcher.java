package io.amelia.foundation.service;

import com.sun.istack.internal.NotNull;
import io.amelia.events.EventDispatcher;
import io.amelia.foundation.RegistrarBase;
import io.amelia.foundation.service.events.ServiceRegisterEvent;
import io.amelia.foundation.service.events.ServiceUnregisterEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceDispatcher
{
	private static final Map<Class<?>, List<RegisteredService<?, ServiceProvider>>> providers = new HashMap<>();

	/**
	 * Get a list of known services. A service is known if it has registered providers for it.
	 *
	 * @return a copy of the set of known services
	 */
	public static Set<Class<?>> getKnownServices()
	{
		synchronized ( providers )
		{
			return new HashSet<>( providers.keySet() );
		}
	}

	/**
	 * Queries for a provider. This may return if no provider has been registered for a service. The highest priority provider is returned.
	 *
	 * @param <T>     The service interface
	 * @param service The service interface
	 * @return provider or null
	 * @throws ClassCastException if the registered service was not of type expected
	 */
	@SuppressWarnings( "unchecked" )
	public static <T extends ServiceProvider> T getService( @NotNull Class<?> service ) throws ClassCastException
	{
		synchronized ( providers )
		{
			List<RegisteredService<?, ServiceProvider>> registered = providers.get( service );

			if ( registered == null )
				for ( Class<?> clz : providers.keySet() )
					if ( clz.isAssignableFrom( service ) )
						registered = providers.get( clz );

			if ( registered == null )
				return null;

			// This should not be null!
			return ( T ) registered.get( 0 ).getProvider();
		}
	}

	@SuppressWarnings( "unchecked" )
	public static <T extends ServiceProvider> T getService( @NotNull Class<?> service, @NotNull ServicePriority priority ) throws ClassCastException
	{
		synchronized ( providers )
		{
			for ( Class<?> clz : providers.keySet() )
				if ( clz.isAssignableFrom( service ) )
					for ( RegisteredService<?, ServiceProvider> provider : providers.get( clz ) )
						if ( provider.getPriority().equals( priority ) )
							return ( T ) provider.getProvider();
		}
		return null;
	}

	@SuppressWarnings( "unchecked" )
	public static <T extends ServiceProvider> List<T> getServiceList( @NotNull Class<?> service ) throws ClassCastException
	{
		synchronized ( providers )
		{
			return providers.entrySet().stream().filter( e -> e.getKey().isAssignableFrom( service ) ).flatMap( e -> e.getValue().stream() ).map( p -> ( T ) p.getProvider() ).collect( Collectors.toList() );
		}
	}

	/**
	 * Queries for a provider registration. This may return if no provider has been registered for a service.
	 *
	 * @param <C>     The service interface
	 * @param service The service interface
	 * @return provider registration or null
	 */
	@SuppressWarnings( "unchecked" )
	public static <C> RegisteredService<C, ServiceProvider> getServiceRegistration( @NotNull Class<C> service )
	{
		synchronized ( providers )
		{
			List<RegisteredService<?, ServiceProvider>> registered = providers.get( service );

			if ( registered == null )
				return null;

			// This should not be null!
			return ( RegisteredService<C, ServiceProvider> ) registered.get( 0 );
		}
	}

	/**
	 * Get registrations of providers for a service. The returned list is an unmodifiable copy.
	 *
	 * @param <C>     The service interface
	 * @param service The service interface
	 * @return a copy of the list of registrations
	 */
	@SuppressWarnings( "unchecked" )
	public static <C> Stream<RegisteredService<C, ServiceProvider>> getServiceRegistrations( @NotNull Class<C> service )
	{
		synchronized ( providers )
		{
			return providers.get( service ).stream().map( r -> ( RegisteredService<C, ServiceProvider> ) r );
		}
	}

	/**
	 * Get registrations of providers for a context.
	 *
	 * @param context The context
	 * @return provider registration or null
	 */
	public static Stream<RegisteredService<?, ServiceProvider>> getServiceRegistrations( @NotNull RegistrarBase context )
	{
		synchronized ( providers )
		{
			return providers.values().stream().flatMap( Collection::stream ).filter( p -> p.getRegistrar().equals( context ) );
		}
	}

	/**
	 * Register a provider of a service.
	 *
	 * @param <T>      Provider
	 * @param service  service class
	 * @param provider provider to register
	 * @param context  context with the provider
	 * @param priority priority of the provider
	 */
	@SuppressWarnings( "unchecked" )
	public static <T, P extends ServiceProvider> void registerService( @NotNull Class<T> service, @NotNull P provider, @NotNull RegistrarBase context, @NotNull ServicePriority priority )
	{
		RegisteredService<T, P> registeredProvider = null;
		synchronized ( providers )
		{
			List<RegisteredService<?, ServiceProvider>> registered = providers.get( service );
			if ( registered == null )
			{
				registered = new ArrayList<>();
				providers.put( service, registered );
			}

			registeredProvider = new RegisteredService<>( service, provider, context, priority );

			// Insert the provider into the collection, much more efficient big O than sort
			int position = Collections.binarySearch( registered, registeredProvider );
			if ( position < 0 )
				registered.add( -( position + 1 ), ( RegisteredService<?, ServiceProvider> ) registeredProvider );
			else
				registered.add( position, ( RegisteredService<?, ServiceProvider> ) registeredProvider );

		}
		EventDispatcher.callEvent( new ServiceRegisterEvent<T>( registeredProvider ) );
	}

	/**
	 * Returns whether a provider has been registered for a service.
	 *
	 * @param <C>     service
	 * @param service service to check
	 * @return true if and only if there are registered providers
	 */
	public static <C> boolean serviceIsProvidedFor( @NotNull Class<C> service )
	{
		synchronized ( providers )
		{
			return providers.containsKey( service );
		}
	}

	/**
	 * Unregister a particular provider for a particular service.
	 *
	 * @param service  The service interface
	 * @param provider The service provider implementation
	 */
	@SuppressWarnings( {"rawtypes", "unchecked"} )
	public static void serviceUnregister( @NotNull Class<?> service, @NotNull ServiceProvider provider )
	{
		ArrayList<ServiceUnregisterEvent<?>> unregisteredEvents = new ArrayList<ServiceUnregisterEvent<?>>();
		synchronized ( providers )
		{
			Iterator<Map.Entry<Class<?>, List<RegisteredService<?, ServiceProvider>>>> it = providers.entrySet().iterator();

			try
			{
				while ( it.hasNext() )
				{
					Map.Entry<Class<?>, List<RegisteredService<?, ServiceProvider>>> entry = it.next();

					// We want a particular service
					if ( entry.getKey() != service )
						continue;

					Iterator<RegisteredService<?, ServiceProvider>> it2 = entry.getValue().iterator();

					try
					{
						// Removed entries that are from this context

						while ( it2.hasNext() )
						{
							RegisteredService<?, ServiceProvider> registered = it2.next();

							if ( registered.getProvider() == provider )
							{
								it2.remove();
								unregisteredEvents.add( new ServiceUnregisterEvent( registered ) );
							}
						}
					}
					catch ( NoSuchElementException e )
					{ // Why does Java suck
					}

					// Get rid of the empty list
					if ( entry.getValue().size() == 0 )
						it.remove();
				}
			}
			catch ( NoSuchElementException e )
			{
				// Ignore
			}
		}
		for ( ServiceUnregisterEvent<?> event : unregisteredEvents )
			EventDispatcher.callEvent( event );
	}

	/**
	 * Unregister a particular provider.
	 *
	 * @param provider The service provider implementation
	 */
	@SuppressWarnings( {"rawtypes", "unchecked"} )
	public static void serviceUnregister( @NotNull Object provider )
	{
		ArrayList<ServiceUnregisterEvent<?>> unregisteredEvents = new ArrayList<ServiceUnregisterEvent<?>>();
		synchronized ( providers )
		{
			Iterator<Map.Entry<Class<?>, List<RegisteredService<?, ServiceProvider>>>> it = providers.entrySet().iterator();

			try
			{
				while ( it.hasNext() )
				{
					Map.Entry<Class<?>, List<RegisteredService<?, ServiceProvider>>> entry = it.next();
					Iterator<RegisteredService<?, ServiceProvider>> it2 = entry.getValue().iterator();

					try
					{
						// Removed entries that are from this context

						while ( it2.hasNext() )
						{
							RegisteredService<?, ServiceProvider> registered = it2.next();

							if ( registered.getProvider().equals( provider ) )
							{
								it2.remove();
								unregisteredEvents.add( new ServiceUnregisterEvent( registered ) );
							}
						}
					}
					catch ( NoSuchElementException e )
					{ // Why does Java suck
					}

					// Get rid of the empty list
					if ( entry.getValue().size() == 0 )
						it.remove();
				}
			}
			catch ( NoSuchElementException e )
			{
				// Ignore
			}
		}
		for ( ServiceUnregisterEvent<?> event : unregisteredEvents )
			EventDispatcher.callEvent( event );
	}

	/**
	 * Unregister all the providers registered by a particular context.
	 *
	 * @param context The context
	 */
	@SuppressWarnings( {"rawtypes", "unchecked"} )
	public static void serviceUnregisterAll( @NotNull Object context )
	{
		ArrayList<ServiceUnregisterEvent<?>> unregisteredEvents = new ArrayList<>();
		synchronized ( providers )
		{
			Iterator<Map.Entry<Class<?>, List<RegisteredService<?, ServiceProvider>>>> it = providers.entrySet().iterator();

			try
			{
				while ( it.hasNext() )
				{
					Map.Entry<Class<?>, List<RegisteredService<?, ServiceProvider>>> entry = it.next();
					Iterator<RegisteredService<?, ServiceProvider>> it2 = entry.getValue().iterator();

					try
					{
						// Removed entries that are from this context
						while ( it2.hasNext() )
						{
							RegisteredService<?, ServiceProvider> registered = it2.next();

							if ( registered.getRegistrar().equals( context ) )
							{
								it2.remove();
								unregisteredEvents.add( new ServiceUnregisterEvent( registered ) );
							}
						}
					}
					catch ( NoSuchElementException e )
					{

					}

					// Get rid of the empty list
					if ( entry.getValue().size() == 0 )
						it.remove();
				}
			}
			catch ( NoSuchElementException e )
			{
				// Ignore
			}
		}
		for ( ServiceUnregisterEvent<?> event : unregisteredEvents )
			EventDispatcher.callEvent( event );
	}

	private ServiceDispatcher()
	{
		// Static
	}

	/**
	 * @param <T> Provided Service Interface
	 * @param <P> The Service Provider
	 */
	public static class RegisteredService<T, P extends ServiceProvider> implements Comparable<RegisteredService<?, ?>>
	{
		private RegistrarBase context;
		private ServicePriority priority;
		private P provider;
		private Class<T> service;

		public RegisteredService( @NotNull Class<T> service, @NotNull P provider, @NotNull RegistrarBase context, @NotNull ServicePriority priority )
		{
			this.service = service;
			this.context = context;
			this.provider = provider;
			this.priority = priority;
		}

		@Override
		public int compareTo( RegisteredService<?, ?> o )
		{
			if ( priority.ordinal() == o.getPriority().ordinal() )
				return 0;
			else
				return priority.ordinal() < o.getPriority().ordinal() ? 1 : -1;
		}

		public ServicePriority getPriority()
		{
			return priority;
		}

		public P getProvider()
		{
			return provider;
		}

		public RegistrarBase getRegistrar()
		{
			return context;
		}

		public Class<T> getService()
		{
			return service;
		}
	}
}