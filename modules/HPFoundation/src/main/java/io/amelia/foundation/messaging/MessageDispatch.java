/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.messaging;

import io.amelia.foundation.events.Events;
import io.amelia.foundation.events.EventException;
import io.amelia.foundation.events.messaging.MessageEvent;
import io.amelia.foundation.binding.Bindings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Handles the delivery and receiving of chat messages
 */
public class MessageDispatch
{
	public static final String BINDING_PREFIX = "io.amelia.messaging";

	private static final Map<MessageChannel, List<MessageReceiver>> channels = new ConcurrentHashMap<>();

	public static Collection<MessageReceiver> channelRecipients( MessageChannel channel )
	{
		return Collections.unmodifiableList( channels.get( channel ) );
	}

	public static void channelRegister( MessageChannel channel, MessageReceiver receiver )
	{
		channels.compute( channel, ( k, v ) -> v == null ? new ArrayList<>() : v ).add( receiver );
	}

	public static void channelUnregister( MessageChannel channel, MessageReceiver... receivers )
	{
		if ( channels.containsKey( channel ) )
			channels.get( channel ).removeAll( Arrays.asList( receivers ) );
	}

	public static void sendMessage( MessageBuilder builder ) throws MessageException
	{
		MessageEvent event = new MessageEvent( builder.getSender(), builder.compileReceivers(), builder.getMessages() );
		try
		{
			Events.callEventWithException( event );
		}
		catch ( EventException e )
		{
			throw new MessageException( "Encountered an exception while trying to deliver a message", builder.getSender(), builder.getMessages().collect( Collectors.toList() ), e );
		}
		if ( !event.isCancelled() && !event.getRecipients().isEmpty() && !event.getMessages().isEmpty() )
			for ( MessageReceiver dest : event.getRecipients() )
				dest.sendMessage( event.getSender(), event.getMessages() );
	}

	/**
	 * Attempts to send specified object to every initialized Account
	 *
	 * @param objs The objects to send
	 */
	public static void sendMessage( Object... objs ) throws MessageException
	{
		List<MessageReceiver> receivers = Bindings.getReference( BINDING_PREFIX ).fetch( MessageReceiver.class ).filter( MessageReceiver::validate ).collect( Collectors.toList() );
		sendMessage( MessageBuilder.msg( objs ).to( receivers ) );
	}
}
