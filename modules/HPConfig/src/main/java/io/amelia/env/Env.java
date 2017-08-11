package io.amelia.env;

import io.amelia.lang.UncaughtException;
import io.amelia.support.Objs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Env
{
	private final Map<String, Object> env = new HashMap<>();
	private final File envFile;

	public Env( File envFile ) throws IOException
	{
		Objs.notNull( envFile );
		this.envFile = envFile;

		synchronized ( env )
		{
			Properties prop = new Properties();
			prop.load( new FileInputStream( envFile ) );

			for ( String key : prop.stringPropertyNames() )
				env.put( key, prop.getProperty( key ) );
		}
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

	public Map<String, Object> map()
	{
		return Collections.unmodifiableMap( env );
	}

	public Env set( String key, Object value, boolean updateEnvFile )
	{
		Objs.notNull( key );
		Objs.notNull( value );
		env.put( key, value );

		if ( updateEnvFile )
		{
			try
			{
				Properties prop = new Properties();
				prop.load( new FileInputStream( envFile ) );
				prop.setProperty( key, Objs.castToString( value ) );
				prop.store( new FileOutputStream( envFile ), "" );
			}
			catch ( IOException e )
			{
				throw new UncaughtException( e );
			}
		}

		return this;
	}
}
