/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.events.builtin;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.amelia.foundation.events.Cancellable;
import io.amelia.foundation.parcel.ParcelReceiver;
import io.amelia.foundation.parcel.ParcelSender;
import io.amelia.support.Lists;

/**
 * Fired when a system message will be delivered
 */
@Deprecated
public class MessageEvent extends ApplicationEvent implements Cancellable
{
	private final ParcelSender sender;
	private boolean cancelled = false;
	private Collection<Object> objs;
	private Collection<ParcelReceiver> recipients;

	public MessageEvent( final ParcelSender sender, final Collection<ParcelReceiver> recipients, final Object... objs )
	{
		this.sender = sender;
		this.recipients = recipients;
		this.objs = Arrays.asList( objs );
	}

	public void addMessage( Object obj )
	{
		objs.add( obj );
	}

	public void addRecipient( ParcelReceiver acct )
	{
		recipients.add( acct );
	}

	public boolean containsRecipient( ParcelReceiver acct )
	{
		for ( ParcelReceiver acct1 : recipients )
			if ( acct1.getId().equals( acct.getId() ) )
				return true;
		return false;
	}

	public Collection<Object> getMessages()
	{
		return objs;
	}

	/**
	 * WARNING! This will completely clear and reset the messages.
	 *
	 * @param objs The new messages
	 */
	public void setMessages( Iterable<Object> objs )
	{
		this.objs = Lists.newArrayList( objs );
	}

	@SuppressWarnings( "unchecked" )
	public <T> List<T> getObjectMessages( Class<T> clz )
	{
		return objs.stream().filter( o -> o.getClass() == clz ).map( o -> ( T ) o ).collect( Collectors.toList() );
	}

	public Collection<ParcelReceiver> getRecipients()
	{
		return recipients;
	}

	public void setRecipients( Set<ParcelReceiver> recipients )
	{
		this.recipients = recipients;
	}

	public ParcelSender getSender()
	{
		return sender;
	}

	public List<String> getStringMessages()
	{
		return getObjectMessages( String.class );
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	@Override
	public void setCancelled( boolean cancel )
	{
		cancelled = cancel;
	}

	public Object removeMessage( int index )
	{
		return objs.remove( index );
	}

	public boolean removeMessage( Object obj )
	{
		return objs.remove( obj );
	}

	public boolean removeRecipient( ParcelReceiver acct )
	{
		for ( ParcelReceiver acct1 : recipients )
			if ( acct1.getId().equals( acct.getId() ) )
				return recipients.remove( acct1 );
		return false;
	}

	/**
	 * WARNING! This will completely clear and reset the messages.
	 *
	 * @param objs The new messages
	 */
	public void setMessages( Object... objs )
	{
		this.objs = Arrays.asList( objs );
	}
}
