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

import io.amelia.foundation.events.EventException;

import java.util.List;

/**
 * Thrown for problems encountered within the MessageDispatch class
 */
public class MessageException extends Exception
{
	private static final long serialVersionUID = 6236409081662686334L;
	private final List<Object> objs;
	private final MessageSender sender;

	public MessageException( String message, MessageSender sender, List<Object> objs )
	{
		this( message, sender, objs, null );
	}

	public MessageException( String message, MessageSender sender, List<Object> objs, EventException cause )
	{
		super( message, cause );

		this.sender = sender;
		this.objs = objs;
	}

	public List<Object> getMessages()
	{
		return objs;
	}

	public MessageSender getSender()
	{
		return sender;
	}
}
