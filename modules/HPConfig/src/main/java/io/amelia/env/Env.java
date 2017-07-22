package io.amelia.env;

import io.amelia.support.Objs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Env
{
	private final Map<String, Object> env = new HashMap<>();

	public Env defs( Map<String, Object> defs )
	{
		Objs.notNull( defs );
		env.putAll( defs );
		return this;
	}

	public boolean getBoolean( String key )
	{
		return Objs.isTrue( getObject( key ) );
	}

	public Object getObject( String key )
	{
		return env.get( key );
	}

	public String getString( String key )
	{
		return Objs.castToStringWithException( getObject( key ) );
	}

	public boolean isValueSet( String key )
	{
		return env.containsKey( key ) && !Objs.isNull( env.get( key ) );
	}

	public Env load( InputStream is ) throws IOException
	{
		load( null, is );
		return this;
	}

	public Env load( Map<String, Object> defs, InputStream is ) throws IOException
	{
		Objs.notNull( is );
		synchronized ( env )
		{
			Properties prop = new Properties();
			prop.load( is );

			if ( defs != null )
				env.putAll( defs );

			for ( String key : prop.stringPropertyNames() )
				env.put( key, prop.getProperty( key ) );
		}
		return this;
	}

	public Map<String, Object> map()
	{
		return Collections.unmodifiableMap( env );
	}

	public Env set( String key, Object value )
	{
		Objs.notNull( key );
		Objs.notNull( value );
		synchronized ( env )
		{
			env.put( key, value );
		}
		return this;
	}
}
