package com.marchnetworks.shared.config;

import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.common.Base64;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.config.AppConfig;
import com.marchnetworks.common.config.AppConfigImpl;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.utils.ServerUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CommonConfiguration implements InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( CommonConfiguration.class );

	private static String SERVER_DEV_MODE = "server_dev_mode";

	private static String SERVER_HTTP_PORT = "http_port";

	private static String SERVER_HTTP_HOST = "http_host";

	private static final int SERVER_DEFAULT_HTTP_PORT = 80;

	private AppConfig configuration;
	private int serverPort;
	private String serverHost;

	public void onAppInitialized()
	{
		logInfo();
	}

	public int getServerPort()
	{
		if ( serverPort == 0 )
		{
			String serverPortProperty = getProperty( SERVER_HTTP_PORT );

			if ( serverPortProperty != null )
			{
				LOG.info( "serverPort (from config file) = {}", serverPortProperty );
				serverPort = Integer.valueOf( serverPortProperty ).intValue();
			}
			else
			{
				serverPort = 80;
			}
		}

		return serverPort;
	}

	public void setServerPort( int serverPort )
	{
		this.serverPort = serverPort;
	}

	public String getServerHost()
	{
		if ( serverHost == null )
		{
			serverHost = getProperty( SERVER_HTTP_HOST );

			if ( serverHost != null )
			{
				LOG.info( "serverHost (from config file) = {}", serverHost );
			}
			else
			{
				try
				{
					serverHost = InetAddress.getLocalHost().getHostAddress();
				}
				catch ( UnknownHostException e )
				{
					LOG.error( "Can not obtain server address." );
					throw new RuntimeException( e );
				}
			}
		}
		return serverHost;
	}

	public String getProperty( ConfigProperty a_Property )
	{
		return getProperty( a_Property, null );
	}

	public String getProperty( ConfigProperty a_Property, String a_Default )
	{
		return getProperty( a_Property.getXmlName(), a_Default );
	}

	public int getIntProperty( ConfigProperty a_Property, int a_Default )
	{
		return getIntProperty( a_Property.getXmlName(), a_Default );
	}

	public boolean getBooleanProperty( ConfigProperty a_Property )
	{
		return getBooleanProperty( a_Property, false );
	}

	public boolean getBooleanProperty( ConfigProperty a_Property, boolean a_Default )
	{
		String value = getConfiguration().getProperty( a_Property );
		if ( value == null )
		{
			return a_Default;
		}
		return !value.equals( "false" );
	}

	public String getProperty( String a_Name )
	{
		return getProperty( a_Name, null );
	}

	public List<String> getPropertyList( ConfigProperty a_Property )
	{
		return getPropertyList( a_Property.getXmlName() );
	}

	public List<String> getPropertyList( String propertyName )
	{
		List<String> stringPropList = new ArrayList();

		String rawValue = getConfiguration().getProperty( propertyName );
		if ( rawValue != null )
		{
			String[] values = rawValue.split( "," );
			for ( String string : values )
			{
				stringPropList.add( string );
			}
		}
		return stringPropList;
	}

	public String getProperty( String a_Name, String a_Default )
	{
		String value = getConfiguration().getProperty( a_Name );
		return value == null ? a_Default : value;
	}

	public int getIntProperty( String a_Name, int a_Default )
	{
		String value = null;
		try
		{
			value = getConfiguration().getProperty( a_Name );

			return value != null ? Integer.parseInt( value ) : a_Default;
		}
		catch ( NumberFormatException nfe )
		{
			LOG.debug( "Exception trying to get property for name=" + a_Name + " value=" + value, nfe );
		}
		return a_Default;
	}

	public void setProperty( ConfigProperty a_Property, String a_Value )
	{
		getConfiguration().setProperty( a_Property, a_Value );
	}

	public void setProperty( ConfigProperty a_Property, List<String> a_Value )
	{
		String listAsString = CollectionUtils.collectionToString( a_Value, "," );
		getConfiguration().setProperty( a_Property, listAsString );
	}

	public boolean isServerDevMode()
	{
		String serverMode = getConfiguration().getProperty( SERVER_DEV_MODE );
		return serverMode != null ? Boolean.parseBoolean( serverMode ) : false;
	}

	public AppConfig getConfiguration()
	{
		if ( configuration == null )
		{
			configuration = AppConfigImpl.getInstance();
		}
		return configuration;
	}

	public String getLdapPasswordFromConfig()
	{
		String encryptedPassword = getProperty( ConfigProperty.LDAP_PASSWORD );
		if ( ( encryptedPassword != null ) && ( encryptedPassword != "$ldap_password" ) )
		{
			return decryptPropertyValue( encryptedPassword );
		}
		return null;
	}

	public String getAdminPasswordFromConfig()
	{
		String encryptedPassword = getProperty( ConfigProperty.ADMIN_PASSWORD );
		if ( ( encryptedPassword != null ) && ( encryptedPassword != "$local_admin_password" ) )
		{
			return decryptPropertyValue( encryptedPassword );
		}
		return null;
	}

	private String decryptPropertyValue( String encryptedValue )
	{
		String value = null;
		if ( encryptedValue != null )
		{
			try
			{
				String keySecret = "marchnetworks613";
				SecretKey key = new SecretKeySpec( keySecret.getBytes(), "AES" );
				Cipher cipher = Cipher.getInstance( "AES/ECB/PKCS5Padding", "SunJCE" );

				byte[] encrypted = Base64.decode( encryptedValue );
				cipher.init( 2, key );
				byte[] decrypted = cipher.doFinal( encrypted );
				value = CommonAppUtils.encodeToUTF8String( decrypted );
			}
			catch ( NoSuchAlgorithmException e )
			{
				LOG.warn( e.getMessage() );
			}
			catch ( InvalidKeyException e )
			{
				LOG.warn( e.getMessage() );
			}
			catch ( NoSuchPaddingException e )
			{
				LOG.warn( e.getMessage() );
			}
			catch ( IllegalBlockSizeException e )
			{
				LOG.warn( e.getMessage() );
			}
			catch ( BadPaddingException e )
			{
				LOG.warn( e.getMessage() );
			}
			catch ( IOException e )
			{
				LOG.warn( e.getMessage() );
			}
			catch ( NoSuchProviderException e )
			{
				LOG.warn( e.getMessage() );
			}
		}

		return value;
	}

	public String getSmtpPasswordFromConfig()
	{
		String encryptedPassword = getProperty( ConfigProperty.SMTP_PASSWORD );
		if ( encryptedPassword != null )
		{
			return decryptPropertyValue( encryptedPassword );
		}
		return null;
	}

	private void logInfo()
	{
		Logger INFO_LOG = LoggerFactory.getLogger( "serverinfo" );

		String ldapServer = "\nLDAP Primary: " + getProperty( ConfigProperty.LDAP_SERVER );

		INFO_LOG.info( "\nPackage Version: " + getProperty( ConfigProperty.PACKAGE_NUMBER ) + "\nServer Version: " + ServerUtils.getServerVersion() + "\nDatabase Type: " + getProperty( ConfigProperty.DB_STATE ) + "\nAdmin Type: " + getProperty( ConfigProperty.ADMIN_TYPE ) + "\nLDAP: " + checkBooleanProperty( getProperty( ConfigProperty.LDAP_ENABLED ) ) + "\nLDAP Only: " + checkBooleanProperty( getProperty( ConfigProperty.LDAP_ONLY ) ) + ldapServer + "\nLDAP Search Directory Root: " + getProperty( ConfigProperty.LDAP_SEARCH_DIRECTORY_ROOT ) + "\nLDAP Method: " + getProperty( ConfigProperty.LDAP_METHOD ) + "\nLDAP Discovery: " + checkBooleanProperty( getProperty( ConfigProperty.LDAP_DISCOVERY_ENABLED ) ) + "\nLDAP SSL: " + checkBooleanProperty( getProperty( ConfigProperty.LDAP_SSL ) ) + "\n" );
	}

	private String checkBooleanProperty( String booleanProperty )
	{
		return ( booleanProperty != null ) && ( booleanProperty.equals( "true" ) ) ? "Enabled" : "Disabled";
	}
}

