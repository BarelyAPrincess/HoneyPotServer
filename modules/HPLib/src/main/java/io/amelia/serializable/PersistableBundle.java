package io.amelia.serializable;

import com.sun.istack.internal.Nullable;
import io.amelia.support.ArrayMap;
import io.amelia.support.Maps;

import java.io.IOException;
import java.util.HashMap;

/**
 * A mapping from String keys to values of various types. The set of types
 * supported by this class is purposefully restricted to simple objects that can
 * safely be persisted to and restored from disk.
 *
 * @see BaseBundle
 */
public final class PersistableBundle extends BaseBundle implements Cloneable, Parcelable, XmlUtils.WriteMapCallback
{
	public static final PersistableBundle EMPTY;
	public static final Parcelable.Creator<PersistableBundle> CREATOR = new Parcelable.Creator<PersistableBundle>()
	{
		@Override
		public PersistableBundle createFromParcel( Parcel in )
		{
			return in.readPersistableBundle();
		}

		@Override
		public PersistableBundle[] newArray( int size )
		{
			return new PersistableBundle[size];
		}
	};
	private static final String TAG_PERSISTABLEMAP = "pbundle_as_map";

	static
	{
		EMPTY = new PersistableBundle();
		EMPTY.mMap = new HashMap<>();
	}

	/**
	 * Make a PersistableBundle for a single key/value pair.
	 *
	 * @hide
	 */
	public static PersistableBundle forPair( String key, String value )
	{
		PersistableBundle b = new PersistableBundle( 1 );
		b.putString( key, value );
		return b;
	}

	/**
	 * @hide
	 */
	public static boolean isValidType( Object value )
	{
		return ( value instanceof Integer ) || ( value instanceof Long ) || ( value instanceof Double ) || ( value instanceof String ) || ( value instanceof int[] ) || ( value instanceof long[] ) || ( value instanceof double[] ) || ( value instanceof String[] ) || ( value instanceof PersistableBundle ) || ( value == null ) || ( value instanceof Boolean ) || ( value instanceof boolean[] );
	}

	/**
	 * @hide
	 */
	public static PersistableBundle restoreFromXml( XmlPullParser in ) throws IOException, XmlPullParserException
	{
		final int outerDepth = in.getDepth();
		final String startTag = in.getName();
		final String[] tagName = new String[1];
		int event;
		while ( ( ( event = in.next() ) != XmlPullParser.END_DOCUMENT ) && ( event != XmlPullParser.END_TAG || in.getDepth() < outerDepth ) )
		{
			if ( event == XmlPullParser.START_TAG )
			{
				return new PersistableBundle( ( ArrayMap<String, Object> ) XmlUtils.readThisArrayMapXml( in, startTag, tagName, new MyReadMapCallback() ) );
			}
		}
		return EMPTY;
	}


	/**
	 * Constructs a new, empty PersistableBundle.
	 */
	public PersistableBundle()
	{
		super();
		mFlags = FLAG_DEFUSABLE;
	}

	/**
	 * Constructs a new, empty PersistableBundle sized to hold the given number of
	 * elements. The PersistableBundle will grow as needed.
	 *
	 * @param capacity the initial capacity of the PersistableBundle
	 */
	public PersistableBundle( int capacity )
	{
		super( capacity );
		mFlags = FLAG_DEFUSABLE;
	}

	/**
	 * Constructs a PersistableBundle containing a copy of the mappings from the given
	 * PersistableBundle.
	 *
	 * @param b a PersistableBundle to be copied.
	 */
	public PersistableBundle( PersistableBundle b )
	{
		super( b );
		mFlags = b.mFlags;
	}

	/**
	 * Constructs a PersistableBundle from a Bundle.
	 *
	 * @param b a Bundle to be copied.
	 * @throws IllegalArgumentException if any element of {@code b} cannot be persisted.
	 * @hide
	 */
	public PersistableBundle( BaseBundle b )
	{
		this( b.getMap() );
	}

	/**
	 * Constructs a PersistableBundle containing the mappings passed in.
	 *
	 * @param map a Map containing only those items that can be persisted.
	 * @throws IllegalArgumentException if any element of #map cannot be persisted.
	 */
	private PersistableBundle( ArrayMap<String, Object> map )
	{
		super();
		mFlags = FLAG_DEFUSABLE;

		// First stuff everything in.
		putAll( map );

		// Now verify each item throwing an exception if there is a violation.
		final int N = mMap.size();
		for ( int i = 0; i < N; i++ )
		{
			Object value = Maps.mapIndex( mMap, i );
			if ( value instanceof HashMap )
			{
				// Fix up any Maps by replacing them with PersistableBundles.
				mMap.setValueAt( i, new PersistableBundle( ( HashMap<String, Object> ) value ) );
			}
			else if ( value instanceof BaseBundle )
			{
				mMap.setValueAt( i, new PersistableBundle( ( ( BaseBundle ) value ) ) );
			}
			else if ( !isValidType( value ) )
			{
				throw new IllegalArgumentException( "Bad value in PersistableBundle key=" + mMap.keyAt( i ) + " value=" + value );
			}
		}
	}

	/* package */ PersistableBundle( Parcel parcelledData, int length )
	{
		super( parcelledData, length );
		mFlags = FLAG_DEFUSABLE;
	}

	/**
	 * Clones the current PersistableBundle. The internal map is cloned, but the keys and
	 * values to which it refers are copied by reference.
	 */
	@Override
	public Object clone()
	{
		return new PersistableBundle( this );
	}

	@Override
	synchronized public String toString()
	{
		if ( mParcelledData != null )
		{
			if ( isEmptyParcel() )
			{
				return "PersistableBundle[EMPTY_PARCEL]";
			}
			else
			{
				return "PersistableBundle[mParcelledData.dataSize=" + mParcelledData.dataSize() + "]";
			}
		}
		return "PersistableBundle[" + mMap.toString() + "]";
	}

	/**
	 * Report the nature of this Parcelable's contents
	 */
	@Override
	public int describeContents()
	{
		return 0;
	}

	/**
	 * Writes the PersistableBundle contents to a Parcel, typically in order for
	 * it to be passed through an IBinder connection.
	 *
	 * @param parcel The parcel to copy this bundle to.
	 */
	@Override
	public void writeToParcel( Parcel parcel, int flags )
	{
		final boolean oldAllowFds = parcel.pushAllowFds( false );
		try
		{
			writeToParcelInner( parcel, flags );
		}
		finally
		{
			parcel.restoreAllowFds( oldAllowFds );
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a Bundle value, or null
	 */
	@Nullable
	public PersistableBundle getPersistableBundle( @Nullable String key )
	{
		unparcel();
		Object o = mMap.get( key );
		if ( o == null )
		{
			return null;
		}
		try
		{
			return ( PersistableBundle ) o;
		}
		catch ( ClassCastException e )
		{
			typeWarning( key, o, "Bundle", e );
			return null;
		}
	}

	/**
	 * Inserts a PersistableBundle value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key   a String, or null
	 * @param value a Bundle object, or null
	 */
	public void putPersistableBundle( @Nullable String key, @Nullable PersistableBundle value )
	{
		unparcel();
		mMap.put( key, value );
	}

	/**
	 * @hide
	 */
	public void saveToXml( XmlSerializer out ) throws IOException, XmlPullParserException
	{
		unparcel();
		XmlUtils.writeMapXml( mMap, out, this );
	}

	/**
	 * @hide
	 */
	@Override
	public void writeUnknownObject( Object v, String name, XmlSerializer out ) throws XmlPullParserException, IOException
	{
		if ( v instanceof PersistableBundle )
		{
			out.startTag( null, TAG_PERSISTABLEMAP );
			out.attribute( null, "name", name );
			( ( PersistableBundle ) v ).saveToXml( out );
			out.endTag( null, TAG_PERSISTABLEMAP );
		}
		else
		{
			throw new XmlPullParserException( "Unknown Object o=" + v );
		}
	}

	/**
	 * @hide
	 */
	static class MyReadMapCallback implements XmlUtils.ReadMapCallback
	{
		@Override
		public Object readThisUnknownObjectXml( XmlPullParser in, String tag ) throws XmlPullParserException, IOException
		{
			if ( TAG_PERSISTABLEMAP.equals( tag ) )
			{
				return restoreFromXml( in );
			}
			throw new XmlPullParserException( "Unknown tag=" + tag );
		}
	}
}
