/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.messaging;

import com.chiorichan.account.AccountType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Constructs a new message
 */
public class MessageBuilder
{
	public static final String BROADCAST_CHANNEL_ADMINISTRATIVE = "sys.admin";
	public static final String BROADCAST_CHANNEL_USERS = "sys.user";

	public static MessageBuilder msg( Object... objs )
	{
		return new MessageBuilder().addMsg( objs );
	}
	private List<MessageChannel> channels = new ArrayList<>();
	private boolean includeSender = false;
	private Set<Object> objs = new HashSet<>();
	private List<MessageReceiver> receivers = new ArrayList<>();
	private MessageSender sender = null;

	private MessageBuilder()
	{

	}

	public MessageBuilder addMsg( Object... objs )
	{
		this.objs.addAll( Arrays.asList( objs ) );
		return this;
	}

	Collection<MessageReceiver> compileReceivers()
	{
		List<MessageReceiver> receivers = new ArrayList<>();
		receivers.addAll( receivers );
		for ( MessageChannel channel : channels )
			receivers.addAll( MessageDispatch.channelRecipients( channel ) );

		if ( includeSender )
		{
			if ( sender instanceof MessageChannel )
				receivers.addAll( MessageDispatch.channelRecipients( ( MessageChannel ) sender ) );
			else if ( sender instanceof MessageReceiver )
				receivers.add( ( MessageReceiver ) sender );
		}
		else
			for ( MessageReceiver receiver : receivers )
				if ( receiver.getId().equals( sender.getId() ) )
					receivers.remove( receiver );

		return Collections.unmodifiableCollection( receivers );
	}

	public MessageBuilder excludeSender()
	{
		includeSender = false;
		return this;
	}

	public MessageBuilder from( MessageSender sender )
	{
		this.sender = sender;
		return this;
	}

	public Collection<MessageChannel> getChannels()
	{
		return channels;
	}

	public Stream<Object> getMessages()
	{
		return objs.stream();
	}

	public Collection<MessageReceiver> getReceivers()
	{
		return receivers;
	}

	public MessageSender getSender()
	{
		if ( sender == null )
			sender = AccountType.ACCOUNT_ROOT;
		return sender;
	}

	public boolean hasSender()
	{
		return sender != null && !AccountType.isNoneAccount( sender );
	}

	public MessageBuilder includeSender()
	{
		includeSender = true;
		return this;
	}

	public MessageBuilder remove( Collection<MessageReceiver> receivers )
	{
		this.receivers.removeAll( receivers );
		return this;
	}

	public MessageBuilder remove( MessageChannel channel )
	{
		channels.remove( channel );
		return this;
	}

	public MessageBuilder remove( MessageReceiver... receivers )
	{
		this.receivers.removeAll( Arrays.asList( receivers ) );
		return this;
	}

	public MessageBuilder to( Collection<MessageReceiver> receivers )
	{
		this.receivers.addAll( receivers );
		return this;
	}

	public MessageBuilder to( MessageChannel channel )
	{
		channels.add( channel );
		return this;
	}

	public MessageBuilder to( MessageReceiver... receivers )
	{
		this.receivers.addAll( Arrays.asList( receivers ) );
		return this;
	}
}
