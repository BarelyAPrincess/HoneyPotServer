package io.amelia.foundation;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Supplier;

import io.amelia.lang.ParcelableException;
import io.amelia.support.data.Parcel;
import io.amelia.support.data.Parcelable;

/**
 * Defines a carrier containing an data object that can be sent to a {@link ParcelHandler}.
 *
 * <p class="note">The best way to get one of these is to call {@link #obtain Message.obtain()}
 * method, which will pull from a pool of recycled objects.</p>
 */
public class ParcelCarrier implements Parcelable
{
	private static final NavigableSet<ParcelCarrier> unusedPool = new TreeSet<>();

	public static final Serializer<ParcelCarrier> SERIALIZER = new Serializer<ParcelCarrier>()
	{
		public ParcelCarrier[] newArray( int size )
		{
			return new ParcelCarrier[size];
		}

		public ParcelCarrier readFromParcel( Parcel source ) throws ParcelableException.Error
		{
			ParcelCarrier msg = ParcelCarrier.obtain();
			msg.readFromParcel( source );
			return msg;
		}

		@Override
		public void writeToParcel( ParcelCarrier msg, Parcel out ) throws ParcelableException.Error
		{
			msg.writeToParcel( out );
		}
	};

	/**
	 * Return an instance from the global unused pool. Allows us to avoid allocating new objects in many cases.
	 */
	public static ParcelCarrier obtain()
	{
		synchronized ( unusedPool )
		{
			ParcelCarrier msg = unusedPool.pollFirst();
			if ( msg == null )
				msg = new ParcelCarrier();
			return msg;
		}
	}

	/**
	 * Same as {@link #obtain()}, but copies the values of an existing
	 * message into the new one.
	 *
	 * @param orig Original message to copy.
	 *
	 * @return A Message object from the global pool.
	 */
	public static ParcelCarrier obtain( ParcelCarrier orig )
	{
		ParcelCarrier newQueueEntry = obtain();
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
	private ParcelHandler receiver;
	/**
	 * Indicates the location of the PostalSender. This will either be local or remote over a network connection.
	 */
	private PostOffice replyTo;
	/**
	 * Senders indicate the source of the incoming message. These might be Receivers themselves or any other data producing object, check responsibly.
	 */
	private PostalSender sender;

	ParcelCarrier()
	{
		payload = new Parcel();
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
	 * Retrieve the a {@link ParcelHandler} implementation that
	 * will receive this message. The object must implement
	 * {@link ParcelHandler#handleMessage(Message)}.
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

	public void readFromParcel( Parcel src ) throws ParcelableException.Error
	{
		Supplier<ParcelableException.Error> exp = () -> new ParcelableException.Error( src, "Failure to readFromParcel(). Was the Parcel constructed from a Message?" );

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
		synchronized ( unusedPool )
		{
			isInUse = false;
			payload = null;

			unusedPool.add( this );
		}
	}

	/**
	 * Sends this Message to the Handler specified by {@link #getReceiver}.
	 * Throws a null pointer exception if this field has not been set.
	 */
	public void sendToTarget()
	{
		receiver.sendMessage( this );
	}

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
