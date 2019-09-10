package com.marchnetworks.management.system;

import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemInfo
{
	private String majorVersion = "0";
	private String minorVersion = "0";
	private String buildVersion = "0";
	private String revisionVersion = "0";
	private String buildDate;
	private String buildServer;
	private static final Logger LOG = Logger.getLogger( SystemInfo.class.getName() );

	private static final String PROP_DEFAULT_VALUE = "";

	private static final String PROP_BUILD_NUM_KEY = "Build-Number";

	private static final String PROP_BUILD_DATE_KEY = "Build-Date";
	private static final String PROP_BUILD_SERVER_KEY = "Build-Server";
	private static final String MANIFEST_FILE = "/META-INF/MANIFEST.MF";

	public SystemInfo()
	{
		java.io.InputStream fileStream = getClass().getClassLoader().getResourceAsStream( "/META-INF/MANIFEST.MF" );
		Properties props = new Properties();
		try
		{
			if ( fileStream != null )
			{
				props.load( fileStream );

				if ( LOG.isLoggable( Level.FINE ) )
				{
					for ( String name : props.stringPropertyNames() )
					{
						LOG.fine( "Prop: " + name + "=" + props.getProperty( name ) );
					}
				}

				String buildNum = ( String ) props.get( "Build-Number" );

				if ( ( buildNum != null ) && ( buildNum.length() > 0 ) )
				{
					StringTokenizer token = new StringTokenizer( buildNum, "." );
					int i = 0;
					while ( token.hasMoreTokens() )
					{
						if ( i == 0 )
						{
							majorVersion = token.nextToken();
						}
						else if ( i == 1 )
						{
							minorVersion = token.nextToken();
						}
						else if ( i == 2 )
						{
							buildVersion = token.nextToken();
						}
						else
							revisionVersion = token.nextToken();
						i++;
					}

					buildDate = props.getProperty( "Build-Date", "" );

					buildServer = props.getProperty( "Build-Server", "" );
				}
				else
				{
					LOG.warning( "No Build-Number prop in manifest file" );
				}
			}
			else
			{
				LOG.log( Level.SEVERE, "Cannot find MANIFEST.MF file" );

				LOG.warning( "Manifest file path=" + getClass().getClassLoader().getResource( "/META-INF/MANIFEST.MF" ) );
			}
		}
		catch ( IOException ioe )
		{
			LOG.log( Level.SEVERE, "Fail to access MANIFEST.MF file located within war file", ioe );
		}
		catch ( Exception e )
		{
			LOG.log( Level.SEVERE, "Error parsing system info", e );
		}
	}

	public String getMajorVersion()
	{
		return majorVersion;
	}

	/**
	 * @deprecated
	 */
	public void setMajorVersion( String majorVersion )
	{
		throw new IllegalStateException();
	}

	public String getMinorVersion()
	{
		return minorVersion;
	}

	/**
	 * @deprecated
	 */
	public void setMinorVersion( String minorVersion )
	{
		throw new IllegalStateException();
	}

	public String getBuildVersion()
	{
		return buildVersion;
	}

	/**
	 * @deprecated
	 */
	public void setBuildVersion( String buildVersion )
	{
		throw new IllegalStateException();
	}

	public String getRevisionVersion()
	{
		return revisionVersion;
	}

	/**
	 * @deprecated
	 */
	public void setRevisionVersion( String revisionVersion )
	{
		throw new IllegalStateException();
	}

	public String getVersion()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( majorVersion ).append( "." ).append( minorVersion ).append( "." ).append( buildVersion ).append( "." ).append( revisionVersion );

		if ( sb.toString().equals( "0.0.0.0" ) )
		{
			return "UNKNOWN";
		}
		return sb.toString();
	}

	/**
	 * @deprecated
	 */
	public void setVersion( String ver )
	{
		throw new IllegalStateException();
	}

	public String getBuildDate()
	{
		return buildDate;
	}

	/**
	 * @deprecated
	 */
	public void setBuildDate( String date )
	{
		throw new IllegalStateException();
	}

	public String getBuildServer()
	{
		return buildServer;
	}

	/**
	 * @deprecated
	 */
	public void setBuildServer( String buildServer )
	{
		throw new IllegalStateException();
	}
}
