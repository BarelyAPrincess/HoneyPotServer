package io.amelia.foundation;

import io.amelia.lang.ConfigException;
import io.amelia.support.data.ParcelLoader;
import io.amelia.support.data.StackerWithValue;
import io.amelia.support.data.ValueTypesTrait;

public final class ConfigMap extends StackerWithValue<ConfigMap, Object> implements ValueTypesTrait
{
	private String loadedValueHash = null;

	protected ConfigMap()
	{
		super( ConfigMap::new, "" );
	}

	protected ConfigMap( String key )
	{
		super( ConfigMap::new, key );
	}

	protected ConfigMap( ConfigMap parent, String key )
	{
		super( ConfigMap::new, parent, key );
	}

	protected ConfigMap( ConfigMap parent, String key, Object value )
	{
		super( ConfigMap::new, parent, key, value );
	}

	void loadNewValue( String key, Object obj )
	{
		getChildOrCreate( key ).loadNewValue( obj );
	}

	void loadNewValue( Object obj )
	{
		disposeCheck();
		// A loaded value is only set if the current value is null, was never set, or the new value hash doesn't match the loaded one.
		if ( loadedValueHash == null || value == null || !ParcelLoader.hashObject( obj ).equals( loadedValueHash ) )
		{
			loadedValueHash = ParcelLoader.hashObject( obj );
			updateValue( obj );
		}
	}

	@Override
	public void throwExceptionError( String message ) throws ConfigException.Error
	{
		throw new ConfigException.Error( this, message );
	}

	@Override
	public void throwExceptionIgnorable( String message ) throws ConfigException.Ignorable
	{
		throw new ConfigException.Ignorable( this, message );
	}
}
