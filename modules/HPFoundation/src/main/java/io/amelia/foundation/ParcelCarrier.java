package io.amelia.foundation;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.amelia.lang.ParcelableException;
import io.amelia.support.data.Parcel;
import io.amelia.support.data.ParcelSerializer;
import io.amelia.support.data.Parcelable;

/**
 * Defines a carrier containing an data object that can be sent to a {@link ApplicationRouter}.
 *
 * <p class="note">The best way to get one of these is to call {@link #obtain Message.obtain()}
 * method, which will pull from a pool of recycled objects.</p>
 */
@Parcelable( ParcelCarrier.Serializer.class )
public class ParcelCarrier
{
	private static final NavigableSet<ParcelCarrier> unusedPool = new TreeSet<>();

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
	 * Indicates if the Message was received from over a remote connection. (e.g., Network)
	 */
	private boolean isRemote;
	/**
	 * The message payload to be delivered to the PostalReceiver.
	 * If the target receiver is NOT local, the payload must be a parcel or parcelable.
	 */
	private Parcel payload;
	/**
	 * Receivers have the ability to process queued incoming messages.
	 */
	private ApplicationRouter target;
	/**
	 * Indicates the location of the PostalSender. This will either be local or remote over a network connection.
	 */
	private ApplicationRouter source;

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
			payload = new Parcel();
		return payload;
	}

	/**
	 * Retrieve the a {@link ApplicationRouter} implementation that
	 * will receive this message. The object must implement
	 * {@link ApplicationRouter#handleParcel(ParcelCarrier)}.
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
			payload = src.getParcelable( "parcelable" );
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

	public void setPayload( @Nonnull Object payload ) throws ParcelableException.Error
	{
		if ( !Parcel.Factory.isSerializable( payload ) )
			throw new IllegalArgumentException( "Payload object must be serializable." );
		this.payload = Parcel.Factory.serialize( payload );
	}

	public void setPayload( @Nonnull Parcel payload )
	{
		this.payload = payload;
	}

	public static class Serializer implements ParcelSerializer<ParcelCarrier>
	{
		@Override
		public ParcelCarrier readFromParcel( Parcel src ) throws ParcelableException.Error
		{
			ParcelCarrier parcelCarrier = ParcelCarrier.obtain();
			parcelCarrier.readFromParcel( src );
			return parcelCarrier;
		}

		@Override
		public void writeToParcel( ParcelCarrier parcelCarrier, Parcel dest ) throws ParcelableException.Error
		{
			dest.setValue( "payload", parcelCarrier.payload );
		}
	}
}
