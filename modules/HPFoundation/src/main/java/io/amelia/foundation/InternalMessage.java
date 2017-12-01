package io.amelia.foundation;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Supplier;

import io.amelia.lang.BadParcelableException;
import io.amelia.support.GlobalReference;
import io.amelia.support.data.Parcel;
import io.amelia.support.data.Parcelable;

public class InternalMessage implements Parcelable
{
	private static final GlobalReference<NavigableSet<InternalMessage>> unusedPool = new GlobalReference<>();

	private static volatile NavigableSet<InternalMessage> unusedPool = new TreeSet<>();

	public static final Parcelable.Creator<InternalMessage> CREATOR = new Parcelable.Creator<InternalMessage>()
	{
		public InternalMessage[] newArray( int size )
		{
			return new InternalMessage[size];
		}

		public InternalMessage readFromParcel( Parcel source )
		{
			InternalMessage msg = InternalMessage.obtain();
			msg.readFromParcel( source );
			return msg;
		}
	};

	/**
	 * Return an instance from the global unused pool. Allows us to avoid allocating new objects in many cases.
	 */
	public static InternalMessage obtain()
	{
		synchronized ( unusedPool )
		{
			InternalMessage msg = unusedPool.pollFirst();
			if ( msg == null )
				msg = new InternalMessage();
			return msg;
		}
	}

	/**
	 * Same as {@link #obtain()}, but copies the values of an existing
	 * message into the new one.
	 *
	 * @param orig Original message to copy.
	 * @return A Message object from the global pool.
	 */
	public static InternalMessage obtain( InternalMessage orig )
	{
		InternalMessage newQueueEntry = obtain();
		newQueueEntry.replyTo = orig.replyTo;
		newQueueEntry.payload = orig.payload;
		newQueueEntry.receiver = orig.receiver;

		return newQueueEntry;
	}
	/**
	 * The message code
	 */
	private MessageCode code = MessageCode.DEFAULT;
	/**
	 * Indicates the payload is infact a Parcel instance. If this is true, getParcel will construct a new Parcel.
	 */
	private boolean isPayloadParcel = true;
	/**
	 * Indicates if the Message was received from over a remote connection. (e.g., Network)
	 */
	private boolean isRemote;
	/**
	 * The message payload to be delivered to the PostalReceiver.
	 * If the target receiver is NOT local, the payload must be a parcel or parcelable.
	 */
	private Object payload;
	/**
	 * Receivers have the ability to process queued incoming messages.
	 */
	private LooperReceiver receiver;
	/**
	 * Indicates the location of the PostalSender. This will either be local or remote over a network connection.
	 */
	private PostOffice replyTo;
	/**
	 * Senders indicate the source of the incoming message. These might be Receivers themselves or any other data producing object, check responsibly.
	 */
	private PostalSender sender;

	InternalMessage()
	{
		payload = new Parcel();
	}

	private NavigableSet<InternalMessage> get()
	{
		return unusedPool.get();
	}

	public MessageCode getCode()
	{
		return code;
	}

	public Parcel getParcel()
	{
		if ( payload == null )
			if ( isPayloadParcel )
				payload = new Parcel();
			else
				return null;

		if ( payload instanceof Parcelable )
		{
			Parcel parcel = new Parcel();
			( ( Parcelable ) payload ).writeToParcel( parcel );
			return parcel;
		}

		if ( !( payload instanceof Parcel ) )
			throw new IllegalStateException( "Payload type is not a Parcel" );

		return ( Parcel ) payload;
	}

	public Object getPayload()
	{
		return payload;
	}

	public void setPayload( Object payload )
	{
		if ( isPayloadParcel && !( payload instanceof Parcel ) && !( payload instanceof Parcelable ) )
			throw new IllegalArgumentException( "Payload must be a Parcel or Parcelable" );
		this.payload = payload;
	}

	/**
	 * Retrieve the a {@link LooperReceiver} implementation that
	 * will receive this message. The object must implement
	 * {@link LooperReceiver#handleMessage(Message)}.
	 * Each Handler has its own name-space for
	 * message codes, so you do not need to
	 * worry about yours conflicting with other handlers.
	 */
	public PostalReceiver getReceiver()
	{
		return receiver;
	}

	public void setReceiver( PostalReceiver receiver )
	{
		this.receiver = receiver;
	}

	@Override
	public long getWhen()
	{
		return when;
	}

	public boolean isInUse()
	{
		return isInUse;
	}

	@Override
	public void markInUse( boolean isInUse )
	{
		this.isInUse = isInUse;
	}

	public void readFromParcel( Parcel src ) throws BadParcelableException
	{
		Supplier<BadParcelableException> exp = () -> new BadParcelableException( "Failure to readFromParcel(). Was the Parcel constructed from a Message?" );

		when = src.getLong().orElseThrow( exp );

		if ( src.hasChild( "parcel" ) )
			payload = src.getChild( "parcel" );
		else if ( src.hasChild( "parcelable" ) )
			payload = src.getParcelable( "parcelable", null ); // TODO loader
		else
			payload = src.getValue( "payload" ).orElse( null );
	}

	@Override
	public void recycle()
	{
		if ( isInUse() )
			return;

		recycleUnchecked();
	}

	void recycleUnchecked()
	{
		isInUse = false;
		payload = null;

		unusedPool.add( this );
	}

	/**
	 * Sends this Message to the Handler specified by {@link #getReceiver}.
	 * Throws a null pointer exception if this field has not been set.
	 */
	public void sendToTarget()
	{
		receiver.sendMessage( this );
	}

	@Override
	public void writeToParcel( Parcel dest )
	{
		if ( payload instanceof Parcel )
			dest.setChild( "parcel", ( Parcel ) payload );
		else if ( payload instanceof Parcelable )
			( ( Parcelable ) payload ).writeToParcel( dest.getChildOrCreate( "parcelable" ) );
		else
			dest.setValue( "payload", payload );
	}
}
