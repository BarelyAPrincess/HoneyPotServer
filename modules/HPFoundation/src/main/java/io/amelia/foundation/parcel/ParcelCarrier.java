/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.parcel;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.ParcelException;
import io.amelia.lang.ParcelableException;
import io.amelia.support.data.Parcel;
import io.amelia.support.data.ParcelSerializer;
import io.amelia.support.data.Parcelable;

/**
 * Defines a carrier containing data and signals to be transmitted over network, IPC, and by reference.
 * A {@link ParcelSender} is the origin of the carrier, while {@link ParcelReceiver} is the target of the carrier.
 *
 * <p class="note">The best way to get one of these is to call {@link #obtain}
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
		ParcelCarrier parcelCarrier = obtain();

		parcelCarrier.payloadParcel = orig.payloadParcel;
		parcelCarrier.payloadObject = orig.payloadObject;
		parcelCarrier.origin = orig.origin;
		parcelCarrier.replyTo = orig.replyTo;
		parcelCarrier.targetReceiver = orig.targetReceiver;

		return parcelCarrier;
	}

	public static ParcelCarrier obtain( ResultCode code, Object payload )
	{
		ParcelCarrier parcelCarrier = obtain( code );
		parcelCarrier.setPayload( payload );
		return parcelCarrier;
	}

	public static ParcelCarrier obtain( int code, Object payload )
	{
		ParcelCarrier parcelCarrier = obtain( code );
		parcelCarrier.setPayload( payload );
		return parcelCarrier;
	}

	public static ParcelCarrier obtain( ResultCode code )
	{
		ParcelCarrier parcelCarrier = obtain();
		parcelCarrier.setCode( code );
		return parcelCarrier;
	}

	public static ParcelCarrier obtain( int code )
	{
		ParcelCarrier parcelCarrier = obtain();
		parcelCarrier.setCode( code );
		return parcelCarrier;
	}

	/**
	 * The parcel code
	 */
	private int code = ResultCode.DEFAULT.getCode();
	/**
	 * Indicates the Parcel has been transmitted. This prevents remote {@link ParcelInterface} from modifying the parcel intentionally or accidentally.
	 */
	private boolean finalized = false;
	/**
	 * Indicates if the Message was received from over a remote connection. (e.g., Network)
	 */
	private boolean isRemote;
	/**
	 * Indicates the location of the PostalSender. This will either be local or remote over a network connection.
	 */
	private ParcelSender origin;
	/**
	 * The raw payload object. Will be preserved as is if transmitted locally, otherwise,
	 * it will be checked if it can be serialized to a Parcel when sent over the network.
	 */
	private Object payloadObject;
	/**
	 * The Parcel payload.
	 */
	private Parcel payloadParcel = new Parcel();
	/**
	 * Explicitly specifies where to send reply parcels.
	 * If null, then {@link ParcelSender#getReplyTo()} is used.
	 */
	private ParcelReceiver replyTo = null;
	/**
	 * The carrier tag
	 */
	private String tag = null;
	/**
	 * The channel to transmit this parcel over.
	 */
	private ParcelChannel targetChannel = null;
	/**
	 * Receiver with the ability to process this parcel.
	 */
	private ParcelReceiver targetReceiver = null;

	public int getCode()
	{
		return code;
	}

	public void setCode( int code )
	{
		if ( ResultCode.isReserved( code ) )
			throw ParcelException.runtime( "The specified code is reserved, as it belongs to the ResultCode enum. Use another code or use the enum instead." );
		this.code = code;
	}

	public Object getPayloadObject()
	{
		return payloadObject;
	}

	public Parcel getPayloadParcel()
	{
		return payloadParcel;
	}

	public String getTag()
	{
		return tag;
	}

	public void setTag( @Nullable String tag )
	{
		notFinalized();
		this.tag = tag;
	}

	public ParcelChannel getTargetChannel()
	{
		return targetChannel;
	}

	public void setTargetChannel( ParcelChannel targetChannel )
	{
		notFinalized();
		this.targetChannel = targetChannel;
	}

	public ParcelReceiver getTargetReceiver()
	{
		return targetReceiver;
	}

	public void setTargetReceiver( ParcelReceiver targetReceiver )
	{
		notFinalized();
		this.targetReceiver = targetReceiver;
	}

	/**
	 * Determines if the carrier is suitable for a network transmission.
	 * First we check for all the required fields, then we check if the payload is safe for network.
	 *
	 * @return True is so, false otherwise.
	 */
	public boolean isTransmittable()
	{
		if ( targetReceiver == null || targetChannel == null )
			return false;
		if ( payloadParcel == null && payloadObject != null && !Parcel.Factory.isSerializable( payloadObject ) )
			return false;

		return true;
	}

	public void markFinalized()
	{
		notFinalized();
		this.finalized = true;
	}

	public void notFinalized()
	{
		if ( finalized )
			throw ParcelException.runtime( "Parcel can't be modified once finalized." );
	}

	public void recycle()
	{
		if ( finalized )
			return;

		recycleUnchecked();
	}

	void recycleUnchecked()
	{
		synchronized ( unusedPool )
		{
			finalized = false;
			payloadParcel = null;
			payloadObject = null;
			origin = null;
			replyTo = null;
			targetReceiver = null;

			unusedPool.add( this );
		}
	}

	public void setCode( ResultCode code )
	{
		this.code = code.getCode();
	}

	public void setOrigin( ParcelSender origin )
	{
		this.origin = origin;
	}

	/**
	 * Sets the payload to be transmitted with this carrier.
	 * <p>
	 * Note: Be advised that objects that don't implement {@link Parcel} or isn't
	 * annotated as being {@link Parcelable}, usually can't be transmittable over
	 * the network. So it's best practice to use {@link Parcel} whenever possible.
	 * Ultimately, it's up to the network implementation if the payload is acceptable.
	 *
	 * @param payload The payload to set on this carrier.
	 */
	public void setPayload( @Nonnull Object payload )
	{
		notFinalized();
		if ( payload instanceof Parcel )
		{
			this.payloadParcel = ( Parcel ) payload;
			this.payloadObject = null;
		}
		else
		{
			this.payloadParcel = null;
			this.payloadObject = payload;
		}
	}

	/**
	 * Allows for the sending and reply of very basic rudimentary signals.
	 * Interpretation of each signal is solely the responsibility of the implementing code.
	 * These codes are also reserved, as to guarantee that a signal is not misinterpreted.
	 */
	public enum ResultCode
	{
		DEFAULT( 0xfc ),
		OKAY( 0xfd ),
		DENIED( 0xfe ),
		UNKNOWN( 0xff );

		private static List<Integer> codes;

		/**
		 * Returns a list of codes used in the enum.
		 *
		 * @return List of codes.
		 */
		public static List<Integer> getCodes()
		{
			if ( codes == null )
				codes = Arrays.stream( ResultCode.values() ).map( ResultCode::getCode ).collect( Collectors.toList() );
			return codes;
		}

		/**
		 * The codes used in this enum are reserved to guarantee misinterpretation from receiving class.
		 *
		 * @param code The code to check.
		 *
		 * @return Is the provided code reserved? True if so, false otherwise.
		 */
		public static boolean isReserved( int code )
		{
			return getCodes().contains( code );
		}

		private int code;

		ResultCode( int code )
		{
			this.code = code;
		}

		public int getCode()
		{
			return code;
		}
	}

	public static class Serializer implements ParcelSerializer<ParcelCarrier>
	{
		@Override
		public ParcelCarrier readFromParcel( Parcel src ) throws ParcelableException.Error
		{
			Supplier<ParcelableException.Error> exp = () -> new ParcelableException.Error( src, "Failure to readFromParcel(). Was the Parcel constructed from a Message?" );

			ParcelCarrier parcelCarrier = ParcelCarrier.obtain();

			parcelCarrier.code = src.getInteger( "code" ).orElseThrow( exp );
			parcelCarrier.tag = src.getString( "tag" ).orElseThrow( exp );

			if ( src.hasValue( "payloadParcel" ) )
				parcelCarrier.payloadParcel = ( Parcel ) src.getValue( "payloadParcel" ).filter( value -> value instanceof Parcel ).orElseThrow( exp );
			else if ( src.hasChild( "payloadObject" ) )
				parcelCarrier.payloadObject = src.getParcelable( "payloadObject" );

			return parcelCarrier;
		}

		@Override
		public void writeToParcel( ParcelCarrier parcelCarrier, Parcel dest ) throws ParcelableException.Error
		{
			// TODO Implement the ability for alternative network implementations to serialize the ParcelCarrier its own way.

			Supplier<ParcelableException.Error> exp = () -> new ParcelableException.Error( dest, "Unable to serialize ParcelCarrier, something is preventing it from being transmittable, either it's missing required fields or the payload can't be serialized." );

			if ( !parcelCarrier.isTransmittable() )
				throw exp.get();

			dest.setValue( "code", parcelCarrier.code );
			dest.setValue( "tag", parcelCarrier.tag );

			if ( parcelCarrier.payloadParcel != null )
				dest.setValue( "payloadParcel", parcelCarrier.payloadParcel );
			else if ( parcelCarrier.payloadObject != null )
				Parcel.Factory.serialize( parcelCarrier.payloadObject, dest.getChildOrCreate( "payloadObject" ) );
		}
	}
}
