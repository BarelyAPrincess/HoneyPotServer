package com.marchnetworks.common.certification;

import com.marchnetworks.common.config.AppConfig;
import com.marchnetworks.common.config.AppConfigImpl;
import com.marchnetworks.common.config.ConfigProperty;

import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.GeneralName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.security.auth.x500.X500PrivateCredential;

public class CertificationReader
{
	private static final Logger LOG = LoggerFactory.getLogger( CertificationReader.class );

	public static final String MARCHNETWORKS_ROOT_ALIAS = "marchnetworks";

	public static final String COMMAND_ALIAS = "command";

	public static final String SERVER_ALIAS = "server";

	public static final String LICENSE_ALIAS = "marchnetworks_license";

	public static final String MARCHNETWORKS_ROOT_KEY_ALIAS = "marchnetworks_privatekey";
	public static final String COMMAND_KEY_ALIAS = "command_privatekey";
	public static final String SERVER_KEY_ALIAS = "server_privatekey";
	private static final String DEFAULT_STORE_KEY = "user.dir";
	private static final String DEFAULT_KEY_STORE_NAME = "keystore.jks";
	private static final String DEFAULT_TRUST_STORE_NAME = "cacerts.jks";
	protected X500PrivateCredential privateCredentialRoot = null;
	protected X500PrivateCredential privateCredentialCommand = null;
	protected X500PrivateCredential privateCredentialServer = null;

	public static final char[] TRUST_STORE_PASSWORD = "B1ackD1amonds".toCharArray();
	public static final char[] KEY_STORE_PASSWORD = "B1ackD1amonds".toCharArray();

	protected String keyStorePath = null;
	protected String keyStoreName = "keystore.jks";
	protected String trustStoreName = "cacerts.jks";

	protected KeyStore keystore = null;
	protected KeyStore truststore = null;

	protected String hostName = null;
	protected List<InetAddress> inetAddresses = null;
	protected List<String> extraHostNames = new ArrayList();
	protected List<byte[]> extraIPs = new ArrayList();

	private byte[] rootCertCache = null;

	public CertificationReader()
	{
		readNetworkList();
	}

	public void LoadDefaults() throws Exception
	{
		clear();
		load();
	}

	public void LoadCustom( String Path, String KeyStoreName, String TrustStoreName ) throws Exception
	{
		clear();
		keyStorePath = Path;
		keyStoreName = KeyStoreName;
		trustStoreName = TrustStoreName;
		load();
	}

	protected void readNetworkList()
	{
		LOG.debug( "readNetworkList" );
		inetAddresses = new ArrayList();

		try
		{
			hostName = InetAddress.getLocalHost().getHostName();

			Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
			while ( nics.hasMoreElements() )
			{
				NetworkInterface nic = ( NetworkInterface ) nics.nextElement();

				Enumeration<InetAddress> addresses = nic.getInetAddresses();
				while ( addresses.hasMoreElements() )
				{
					InetAddress ia = ( InetAddress ) addresses.nextElement();

					if ( !ia.isLoopbackAddress() )
					{

						String ipaddress = ia.getHostAddress();
						String[] tempAddress = ipaddress.split( ":", 10 );
						if ( tempAddress.length <= 2 )
						{

							inetAddresses.add( ia );
						}
					}
				}
			}
		}
		catch ( Exception e )
		{
			LOG.error( "Error initializing network list", e );
		}
	}

	protected void readExtraEntries() throws IOException
	{
		extraHostNames.clear();
		extraIPs.clear();

		String s = keyStorePath + File.separator + "march.server.config.xml";
		LOG.debug( "Reading extra certificate entries from: " + s );
		AppConfig ac = new AppConfigImpl( s );

		String sHostnameProp = ac.getProperty( ConfigProperty.CERT_ALL_HOSTNAMES );
		if ( sHostnameProp == null )
			sHostnameProp = ac.getProperty( "cert_extra_hostname" );

		String sIPProp = ac.getProperty( ConfigProperty.CERT_ALL_IPS );
		if ( sIPProp == null )
			sIPProp = ac.getProperty( "cert_extra_ip" );

		String[] sHostnames = new String[0];
		String[] sIPs = new String[0];
		if ( sHostnameProp != null )
			sHostnames = sHostnameProp.split( "," );

		if ( sIPProp != null )
			sIPs = sIPProp.split( "," );

		for ( String hn : sHostnames )
		{
			if ( ( hn != null ) && ( !hn.equals( "" ) ) )
			{
				int index = hn.lastIndexOf( ":" );
				if ( index > 0 )
					hn = hn.substring( 0, index ).trim();
				if ( !hostName.equalsIgnoreCase( hn ) )
					extraHostNames.add( hn );
			}
		}

		for ( String ip : sIPs )
		{
			int index = ip.lastIndexOf( ":" );
			if ( index > 0 )
			{
				ip = ip.substring( 0, index );
			}
			if ( !isLocalIpAddress( ip ) )
			{

				byte[] bIP = Utils.ipString2Octets( ip.trim() );

				if ( bIP != null )
				{
					extraIPs.add( bIP );
				}
				else
				{
					LOG.warn( "Bad IP address found in config file: " + ip );
				}
			}
		}
	}

	protected void clear()
	{
		extraHostNames.clear();
		extraIPs.clear();
		keyStorePath = System.getProperty( "user.dir" );
		keyStoreName = "keystore.jks";
		trustStoreName = "cacerts.jks";
		rootCertCache = null;

		try
		{
			keystore = KeyStore.getInstance( KeyStore.getDefaultType() );
			truststore = KeyStore.getInstance( KeyStore.getDefaultType() );
		}
		catch ( KeyStoreException e )
		{
			LOG.error( "Cannot instantiate keystore: ", e );
		}
	}

	protected void load() throws Exception
	{
		readExtraEntries();
		loadKeyStores();
		privateCredentialRoot = loadCertificate( "marchnetworks", "marchnetworks_privatekey" );
		privateCredentialCommand = loadCertificate( "command", "command_privatekey" );
		privateCredentialServer = loadCertificate( "server", "server_privatekey" );
		loadRootCertCache();
	}

	protected void loadKeyStores() throws Exception
	{
		String sKeyFile = getStorePath() + File.separator + getKeyStoreName();
		String sTrustFile = getStorePath() + File.separator + getTrustStoreName();

		File f = new File( sKeyFile );
		if ( !f.exists() )
		{
			throw new Exception( "Keystore file doesn't exist: " + sKeyFile );
		}
		f = new File( sTrustFile );
		if ( !f.exists() )
		{
			throw new Exception( "Truststore file doesn't exist: " + sTrustFile );
		}

		FileInputStream keys_in = new FileInputStream( sKeyFile );
		keystore.load( keys_in, KEY_STORE_PASSWORD );
		FileInputStream trust_in = new FileInputStream( sTrustFile );
		truststore.load( trust_in, TRUST_STORE_PASSWORD );
	}

	protected X500PrivateCredential loadCertificate( String alias, String keyAlias ) throws Exception
	{
		X509Certificate xc = ( X509Certificate ) keystore.getCertificate( alias );
		PrivateKey pk = ( PrivateKey ) keystore.getKey( keyAlias, KEY_STORE_PASSWORD );
		return new X500PrivateCredential( xc, pk, alias );
	}

	protected void loadRootCertCache() throws Exception
	{
		Certificate rc = keystore.getCertificate( "marchnetworks" );
		if ( rc == null )
		{
			LOG.error( "Keystore does not contain certificate for {}", "marchnetworks" );
		}
		else
		{
			rootCertCache = rc.getEncoded();
		}
	}

	protected boolean isServerCertComplete( X509Certificate sc, Collection<GeneralName> gnEntries )
	{
		Collection<List<?>> listAltNames;

		try
		{
			listAltNames = sc.getSubjectAlternativeNames();
		}
		catch ( CertificateParsingException e )
		{
			LOG.error( "Couldn't parse server certificate", e );
			return false;
		}

		Set<String> setCert = new TreeSet( String.CASE_INSENSITIVE_ORDER );
		for ( List<?> l : listAltNames )
		{
			if ( l.size() == 2 )
			{
				Object o = l.get( 1 );
				if ( ( o instanceof String ) )
				{
					String s = o.toString();
					if ( s != null )
					{
						setCert.add( s );
					}
				}
			}
		}

		Set<String> setReal = new TreeSet( String.CASE_INSENSITIVE_ORDER );
		for ( GeneralName gn : gnEntries )
		{

			if ( gn.getTagNo() == 2 )
			{
				String name = gn.getName().toString();
				setReal.add( name );
			}
		}

		LOG.info( "--Cert:\n   " + setCert.toString() );
		LOG.info( "--Scan:\n   " + setReal.toString() );

		return setReal.equals( setCert );
	}

	protected Collection<GeneralName> getAllNetworkExtraEntries()
	{
		Collection<GeneralName> NetworkList = getNetworkList();
		Collection<GeneralName> ExtraEntries = getExtraEntries();

		ArrayList<GeneralName> Result = new ArrayList( NetworkList );
		Result.addAll( ExtraEntries );
		return Result;
	}

	protected Collection<GeneralName> getNetworkList()
	{
		ArrayList<GeneralName> gns = new ArrayList();

		gns.add( new GeneralName( 2, hostName ) );

		for ( InetAddress ia : inetAddresses )
		{

			gns.add( new GeneralName( 2, ia.getHostName() ) );

			String addy = ia.getHostAddress();
			gns.add( new GeneralName( 2, addy ) );

			byte[] ip = Utils.ipString2Octets( addy );
			gns.add( new GeneralName( 7, new DEROctetString( ip ) ) );

			String fqdn = ia.getCanonicalHostName();
			if ( !fqdn.endsWith( addy ) )
				gns.add( new GeneralName( 2, fqdn ) );
		}
		return gns;
	}

	protected Collection<GeneralName> getExtraEntries()
	{
		ArrayList<GeneralName> gns = new ArrayList();

		for ( String hn : extraHostNames )
		{
			gns.add( new GeneralName( 2, hn ) );
		}

		for ( byte[] ip : extraIPs )
		{
			String sIP = Utils.ipOctets2String( ip );
			gns.add( new GeneralName( 2, sIP ) );

			gns.add( new GeneralName( 7, new DEROctetString( ip ) ) );
		}
		return gns;
	}

	protected static Collection<UserCertificate> readConfigDirCerts( String dir )
	{
		File fdir = new File( dir );
		FilenameFilter ff = new FilenameFilter()
		{
			public boolean accept( File dir, String name )
			{
				return ( name.endsWith( ".cer" ) ) || ( name.endsWith( ".CER" ) );
			}
		};
		File[] certFiles = fdir.listFiles( ff );
		ArrayList<UserCertificate> Result = new ArrayList( certFiles.length );

		for ( File f : certFiles )
		{
			Certificate c = readCertificate( f );
			if ( c != null )
			{
				String alias = f.getName().substring( 0, f.getName().length() - 4 );
				alias = alias.toLowerCase();
				if ( isCertAliasClashing( alias ) )
				{
					LOG.warn( "User-supplied certificate with filename " + f.getName() + " conflicts with CES certificate name." );
					LOG.warn( "Please rename the certificate file to a different name and restart Enterprise." );
				}
				else
				{
					Result.add( new UserCertificate( c, alias ) );
				}
			}
		}
		return Result;
	}

	protected static Certificate readCertificate( File f )
	{
		Certificate Result = null;
		try
		{
			FileInputStream fis = new FileInputStream( f );
			CertificateFactory cf = CertificateFactory.getInstance( "X509" );
			try
			{
				Result = cf.generateCertificate( fis );
			}
			finally
			{
				fis.close();
			}
		}
		catch ( Exception e )
		{
			LOG.warn( "Error loading certificate from file " + f.getName() + ": ", e );
		}
		return Result;
	}

	protected static boolean isCertAliasClashing( String s )
	{
		return ( s.equalsIgnoreCase( "marchnetworks" ) ) || ( s.equalsIgnoreCase( "command" ) ) || ( s.equalsIgnoreCase( "server" ) );
	}

	protected static Collection<UserCertificate> readExtraTruststoreCerts( KeyStore truststore )
	{
		ArrayList<UserCertificate> Result = new ArrayList();
		List<String> aliases = null;

		try
		{
			aliases = Collections.list( truststore.aliases() );
		}
		catch ( KeyStoreException e )
		{
			LOG.error( "Error loading list of aliases in truststore: ", e );
		}

		for ( String s : aliases )
		{
			if ( !isCertAliasClashing( s ) )
			{
				try
				{

					Certificate c = truststore.getCertificate( s );
					Result.add( new UserCertificate( c, s ) );
				}
				catch ( KeyStoreException e )
				{
					LOG.warn( "Error loading certificate with alias " + s + ": ", e );
				}
			}
		}
		return Result;
	}

	protected boolean isUserCertsEqual( Collection<UserCertificate> A, Collection<UserCertificate> B )
	{
		if ( ( A == null ) && ( B == null ) )
			return true;
		if ( ( A == null ) || ( B == null ) )
		{
			return false;
		}

		if ( A.size() != B.size() )
		{
			return false;
		}

		for ( UserCertificate ucA : A )
		{
			boolean found = false;
			for ( UserCertificate ucB : B )
			{
				if ( ucA.getAlias().equalsIgnoreCase( ucB.getAlias() ) )
				{
					found = true;

					if ( !ucA.getCertificate().equals( ucB.getCertificate() ) )
					{
						return false;
					}
				}
			}
			if ( !found )
				return false;
		}
		return true;
	}

	private boolean isLocalIpAddress( String address )
	{
		for ( InetAddress netAddress : inetAddresses )
		{
			if ( netAddress.getHostAddress().equalsIgnoreCase( address ) )
			{
				return true;
			}
		}
		return false;
	}

	public X500PrivateCredential getCommandCredential()
	{
		return privateCredentialCommand;
	}

	public String[] getCommandCertChain() throws Exception
	{
		ArrayList<Certificate> list = new ArrayList();
		list.add( privateCredentialRoot.getCertificate() );
		list.add( privateCredentialCommand.getCertificate() );

		String[] listCertsPEM = Utils.convertCertsToPEMStringArray( list );
		return listCertsPEM;
	}

	public String[] getServerCertChain() throws Exception
	{
		ArrayList<Certificate> list = new ArrayList();
		list.add( privateCredentialRoot.getCertificate() );
		list.add( privateCredentialCommand.getCertificate() );
		list.add( privateCredentialServer.getCertificate() );

		String[] listCertsPEM = Utils.convertCertsToPEMStringArray( list );
		return listCertsPEM;
	}

	public byte[] getRootCertCache()
	{
		return rootCertCache;
	}

	public String getServerHostName()
	{
		return hostName;
	}

	public String getStorePath()
	{
		return keyStorePath;
	}

	public String getKeyStoreName()
	{
		return keyStoreName;
	}

	public String getTrustStoreName()
	{
		return trustStoreName;
	}
}
