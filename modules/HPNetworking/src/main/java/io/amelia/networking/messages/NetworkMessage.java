package io.amelia.networking.messages;

import io.amelia.foundation.ConfigLoader;
import io.amelia.lang.NetworkException;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.udp.UDPWorker;
import io.amelia.support.data.Parcel;
import io.amelia.support.Encrypt;
import io.amelia.support.NIO;
import io.amelia.support.data.ParcelLoader;
import io.amelia.support.data.StackerBase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Objects;

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
	protected NetworkMessage( ByteBuf encoded ) throws NetworkException
	{
		messageCount = encoded.readInt() + 1;
		messageId = NIO.decodeStringFromByteBuf( encoded );
		originId = NIO.decodeStringFromByteBuf( encoded );

		dataLastReceived = new Parcel();

		dataLastReceived = ParcelLoader.decodeJson( NIO.readByteBufToInputStream( encoded ) );

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

		buf.writeBytes( dataToBeCommitted.encodeToStream() );

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

	private void setMessageLifecycle( MessageLifecycle messageLifecycle )
	{
		if ( this.messageLifecycle == messageLifecycle )
			return;
		if ( this.messageLifecycle == MessageLifecycle.FINISHED )
			throw NetworkException.ignorable( "Message is finished!" );
		if ( this.messageLifecycle == MessageLifecycle.COMMITTED && messageLifecycle == MessageLifecycle.CREATED )
			throw NetworkException.ignorable( "Message can't be uncommitted!" );

		this.messageLifecycle = messageLifecycle;
	}

	public MessageState getMessageState()
	{
		return messageState;
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
		messageLifecycle = MessageLifecycle.FINISHED;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{State: " + getMessageState().name() + ", Committed: " + ( isCommitted() ? "Yes" : "No" ) + "}";
	}

	protected enum MessageLifecycle
	{
		CREATED,
		EXISTING,
		COMMITTED
	}
}
