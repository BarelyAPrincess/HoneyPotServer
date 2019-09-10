package com.marchnetworks.management.initialization;

import com.marchnetworks.common.certification.CertificationCreator;
import com.marchnetworks.common.utils.XmlUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class Main
{
	static
	{
		if ( Security.getProperty( "BC" ) == null )
			Security.addProvider( new BouncyCastleProvider() );
	}

	private static org.slf4j.Logger LOG = LoggerFactory.getLogger( Main.class );

	private static final Class[] parameters = {URL.class};
	private static final String LOGFILE = "InitServer.log";
	private static final String CONFIG_DIR = "config";
	private static final String SQLJAR = "lib//ext//jtds-1.2.8.jar";

	public static void main( String[] args )
	{
		String domainPath = setupDomainPathLogging( args );
		if ( domainPath.equals( "" ) )
		{
			return;
		}
		try
		{
			addFile( domainPath + "lib//ext//jtds-1.2.8.jar" );
		}
		catch ( IOException localIOException )
		{
		}

		databaseInit( domainPath );

		String configPath = domainPath + "config";
		File f = new File( configPath );
		if ( !f.exists() )
		{
			LOG.error( "Config path not found: " + configPath );
			LOG.error( "---> Skipping certificate checking" );
		}
		else
		{
			CertificationCreator cc = new CertificationCreator();
			try
			{
				cc.AssureCertificates( domainPath + "config" );
			}
			catch ( Exception e )
			{
				LOG.error( "Error during AssureCertificates: ", e );
			}
		}

		LOG.info( "...InitServer Done" );
	}

	private static String setupDomainPathLogging( String[] args )
	{
		LogManager lm = LogManager.getLogManager();
		java.util.logging.Logger l = lm.getLogger( "" );

		Handler[] handlers = l.getHandlers();
		if ( handlers.length == 1 )
		{
			Handler h = handlers[0];
			h.setFormatter( new InitLogFormatter() );
		}

		if ( args.length < 1 )
		{
			LOG.error( "No domain path specified, exiting" );
			return "";
		}
		String domainPath = args[0];

		File f = new File( domainPath );
		if ( !f.exists() )
		{
			LOG.error( "Domain path not found: " + domainPath );
			return "";
		}

		if ( !domainPath.endsWith( File.separator ) )
			domainPath = domainPath + File.separator;

		String logPath = domainPath + "logs";
		f = new File( logPath );
		if ( !f.exists() )
		{
			LOG.error( "Log path not found: " + logPath );
		}
		else
		{
			try
			{
				Handler fh = new FileHandler( logPath + File.separator + "InitServer.log", true );
				fh.setFormatter( new InitLogFormatter() );
				l.addHandler( fh );
			}
			catch ( Exception e )
			{
				LOG.error( "Couldn't setup logging to " + logPath + File.separator + "InitServer.log", e );
			}
		}

		return domainPath;
	}

	private static boolean databaseInit( String domainPath )
	{
		String dbUrl = null;
		String port = null;
		String username = null;
		String password = null;
		// String pipe = null;

		Document theDocument = XmlUtils.getDocumentFromFile( domainPath + "config" + File.separator + "domain.xml" );

		NodeList nodes = theDocument.getElementsByTagName( "jdbc-connection-pool" );

		for ( int i = 0; i < nodes.getLength(); i++ )
		{
			Element aNode = ( Element ) nodes.item( i );
			if ( "CommandDS".equals( aNode.getAttribute( "name" ) ) )
			{
				NodeList propertyList = aNode.getElementsByTagName( "property" );
				for ( int j = 0; j < propertyList.getLength(); j++ )
				{
					Element aPropertyNode = ( Element ) propertyList.item( j );
					if ( "PortNumber".equals( aPropertyNode.getAttribute( "name" ) ) )
					{
						port = aPropertyNode.getAttribute( "value" );
					}
					else if ( "serverName".equals( aPropertyNode.getAttribute( "name" ) ) )
					{
						dbUrl = aPropertyNode.getAttribute( "value" );
					}
					else if ( "User".equals( aPropertyNode.getAttribute( "name" ) ) )
					{
						username = aPropertyNode.getAttribute( "value" );
					}
					else if ( "Password".equals( aPropertyNode.getAttribute( "name" ) ) )
					{
						password = aPropertyNode.getAttribute( "value" );
					}
					/*else if ( "namedPipe".equals( aPropertyNode.getAttribute( "name" ) ) )
					{
						pipe = aPropertyNode.getAttribute( "value" );
					}*/
				}
				break;
			}
		}

		if ( ( dbUrl == null ) || ( port == null ) || ( username == null ) || ( password == null ) )
		{
			LOG.error( "Couldn't parse domain.xml for database attributes." );
			return false;
		}

		try
		{
			// Class.forName( "net.sourceforge.jtds.jdbc.Driver" );
			Class.forName( "com.mysql.jdbc.Driver" );

			String createCommandDBQuery = "CREATE DATABASE command;";
			String createAppDBQuery = "CREATE DATABASE apps;";

			String url = "jdbc:mysql://" + dbUrl + "/?user=" + username + "&password=" + password;
			// String url = "jdbc:jtds:sqlserver://" + dbUrl + ";instance=command;namedPipe=" + pipe;
			/*if ( !"false".equals( pipe ) )
			{
				String dbPath = domainPath.substring( 0, domainPath.lastIndexOf( "glassfish\\domains\\command" ) ) + "databases";
				String enableAccount = "USE [master] ALTER LOGIN [sa] ENABLE; USE [master] ALTER LOGIN [sa] WITH PASSWORD = 'G0i2h$Z}UtSQ3Dg%'";
				createCommandDBQuery = "CREATE DATABASE [command] ON PRIMARY\n ( NAME = N'command', FILENAME = N'" + dbPath + "\\command.mdf' , SIZE = 4096KB , MAXSIZE = UNLIMITED, FILEGROWTH = 1024KB ) \nLOG ON ( NAME = N'command_log', FILENAME = N'" + dbPath + "\\command_log.ldf' , SIZE = 1024KB , MAXSIZE = 2048GB , FILEGROWTH = 10%);";
				createAppDBQuery = "CREATE DATABASE [apps] ON PRIMARY\n ( NAME = N'apps', FILENAME = N'" + dbPath + "\\apps.mdf' , SIZE = 4096KB , MAXSIZE = UNLIMITED, FILEGROWTH = 1024KB ) \nLOG ON ( NAME = N'apps_log', FILENAME = N'" + dbPath + "\\apps_log.ldf' , SIZE = 1024KB , MAXSIZE = 2048GB , FILEGROWTH = 10%);";

				DriverManager.setLoginTimeout( 2 );
				Connection conn = DriverManager.getConnection( url );
				Statement stmt = conn.createStatement();
				try
				{
					stmt.execute( enableAccount );
					stmt.execute( createCommandDBQuery );
					stmt.execute( createAppDBQuery );
				}
				catch ( Exception e )
				{
					LOG.info( e.toString() );
				}
				conn.close();
			}
			else
			{*/
			LOG.info( createCommandDBQuery );
			LOG.info( url );
			DriverManager.setLoginTimeout( 2 );
			Connection conn = DriverManager.getConnection( url );//, username, password );

			Statement stmt = conn.createStatement();
			try
			{
				stmt.execute( createCommandDBQuery );
			}
			catch ( Exception e )
			{
				LOG.info( e.toString() );
			}

			try
			{
				stmt.execute( createAppDBQuery );
			}
			catch ( Exception e )
			{
				LOG.info( e.toString() );
			}

			conn.close();
			//}

			LOG.info( "...Database Init Done" );
			return true;
		}
		catch ( Exception e )
		{
			LOG.error( "Couldn't create DBs due to exception {}", e );
		}

		return false;
	}

	public static void addFile( String s ) throws IOException
	{
		File f = new File( s );
		addFile( f );
	}

	public static void addFile( File f ) throws IOException
	{
		addURL( f.toURI().toURL() );
	}

	public static void addURL( URL u ) throws IOException
	{
		URLClassLoader sysloader = ( URLClassLoader ) ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> sysclass = URLClassLoader.class;
		try
		{
			Method method = sysclass.getDeclaredMethod( "addURL", parameters );
			method.setAccessible( true );
			method.invoke( sysloader, new Object[] {u} );
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
			throw new IOException( "Error, could not add URL to system classloader" );
		}
	}
}
