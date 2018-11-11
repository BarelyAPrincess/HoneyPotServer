/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.networking.messages;

import java.util.Objects;

import io.amelia.lang.NetworkException;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.udp.UDPWorker;
import io.amelia.support.Encrypt;
import io.amelia.support.NIO;
import io.amelia.support.data.Parcel;
import io.amelia.support.data.StackerBase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Interface structure for NetworkMessages.
 * <p>
 * Legend:
 * ORIGIN = The connection the message originated from.
 * REMOTE = The connection that received the message and might responded.
 */
public abstract class NetworkMessage
{
	private final Parcel dataLastReceived;
	private final Parcel dataToBeCommitted = new Parcel();
	private long messageComittedTime = -1;
	private int messageCount = 0;
	private String messageId;
	private MessageLifecycle messageLifecycle = MessageLifecycle.CREATED;
	private String originId;

	/* @Override
	public String toString()
	{
		return getClass().getSimpleName() + "{State: " + getMessageState().name() + ", Committed: " + ( isCommitted() ? "Yes" : "No" ) + "}";
	}*/

	/**
	 * Instigate message as a ORIGIN
	 */
	protected NetworkMessage()
	{
		messageId = Encrypt.hash();
		originId = getManager().getId();

		dataLastReceived = null;
	}

	/**
	 * Instigate message as a REMOTE
	 */
	protected NetworkMessage( ByteBuf encoded ) throws NetworkException.Error
	{
		messageCount = encoded.readInt() + 1;
		messageId = NIO.decodeStringFromByteBuf( encoded );
		originId = NIO.decodeStringFromByteBuf( encoded );

		dataLastReceived = new Parcel();

		// dataLastReceived = ParcelLoader.decodeJson( NIO.readByteBufToInputStream( encoded ) );

		dataLastReceived.addFlag( StackerBase.Flag.READ_ONLY );
	}

	protected abstract void beforeCommit();

	protected ByteBuf commit()
	{
		if ( isCommitted() )
			throw NetworkException.ignorable( "Message is already committed!" );

		setMessageLifecycle( MessageLifecycle.COMMITTED );

		ByteBuf buf = Unpooled.buffer();

		buf.writeInt( messageCount );
		NIO.encodeStringToByteBuf( buf, messageId );
		NIO.encodeStringToByteBuf( buf, originId );

		// buf.writeBytes( dataToBeCommitted.encodeToStream() );

		return buf;
	}

	/**
	 * Informs the message handler if a data response is expected from the ORIGIN.
	 *
	 * @return True is a response is expected
	 */
	public boolean expectsResponse()
	{
		return false;
	}

	public UDPWorker getManager()
	{
		return NetworkLoader.UDP();
	}

	public MessageLifecycle getMessageLifecycle()
	{
		return messageLifecycle;
	}

	/* public MessageState getMessageState()
	{
		return messageState;
	}*/

	private void setMessageLifecycle( MessageLifecycle messageLifecycle )
	{
		if ( this.messageLifecycle == messageLifecycle )
			return;
		if ( this.messageLifecycle == MessageLifecycle.COMMITTED && messageLifecycle == MessageLifecycle.CREATED )
			throw NetworkException.ignorable( "Message can't be uncommitted!" );
		if ( this.messageLifecycle == MessageLifecycle.COMMITTED )
			throw NetworkException.ignorable( "Message is finished!" );

		this.messageLifecycle = messageLifecycle;
	}

	public Parcel getOriginDataMap()
	{
		return dataLastReceived;
	}

	public String getOriginId()
	{
		return originId;
	}

	public Parcel getRemoteDataMap()
	{
		return dataToBeCommitted;
	}

	public boolean isCommitted()
	{
		return messageLifecycle == MessageLifecycle.COMMITTED;
	}

	public boolean isOrigin()
	{
		return Objects.equals( getOriginId(), getManager().getId() );
	}

	protected void onResponse()
	{
		// Only fires on ORIGIN
		messageLifecycle = MessageLifecycle.COMMITTED;
	}

	protected enum MessageLifecycle
	{
		CREATED,
		EXISTING,
		COMMITTED
	}
}
