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

	public PropDevMeta() throws ApplicationException.Error
	{
		this( "build.properties" );
	}

	public PropDevMeta( File propFile ) throws FileNotFoundException, ApplicationException.Error
	{
		this( new FileInputStream( propFile ) );
	}

	public PropDevMeta( String fileName ) throws ApplicationException.Error
	{
		this( Kernel.class, fileName );
	}

	public PropDevMeta( Class<?> cls, String fileName ) throws ApplicationException.Error
	{
		this( cls.getClassLoader().getResourceAsStream( fileName ) );
	}

	public PropDevMeta( InputStream is ) throws ApplicationException.Error
	{
		try
		{
			prop.load( is );
		}
		catch ( IOException e )
		{
			throw ApplicationException.error( e );
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
