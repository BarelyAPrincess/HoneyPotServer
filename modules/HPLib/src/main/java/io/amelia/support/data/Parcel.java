/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support.data;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import io.amelia.lang.ParcelableException;
import io.amelia.support.Reflection;

/**
 * TODO Add value filter method?
 */
public class Parcel extends StackerWithValue<Parcel, Object> implements ValueTypesTrait
{
	public Parcel()
	{
		super( Parcel::new, "" );
	}

	protected Parcel( String key )
	{
		super( Parcel::new, key );
	}

	protected Parcel( Parcel parent, String key )
	{
		super( Parcel::new, parent, key );
	}

	protected Parcel( Parcel parent, String key, Object value )
	{
		super( Parcel::new, parent, key, value );
	}

	public final <T> T getParcelable( String key ) throws ParcelableException.Error
	{
		if ( !hasChild( key ) )
			return null;

		try
		{
			return Factory.deserialize( getChild( key ) );
		}
		catch ( ClassNotFoundException e )
		{
			throw new ParcelableException.Error( this, e );
		}
	}

	public final <T> T getParcelable( String key, Class<T> objClass ) throws ParcelableException.Error
	{
		if ( !hasChild( key ) )
			return null;

		return Factory.deserialize( getChild( key ), objClass );
	}

	@Override
	public void throwExceptionError( String message ) throws ParcelableException.Error
	{
		throw new ParcelableException.Error( this, message );
	}

	@Override
	public void throwExceptionIgnorable( String message ) throws ParcelableException.Ignorable
	{
		throw new ParcelableException.Ignorable( this, message );
	}

	/**
	 * Used to serialize an Object to a {@link Parcel} and vice-versa,
	 * as well as, deserialize from bytes, e.g., file or network.
	 */
	public static class Factory
	{
		private static final Map<Class<?>, ParcelSerializer<?>> serializers = new HashMap<>();

		public static <T> T deserialize( Parcel src, Class<T> objClass ) throws ParcelableException.Error
		{
			ParcelSerializer<T> serializer = getClassSerializer( objClass );

			if ( serializer == null )
			{
				Parcelable parcelable = objClass.getAnnotation( Parcelable.class );
				try
				{
					serializer = parcelable.value().newInstance();
					registerClassSerializer( objClass, serializer );
				}
				catch ( InstantiationException | IllegalAccessException ignore )
				{
					// Ignore
				}
			}

			if ( serializer == null )
			{
				try
				{
					return objClass.getConstructor( Parcel.class ).newInstance( src );
				}
				catch ( NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ignore )
				{
					// Ignore
				}
			}

			return serializer == null ? null : serializer.readFromParcel( src );
		}

		@SuppressWarnings( "unchecked" )
		public static <T> T deserialize( Parcel src ) throws ClassNotFoundException, ParcelableException.Error
		{
			if ( !src.hasChild( "$class" ) )
				throw new ParcelableException.Ignorable( null, "Something went wrong! The Parcel doesn't contain reference to which class we're to deserialize to." );
			return deserialize( src, ( Class<T> ) Class.forName( src.getString( "$class" ).get() ) );
		}

		@SuppressWarnings( "unchecked" )
		public static <T> ParcelSerializer<T> getClassSerializer( Class<T> objClass )
		{
			synchronized ( serializers )
			{
				return ( ParcelSerializer<T> ) serializers.get( objClass );
			}
		}

		public static boolean isSerializable( Object obj )
		{
			return !( obj instanceof Parcel ) && ( serializers.containsKey( obj.getClass() ) || Reflection.hasAnnotation( obj.getClass(), Parcelable.class ) );
		}

		public static void registerClassSerializer( Class<?> objClass, ParcelSerializer<?> parcelable )
		{
			synchronized ( serializers )
			{
				if ( serializers.containsKey( objClass ) )
					throw new ParcelableException.Ignorable( null, "The class " + objClass.getSimpleName() + " is already registered." );
				serializers.put( objClass, parcelable );
			}
		}

		public static <T> void serialize( @Nonnull T src, @Nonnull Parcel desc ) throws ParcelableException.Error
		{
			if ( src instanceof Parcel )
				throw new ParcelableException.Error( null, "You can't serialize a Parcel to a Parcel." );

			ParcelSerializer<T> serializer = getClassSerializer( ( Class<T> ) src.getClass() );

			if ( serializer == null )
			{
				Parcelable parcelable = src.getClass().getAnnotation( Parcelable.class );
				try
				{
					serializer = parcelable.value().newInstance();
					registerClassSerializer( src.getClass(), serializer );
				}
				catch ( InstantiationException | IllegalAccessException ignore )
				{
					// Ignore
				}
			}

			if ( serializer == null )
				throw new ParcelableException.Error( null, "We were unable to find a serializer for class " + src.getClass().getSimpleName() );

			serializer.writeToParcel( src, desc );

			desc.setValue( "$class", src.getClass().getSimpleName() );
		}

		public static <T> Parcel serialize( @Nonnull T src ) throws ParcelableException.Error
		{
			Parcel desc = new Parcel();
			serialize( src, desc );
			return desc;
		}

		private Factory()
		{
			// Static Access
		}
	}
}
