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

import io.amelia.foundation.App;
import io.amelia.foundation.RegistrarBase;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.annotation.DeprecatedDetail;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.support.Objs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

public class EventDispatcher
{
	public static final Logger L = LogBuilder.get( EventDispatcher.class );

	private static Map<Class<? extends AbstractEvent>, EventHandlers> handlers = new ConcurrentHashMap<>();

	private static Object lock = new Object();

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
			if ( App.isPrimaryThread() )
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

	private static void fireEvent( AbstractEvent event ) throws EventException
	{
		event.onEventPreCall();

		for ( RegisteredListener registration : getEventListeners( event.getClass() ) )
		{
			if ( !registration.getRegistrar().isEnabled() )
				continue;

			// TODO Look into if exceptions needs better handling, such as reporting them to plugin vendors.
			try
			{
				registration.callEvent( event );
			}
			catch ( EventException ex )
			{
				if ( ex.getCause() == null )
				{
					ex.printStackTrace();
					L.log( Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getRegistrar().getName() + "\nEvent Exception Reason: " + ex.getMessage() );
				}
				else
				{
					ex.getCause().printStackTrace();
					L.log( Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getRegistrar().getName() + "\nEvent Exception Reason: " + ex.getCause().getMessage() );
				}
				throw ex;
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getRegistrar().getName(), ex );
			}
		}

		event.onEventPostCall();
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

	private static void listen( final RegistrarBase registrar, final EventListener listener, final Method method ) throws EventException
	{
		final EventHandler eventHandler = method.getAnnotation( EventHandler.class );
		if ( eventHandler == null )
			return;

		final Class<?> checkClass;
		if ( method.getParameterTypes().length != 1 || !AbstractEvent.class.isAssignableFrom( checkClass = method.getParameterTypes()[0] ) )
			throw new EventException( "The EventHandler method signature \"" + method.toGenericString() + "\" in \"" + listener.getClass() + "\" is invalid. It must has at least one argument with an event that extends AbstractEvent." );

		final Class<? extends AbstractEvent> eventClass = checkClass.asSubclass( AbstractEvent.class );
		method.setAccessible( true );

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

		listen( registrar, eventHandler.priority(), eventClass, ( e ) ->
		{

		} );
	}

	public static void listen( final RegistrarBase registrar, final EventListener listener )
	{
		Objs.notNull( registrar, "Registrar can not be null" );
		Objs.notNull( listener, "Listener can not be null" );

		try
		{
			Set<Method> methods = new HashSet<>();
			methods.addAll( Arrays.asList( listener.getClass().getMethods() ) );
			methods.addAll( Arrays.asList( listener.getClass().getDeclaredMethods() ) );
			for ( Method method : methods )
				listen( registrar, listener, method );
		}
		catch ( NoClassDefFoundError e )
		{
			// TODO Does this need better handling? Shouldn't it pass to the caller in some form?
			L.severe( String.format( "%s has failed to register events for %s because %s does not exist.", registrar.getName(), listener.getClass(), e.getMessage() ) );
		}
		catch ( EventException e )
		{
			L.severe( e.getMessage() );
		}
	}

	public static <E extends AbstractEvent> void listen( RegistrarBase registrar, Class<E> event, Consumer<E> listener )
	{
		listen( registrar, EventPriority.NORMAL, event, listener );
	}

	/**
	 * Registers the given event to the specified listener using a directly passed EventExecutor
	 *
	 * @param registrar Registrar of event registration
	 * @param priority  Priority of this event
	 * @param event     Event class to register
	 * @param listener  Consumer that will receive the event
	 */
	public static <E extends AbstractEvent> void listen( RegistrarBase registrar, EventPriority priority, Class<E> event, Consumer<E> listener )
	{
		Objs.notNull( registrar, "Registrar cannot be null" );
		Objs.notNull( priority, "Priority cannot be null" );
		Objs.notNull( event, "Event cannot be null" );
		Objs.notNull( listener, "Listener cannot be null" );

		getEventListeners( event ).register( new RegisteredListener<>( registrar, priority, listener ) );
	}

	public static void unregisterEvents( RegistrarBase registrar )
	{
		EventHandlers.unregisterAll( registrar );
	}

	private EventDispatcher()
	{
		// Static Class
	}
}
