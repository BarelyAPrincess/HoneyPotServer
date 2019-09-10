package com.marchnetworks.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class AppConfigImpl implements AppConfig
{
	public static final String DEFAULT_CONFIG_NAME = "march.server.config.xml";
	private static final String CONFIG_XML_COMMENTS = "Command Enterprise Config";
	private static final String CONFIG_XML_ENCODING = "UTF-8";
	private static final Logger LOG = LoggerFactory.getLogger( AppConfigImpl.class );

	private static AppConfigImpl s_Instance = null;
	private String m_FileName = "march.server.config.xml";
	private HashMap<String, String> m_Properties = new HashMap<>();

	public static AppConfig getInstance()
	{
		if ( s_Instance == null )
			s_Instance = new AppConfigImpl();

		return s_Instance;
	}

	public AppConfigImpl( String filepath ) throws IOException
	{
		m_FileName = filepath;
		load();
	}

	private AppConfigImpl()
	{
		load();
	}

	public String getProperty( ConfigProperty a_Property )
	{
		return getProperty( a_Property.getXmlName() );
	}

	public String getProperty( String a_Name )
	{
		return ( String ) m_Properties.get( a_Name );
	}

	public String setProperty( ConfigProperty a_Property, String a_Value )
	{
		return setProperty( a_Property.getXmlName(), a_Value );
	}

	public String setProperty( String a_Name, String a_Value )
	{
		String oldValue = ( String ) m_Properties.put( a_Name, a_Value );
		LOG.trace( "setProperty: Overriding {} default value of '{}' to override with '{}' old override was '{}'", new Object[] {a_Name, m_Properties.get( a_Name ), a_Value, oldValue} );
		save();
		return oldValue;
	}

	protected InputStream getConfigData() throws FileNotFoundException
	{
		File file = getConfigFile();

		LOG.trace( "file path={}", file.getAbsolutePath() );

		return new FileInputStream( file );
	}

	protected File getConfigFile() throws FileNotFoundException
	{
		LOG.trace( "config file name={}", m_FileName );
		File file = new File( m_FileName );

		if ( !file.exists() )
			throw new FileNotFoundException( "Missing configuration file at: " + file.getAbsolutePath() );

		return file;
	}

	private void load()
	{
		try
		{
			InputStream is = getConfigData();
			Throwable localThrowable2 = null;
			Properties props;

			try
			{
				props = new Properties();
				props.loadFromXML( is );

				for ( String name : props.stringPropertyNames() )
				{
					String value = props.getProperty( name );
					m_Properties.put( name, value );
				}
			}
			catch ( Throwable localThrowable1 )
			{
				localThrowable2 = localThrowable1;
				throw localThrowable1;

			}
			finally
			{

				if ( is != null )
					if ( localThrowable2 != null )
						try
						{
							is.close();
						}
						catch ( Throwable x2 )
						{
							localThrowable2.addSuppressed( x2 );
						}
					else
						is.close();
			}
		}
		catch ( IOException ioex )
		{
			LOG.error( "Exception in AppConfigImpl.load():", ioex );
		}
	}

	private void save()
	{
		Properties props = new Properties();
		Throwable localThrowable2 = null;
		Map.Entry<String, String> entry = null;

		for ( Iterator i$ = m_Properties.entrySet().iterator(); i$.hasNext(); )
		{
			entry = ( Map.Entry ) i$.next();
			props.setProperty( ( String ) entry.getKey(), ( String ) entry.getValue() );
		}
		try
		{
			FileOutputStream fos = new FileOutputStream( m_FileName );

			try
			{
				props.storeToXML( fos, "Command Enterprise Config", "UTF-8" );
			}
			catch ( Throwable localThrowable1 )
			{
				localThrowable2 = localThrowable1;
				throw localThrowable1;
			}
			finally
			{
				if ( fos != null )
					if ( localThrowable2 != null )
						try
						{
							fos.close();
						}
						catch ( Throwable x2 )
						{
							localThrowable2.addSuppressed( x2 );
						}
					else
						fos.close();
			}
		}
		catch ( IOException e )
		{
			LOG.error( "Exception in AppConfigImpl.save():", e );
		}
	}
}
