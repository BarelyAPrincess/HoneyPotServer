package io.amelia.foundation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import io.amelia.lang.ApplicationException;
import io.amelia.support.IO;

public class PropDevMeta implements ImplDevMeta
{
	private Properties prop = new Properties();

	public PropDevMeta() throws ApplicationException
	{
		this( "build.properties" );
	}

	public PropDevMeta( File propFile ) throws FileNotFoundException, ApplicationException
	{
		this( new FileInputStream( propFile ) );
	}

	public PropDevMeta( String fileName ) throws ApplicationException
	{
		this( App.class, fileName );
	}

	public PropDevMeta( Class<?> cls, String fileName ) throws ApplicationException
	{
		this( cls.getClassLoader().getResourceAsStream( fileName ) );
	}

	public PropDevMeta( InputStream is ) throws ApplicationException
	{
		try
		{
			prop.load( is );
		}
		catch ( IOException e )
		{
			throw ApplicationException.fatal( e );
		}
		finally
		{
			IO.closeQuietly( is );
		}
	}

	public String getProperty( String key )
	{
		return prop.getProperty( key );
	}
}
