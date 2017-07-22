/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.events;

import io.amelia.config.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.NaggableRegistrar;
import io.amelia.foundation.RegistrarBase;
import io.amelia.lang.AuthorNagException;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.annotation.DeprecatedDetail;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.support.Objs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class EventDispatcher
{
	public static final Logger L = LogBuilder.get( EventDispatcher.class );

	private static Map<Class<? extends AbstractEvent>, EventHandlers> handlers = new ConcurrentHashMap<>();

	private static Object lock = new Object();
	private static boolean useTimings = ConfigRegistry.getBoolean( "plugins.useTimings" );

	/**
	 * Calls an event with the given details.<br>
	 * This method only synchronizes when the event is not asynchronous.
	 *
	 * @param event Event details
	 */
	public static <T extends AbstractEvent> T callEvent( T event )
	{
		try
		{
			return callEventWithException( event );
		}
		catch ( EventException ex )
		{
			// Ignore
		}

		return event;
	}

	/**
	 * Calls an event with the given details.<br>
	 * This method only synchronizes when the event is not asynchronous.
	 *
	 * @param event Event details
	 * @throws EventException Thrown if you try to call an async event on a sync thread
	 */
	public static <T extends AbstractEvent> T callEventWithException( T event ) throws EventException
	{
		if ( event.isAsynchronous() )
		{
			if ( Thread.holdsLock( lock ) )
				throw new IllegalStateException( event.getEventName() + " cannot be triggered asynchronously from inside synchronized code." );
			if ( Kernel.isPrimaryThread() )
				throw new IllegalStateException( event.getEventName() + " cannot be triggered asynchronously from primary server thread." );
			fireEvent( event );
		}
		else
			synchronized ( lock )
			{
				fireEvent( event );
			}

		return event;
	}

	public static Map<Class<? extends AbstractEvent>, Set<RegisteredListener>> createRegisteredListeners( Listener listener, final RegistrarBase registrar )
	{
		Objs.notNull( registrar, "Registrar can not be null" );
		Objs.notNull( listener, "Listener can not be null" );

		Map<Class<? extends AbstractEvent>, Set<RegisteredListener>> ret = new HashMap<>();
		Set<Method> methods;
		try
		{
			Method[] publicMethods = listener.getClass().getMethods();
			methods = new HashSet<>( publicMethods.length, Float.MAX_VALUE );
			for ( Method method : publicMethods )
				methods.add( method );
			for ( Method method : listener.getClass().getDeclaredMethods() )
				methods.add( method );
		}
		catch ( NoClassDefFoundError e )
		{
			L.severe( String.format( "Plugin %s has failed to register events for %s because %s does not exist.", registrar.getName(), listener.getClass(), e.getMessage() ) );
			return ret;
		}

		for ( final Method method : methods )
		{
			final EventHandler eh = method.getAnnotation( EventHandler.class );
			if ( eh == null )
				continue;
			final Class<?> checkClass;
			if ( method.getParameterTypes().length != 1 || !AbstractEvent.class.isAssignableFrom( checkClass = method.getParameterTypes()[0] ) )
			{
				L.severe( registrar.getName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass() );
				continue;
			}
			final Class<? extends AbstractEvent> eventClass = checkClass.asSubclass( AbstractEvent.class );
			method.setAccessible( true );
			Set<RegisteredListener> eventSet = ret.get( eventClass );
			if ( eventSet == null )
			{
				eventSet = new HashSet<>();
				ret.put( eventClass, eventSet );
			}

			if ( ReportingLevel.E_DEPRECATED.isEnabled() )
				for ( Class<?> clazz = eventClass; AbstractEvent.class.isAssignableFrom( clazz ); clazz = clazz.getSuperclass() )
				{
					if ( clazz.isAnnotationPresent( DeprecatedDetail.class ) )
					{
						DeprecatedDetail deprecated = clazz.getAnnotation( DeprecatedDetail.class );

						L.warning( String.format( "The creator '%s' has registered a listener for %s on method '%s', but the event is Deprecated for reason '%s'.", registrar.getName(), clazz.getName(), method.toGenericString(), deprecated.reason() ) );
						break;
					}

					if ( clazz.isAnnotationPresent( Deprecated.class ) )
					{
						L.warning( String.format( "The creator '%s' has registered a listener for %s on method '%s', but the event is Deprecated!", registrar.getName(), clazz.getName(), method.toGenericString() ) );
						break;
					}
				}

			EventExecutor executor = ( listener1, event ) ->
			{
				try
				{
					if ( !eventClass.isAssignableFrom( event.getClass() ) )
						return;
					method.invoke( listener1, event );
				}
				catch ( InvocationTargetException ex )
				{
					throw new EventException( ex.getCause() );
				}
				catch ( Throwable t )
				{
					throw new EventException( t );
				}
			};

			if ( useTimings )
				eventSet.add( new TimedRegisteredListener( listener, executor, eh.priority(), registrar, eh.ignoreCancelled() ) );
			else
				eventSet.add( new RegisteredListener( listener, executor, eh.priority(), registrar, eh.ignoreCancelled() ) );
		}
		return ret;
	}

	private static void fireEvent( AbstractEvent event ) throws EventException
	{
		for ( RegisteredListener registration : getEventListeners( event.getClass() ) )
		{
			if ( !registration.getContext().isEnabled() )
				continue;

			try
			{
				registration.callEvent( event );
			}
			catch ( AuthorNagException ex )
			{
				if ( registration.getContext() instanceof NaggableRegistrar )
				{
					NaggableRegistrar naggableRegistrar = ( NaggableRegistrar ) registration.getContext();

					if ( naggableRegistrar.isNaggable() )
					{
						naggableRegistrar.setNaggable( false );
						L.log( Level.SEVERE, String.format( "Nag author(s): '%s' of '%s' about the following: %s", naggableRegistrar.getMeta().getAuthors(), naggableRegistrar.getName(), ex.getMessage() ) );
					}
				}
			}
			catch ( EventException ex )
			{
				if ( ex.getCause() == null )
				{
					ex.printStackTrace();
					L.log( Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getContext().getName() + "\nEvent Exception Reason: " + ex.getMessage() );
				}
				else
				{
					ex.getCause().printStackTrace();
					L.log( Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getContext().getName() + "\nEvent Exception Reason: " + ex.getCause().getMessage() );
				}
				throw ex;
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getContext().getName(), ex );
			}
		}

		if ( event instanceof SelfHandling )
			( ( SelfHandling ) event ).handle();
	}

	private static EventHandlers getEventListeners( Class<? extends AbstractEvent> event )
	{
		EventHandlers eventHandlers = handlers.get( event );

		if ( eventHandlers == null )
		{
			eventHandlers = new EventHandlers();
			handlers.put( event, eventHandlers );
		}

		return eventHandlers;
	}

	public static void registerEvent( Class<? extends AbstractEvent> event, Listener listener, EventPriority priority, EventExecutor executor, RegistrarBase registrar )
	{
		registerEvent( event, listener, priority, executor, registrar, false );
	}

	/**
	 * Registers the given event to the specified listener using a directly passed EventExecutor
	 *
	 * @param event           Event class to register
	 * @param listener        Listener to register
	 * @param priority        Priority of this event
	 * @param executor        EventExecutor to register
	 * @param registrar       Registrar of event registration
	 * @param ignoreCancelled Do not call executor if event was already cancelled
	 */
	public static void registerEvent( Class<? extends AbstractEvent> event, Listener listener, EventPriority priority, EventExecutor executor, RegistrarBase registrar, boolean ignoreCancelled )
	{
		Objs.notNull( listener, "Listener cannot be null" );
		Objs.notNull( priority, "Priority cannot be null" );
		Objs.notNull( executor, "Executor cannot be null" );
		Objs.notNull( registrar, "Registrar cannot be null" );

		if ( useTimings )
			getEventListeners( event ).register( new TimedRegisteredListener( listener, executor, priority, registrar, ignoreCancelled ) );
		else
			getEventListeners( event ).register( new RegisteredListener( listener, executor, priority, registrar, ignoreCancelled ) );
	}

	public static void registerEvents( Listener listener, RegistrarBase registrar )
	{
		for ( Map.Entry<Class<? extends AbstractEvent>, Set<RegisteredListener>> entry : createRegisteredListeners( listener, registrar ).entrySet() )
			getEventListeners( entry.getKey() ).registerAll( entry.getValue() );
	}

	public static void unregisterEvents( EventRegistrar creator )
	{
		EventHandlers.unregisterAll( creator );
	}

	public static void unregisterEvents( Listener listener )
	{
		EventHandlers.unregisterAll( listener );
	}

	public static boolean useTimings()
	{
		return useTimings;
	}

	/**
	 * Sets whether or not per event timing code should be used
	 *
	 * @param use True if per event timing code should be used
	 */
	public static void useTimings( boolean use )
	{
		useTimings = use;
	}

	private EventDispatcher()
	{
		// Static Class
	}
}
