package io.amelia.support.data;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import io.amelia.lang.ParcelableException;

/**
 * TODO Add value filter method?
 */
public class Parcel extends StackerWithValue<Parcel, Object> implements ValueTypesOutline
{
	private static final Map<ClassLoader, HashMap<String, Parcelable.Serializer>> mCreators = new HashMap<>();

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

	public final <T extends Parcelable> T getCreator( Parcelable.Serializer<T> creator, ClassLoader loader )
	{
		if ( creator instanceof Parcelable.ClassLoaderCreator<?> )
			return ( ( Parcelable.ClassLoaderCreator<T> ) creator ).readFromParcel( this, loader );
		return creator.readFromParcel( this );
	}

	public final <T extends Parcelable> T getParcelable( String key, ClassLoader loader )
	{
		Parcelable.Serializer<T> creator = getParcelableCreator( key, loader );
		if ( creator == null )
			return null;
		if ( creator instanceof Parcelable.ClassLoaderCreator<?> )
			return ( ( Parcelable.ClassLoaderCreator<T> ) creator ).readFromParcel( this, loader );
		return creator.readFromParcel( this );
	}

	public final <T extends Parcelable> Parcelable.Serializer<T> getParcelableCreator( String key, ClassLoader loader )
	{
		String name = getString( key ).orElse( null );

		if ( name == null )
			return null;

		Parcelable.Serializer<T> creator;

		synchronized ( mCreators )
		{
			HashMap<String, Parcelable.Serializer> map = mCreators.get( loader );

			if ( map == null )
			{
				map = new HashMap<>();
				mCreators.put( loader, map );
			}

			creator = map.get( name );
			if ( creator == null )
			{
				try
				{
					Class c = loader == null ? Class.forName( name ) : Class.forName( name, true, loader );
					Field f = c.getField( "CREATOR" );
					creator = ( Parcelable.Serializer ) f.get( null );
				}
				catch ( IllegalAccessException e )
				{
					throwExceptionIgnorable( "IllegalAccessException when unmarshalling: " + name );
				}
				catch ( ClassNotFoundException e )
				{
					throwExceptionIgnorable( "ClassNotFoundException when unmarshalling: " + name );
				}
				catch ( ClassCastException | NoSuchFieldException e )
				{
					throwExceptionIgnorable( "Parcelable protocol requires a Parcelable.Creator object called CREATOR on class " + name );
				}
				catch ( NullPointerException e )
				{
					throwExceptionIgnorable( "Parcelable protocol requires the CREATOR object to be static on class " + name );
				}

				if ( creator == null )
					throwExceptionIgnorable( "Parcelable protocol requires a Parcelable.Creator object called CREATOR on class " + name );

				map.put( name, creator );
			}
		}

		return creator;
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
}
