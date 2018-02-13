/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support.data.serialization;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for storing and retrieving classes for {@link io.amelia.support.data.StackerWithValue}.
 */
public class DataSerialization
{
	public static final String SERIALIZED_TYPE_KEY = "==";
	private static Map<String, Class<? extends DataSerializable>> aliases = new HashMap<String, Class<? extends DataSerializable>>();

	static
	{
		/*
		 * registerClass( Vector.class );
		 * registerClass( BlockVector.class );
		 * registerClass( ItemStack.class );
		 * registerClass( Color.class );
		 * registerClass( PotionEffect.class );
		 * registerClass( FireworkEffect.class );
		 */
	}

	/**
	 * Attempts to deserialize the given arguments into a new instance of the given class.
	 * <p/>
	 * The class must implement {@link DataSerializable}, including the extra methods as specified in the javadoc of ConfigurationSerializable.
	 * <p/>
	 * If a new instance could not be made, an example being the class not fully implementing the interface, null will be returned.
	 *
	 * @param args  Arguments for deserialization
	 * @param clazz Class to deserialize into
	 * @return New instance of the specified class
	 */
	public static DataSerializable deserializeObject( Map<String, Object> args, Class<? extends DataSerializable> clazz )
	{
		return new DataSerialization( clazz ).deserialize( args );
	}

	/**
	 * Attempts to deserialize the given arguments into a new instance of the given class.
	 * <p/>
	 * The class must implement {@link DataSerializable}, including the extra methods as specified in the javadoc of ConfigurationSerializable.
	 * <p/>
	 * If a new instance could not be made, an example being the class not fully implementing the interface, null will be returned.
	 *
	 * @param args Arguments for deserialization
	 * @return New instance of the specified class
	 */
	public static DataSerializable deserializeObject( Map<String, Object> args )
	{
		Class<? extends DataSerializable> clazz = null;

		if ( args.containsKey( SERIALIZED_TYPE_KEY ) )
		{
			try
			{
				String alias = ( String ) args.get( SERIALIZED_TYPE_KEY );

				if ( alias == null )
					throw new IllegalArgumentException( "Cannot have null alias" );
				clazz = getClassByAlias( alias );
				if ( clazz == null )
					throw new IllegalArgumentException( "Specified class does not exist ('" + alias + "')" );
			}
			catch ( ClassCastException ex )
			{
				ex.fillInStackTrace();
				throw ex;
			}
		}
		else
		{
			throw new IllegalArgumentException( "Args doesn't contain type key ('" + SERIALIZED_TYPE_KEY + "')" );
		}

		return new DataSerialization( clazz ).deserialize( args );
	}

	/**
	 * Gets the correct alias for the given {@link DataSerializable} class
	 *
	 * @param clazz Class to get alias for
	 * @return Alias to use for the class
	 */
	public static String getAlias( Class<? extends DataSerializable> clazz )
	{
		DelegateDeserialization delegate = clazz.getAnnotation( DelegateDeserialization.class );

		if ( delegate == null )
		{
			SerializableAs alias = clazz.getAnnotation( SerializableAs.class );

			if ( ( alias != null ) && ( alias.value() != null ) )
				return alias.value();
		}
		else
		{
			if ( ( delegate.value() == null ) || ( delegate.value() == clazz ) )
				delegate = null;
			else
				return getAlias( delegate.value() );
		}

		return clazz.getName();
	}

	/**
	 * Attempts to get a registered {@link DataSerializable} class by its alias
	 *
	 * @param alias Alias of the serializable
	 * @return Registered class, or null if not found
	 */
	public static Class<? extends DataSerializable> getClassByAlias( String alias )
	{
		return aliases.get( alias );
	}

	/**
	 * Registers the given {@link DataSerializable} class by its alias
	 *
	 * @param clazz Class to register
	 */
	public static void registerClass( Class<? extends DataSerializable> clazz )
	{
		DelegateDeserialization delegate = clazz.getAnnotation( DelegateDeserialization.class );

		if ( delegate == null )
		{
			registerClass( clazz, getAlias( clazz ) );
			registerClass( clazz, clazz.getName() );
		}
	}

	/**
	 * Registers the given alias to the specified {@link DataSerializable} class
	 *
	 * @param clazz Class to register
	 * @param alias Alias to register as
	 * @see SerializableAs
	 */
	public static void registerClass( Class<? extends DataSerializable> clazz, String alias )
	{
		aliases.put( alias, clazz );
	}

	/**
	 * Unregisters the specified alias to a {@link DataSerializable}
	 *
	 * @param alias Alias to unregister
	 */
	public static void unregisterClass( String alias )
	{
		aliases.remove( alias );
	}

	/**
	 * Unregisters any aliases for the specified {@link DataSerializable} class
	 *
	 * @param clazz Class to unregister
	 */
	public static void unregisterClass( Class<? extends DataSerializable> clazz )
	{
		while ( aliases.values().remove( clazz ) )
		{
		}
	}

	private final Class<? extends DataSerializable> clazz;

	protected DataSerialization( Class<? extends DataSerializable> clazz )
	{
		this.clazz = clazz;
	}

	public DataSerializable deserialize( Map<String, Object> args )
	{
		if ( args == null )
			throw new IllegalArgumentException( "Args must not be null" );

		DataSerializable result = null;
		Method method = null;

		if ( result == null )
		{
			method = getMethod( "deserialize", true );

			if ( method != null )
				result = deserializeViaMethod( method, args );
		}

		if ( result == null )
		{
			method = getMethod( "valueOf", true );

			if ( method != null )
				result = deserializeViaMethod( method, args );
		}

		if ( result == null )
		{
			Constructor<? extends DataSerializable> constructor = getConstructor();

			if ( constructor != null )
				result = deserializeViaCtor( constructor, args );
		}

		return result;
	}

	protected DataSerializable deserializeViaCtor( Constructor<? extends DataSerializable> ctor, Map<String, Object> args )
	{
		try
		{
			return ctor.newInstance( args );
		}
		catch ( Throwable ex )
		{
			Logger.getLogger( DataSerialization.class.getName() ).log( Level.SEVERE, "Could not call constructor '" + ctor.toString() + "' of " + clazz + " for deserialization", ex instanceof InvocationTargetException ? ex.getCause() : ex );
		}

		return null;
	}

	protected DataSerializable deserializeViaMethod( Method method, Map<String, Object> args )
	{
		try
		{
			DataSerializable result = ( DataSerializable ) method.invoke( null, args );

			if ( result == null )
				Logger.getLogger( DataSerialization.class.getName() ).log( Level.SEVERE, "Could not call method '" + method.toString() + "' of " + clazz + " for deserialization: method returned null" );
			else
				return result;
		}
		catch ( Throwable ex )
		{
			Logger.getLogger( DataSerialization.class.getName() ).log( Level.SEVERE, "Could not call method '" + method.toString() + "' of " + clazz + " for deserialization", ex instanceof InvocationTargetException ? ex.getCause() : ex );
		}

		return null;
	}

	protected Constructor<? extends DataSerializable> getConstructor()
	{
		try
		{
			return clazz.getConstructor( Map.class );
		}
		catch ( SecurityException | NoSuchMethodException ex )
		{
			return null;
		}
	}

	protected Method getMethod( String name, boolean isStatic )
	{
		try
		{
			Method method = clazz.getDeclaredMethod( name, Map.class );

			if ( !DataSerializable.class.isAssignableFrom( method.getReturnType() ) )
				return null;
			if ( Modifier.isStatic( method.getModifiers() ) != isStatic )
				return null;

			return method;
		}
		catch ( NoSuchMethodException | SecurityException ex )
		{
			return null;
		}
	}
}
