/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.events;

/**
 * Represents an event.
 */
public abstract class AbstractEvent
{
	public enum Result
	{
		/**
		 * Deny the event. Depending on the event, the action indicated by the event will either not take place or will be
		 * reverted. Some actions may not be denied.
		 */
		DENY,
		/**
		 * Neither deny nor allow the event. The server will proceed with its normal handling.
		 */
		DEFAULT,
		/**
		 * Allow / Force the event. The action indicated by the event will take place if possible, even if the server
		 * would not normally allow the action. Some actions may not be allowed.
		 */
		ALLOW
	}
	private String name;
	
	private final boolean async;
	
	/**
	 * The default constructor is defined for cleaner code. This constructor assumes the event is synchronous.
	 */
	public AbstractEvent()
	{
		this( false );
	}
	
	/**
	 * This constructor is used to explicitly declare an event as synchronous or asynchronous.
	 * 
	 * @param isAsync
	 *            true indicates the event will fire asynchronously, false by default from default constructor
	 */
	public AbstractEvent( boolean isAsync )
	{
		async = isAsync;
	}
	
	/**
	 * Convenience method for providing a user-friendly identifier. By default, it is the event's class's {@linkplain Class#getSimpleName() simple name}.
	 * 
	 * @return name of this event
	 */
	public String getEventName()
	{
		if ( name == null )
			name = getClass().getSimpleName();
		return name;
	}
	
	/**
	 * Any custom event that should not by synchronized with other events must use the specific constructor. These are
	 * the caveats of using an asynchronous event:
	 * <ul>
	 * <li>The event is never fired from inside code triggered by a synchronous event. Attempting to do so results in an {@link IllegalStateException}.
	 * <li>However, asynchronous event handlers may fire synchronous or asynchronous events
	 * <li>The event may be fired multiple times simultaneously and in any order.
	 * <li>Any newly registered or unregistered handler is ignored after an event starts execution.
	 * <li>The handlers for this event may block for any length of time.
	 * <li>Some implementations may selectively declare a specific event use as asynchronous. This behavior should be clearly defined.
	 * <li>Asynchronous calls are not calculated in the plugin timing system.
	 * </ul>
	 * 
	 * @return false by default, true if the event fires asynchronously
	 */
	public final boolean isAsynchronous()
	{
		return async;
	}

	public final boolean isRemote()
	{
		return false;
	}

	public final boolean isLocal()
	{
		return !isRemote();
	}

	/**
	 * Intended to be overridden to provide event internal signaling
	 */
	protected void onEventPreCall()
	{

	}

	/**
	 * Intended to be overridden to provide event internal signaling
	 */
	protected void onEventPostCall()
	{

	}

	/**
	 * Should we execute the next {@link RegisteredListener}
	 *
	 * @param registeredListener The next {@link RegisteredListener} in the event chain
	 * @return return true to execute
	 */
	protected boolean onEventConditional( RegisteredListener registeredListener )
	{
		return true;
	}
}
