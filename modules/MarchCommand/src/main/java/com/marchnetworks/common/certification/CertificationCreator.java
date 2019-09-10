package com.marchnetworks.common.certification;

import com.marchnetworks.common.config.AppConfig;
import com.marchnetworks.common.config.AppConfigImpl;
import com.marchnetworks.common.config.ConfigProperty;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Vector;

import javax.security.auth.x500.X500Principal;
import javax.security.auth.x500.X500PrivateCredential;

public class CertificationCreator extends CertificationReader
{
	private static final Logger LOG = LoggerFactory.getLogger( CertificationCreator.class );

	private static final int VALIDITY_START_YEAR = 2000;

	private static final int VALIDITY_END_YEAR = 2030;

	private static final int MINIMUM_PUBLIC_KEY_LENGTH = 1024;

	public static final String BEGIN_CERTIFICATE_REQUEST = "-----BEGIN CERTIFICATE REQUEST-----";
	public static final String END_CERTIFICATE_REQUEST = "-----END CERTIFICATE REQUEST-----";
	public static final char[] ROOT_PASSWORD = "B1ackD1amonds".toCharArray();
	public static final char[] COMMAND_PASSWORD = "B1ackD1amonds".toCharArray();
	public static final char[] SERVER_PASSWORD = "B1ackD1amonds".toCharArray();

	private Date certStartDate = null;
	private Date certEndDate = null;

	private int publicKeyLength;
	private Collection<UserCertificate> m_ConfigDirCerts;

	public CertificationCreator()
	{
		initStartEndDates();
	}

	private void initStartEndDates()
	{
		TimeZone tz = TimeZone.getTimeZone( "GMT" );
		Calendar c = new GregorianCalendar( tz );

		c.clear();
		c.set( 2000, 0, 1, 0, 0, 0 );
		certStartDate = c.getTime();

		c.clear();
		c.set( 2030, 0, 1, 0, 0, 0 );
		certEndDate = c.getTime();
	}

	public void AssureCertificates( String Path ) throws Exception
	{
		clear();

		File fPath = new File( Path );
		if ( !fPath.exists() )
			throw new Exception( "Certificate directory " + Path + " doesn't exist" );
		keyStorePath = Path;

		readNetworkList();
		try
		{
			readExtraEntries();
		}
		catch ( IOException e )
		{
			LOG.warn( "Error loading config file: ", e );
			LOG.warn( "Not adding any extra entries from config file" );
		}

		Collection<GeneralName> gnEntries = getAllNetworkExtraEntries();

		m_ConfigDirCerts = readConfigDirCerts( keyStorePath );

		try
		{
			loadKeyStores();
		}
		catch ( Exception e )
		{
			if ( ( e instanceof FileNotFoundException ) )
			{
				LOG.info( "Keystore files not found." );
			}
			else
				LOG.warn( "Exception loading keystores: ", e );
			RegenRootCerts( gnEntries );
			return;
		}

		try
		{
			privateCredentialRoot = loadCertificate( "marchnetworks", "marchnetworks_privatekey" );
		}
		catch ( Exception e )
		{
			LOG.warn( "Exception loading root cert: ", e );
			RegenRootCerts( gnEntries );
			return;
		}

		if ( Utils.isExpired( privateCredentialRoot.getCertificate() ) )
		{
			LOG.warn( "Root certificate is expired" );
			RegenRootCerts( gnEntries );
			return;
		}

		try
		{
			privateCredentialCommand = loadCertificate( "command", "command_privatekey" );
		}
		catch ( Exception e )
		{
			LOG.warn( "Exception loading command cert: ", e );
			RegenCommandCerts( gnEntries );
			return;
		}

		if ( Utils.isExpired( privateCredentialCommand.getCertificate() ) )
		{
			LOG.warn( "Command certificate is expired" );
			RegenCommandCerts( gnEntries );
			return;
		}

		try
		{
			privateCredentialServer = loadCertificate( "server", "server_privatekey" );
		}
		catch ( Exception e )
		{
			LOG.warn( "Exception loading server cert: ", e );
			RegenServerCerts( gnEntries );
			return;
		}

		if ( Utils.isExpired( privateCredentialServer.getCertificate() ) )
		{
			LOG.warn( "Server certificate is expired" );
			RegenServerCerts( gnEntries );
			return;
		}

		if ( !isServerCertComplete( privateCredentialServer.getCertificate(), gnEntries ) )
		{
			LOG.warn( "Server certificate has wrong entries" );
			RegenServerCerts( gnEntries );
			return;
		}
		LOG.info( "Server cert has all needed entries." );

		Collection<UserCertificate> extraCertsInTruststore = readExtraTruststoreCerts( truststore );
		if ( !isUserCertsEqual( m_ConfigDirCerts, extraCertsInTruststore ) )
		{
			LOG.warn( "Truststore has invalid or missing user-supplied certificates" );
			ReplaceUserCerts( m_ConfigDirCerts );
			return;
		}

		LOG.info( "All certificates are valid" );
	}

	private Vector<DERObjectIdentifier> genCNOvector()
	{
		Vector<DERObjectIdentifier> Result = new Vector();
		Result.add( X509Name.CN );
		Result.add( X509Name.O );
		return Result;
	}

	private X509Name genRootIssuerDN()
	{
		Vector<DERObjectIdentifier> oid = genCNOvector();
		Vector<String> value = new Vector();
		value.add( "Command Root Certificate Authority" );
		value.add( "Command" );
		return new X509Name( oid, value );
	}

	private X509Name genCommandIssuerDN()
	{
		Vector<DERObjectIdentifier> oid = genCNOvector();
		Vector<String> value = new Vector();
		value.add( "Command Enterprise Certificate Authority" );
		value.add( "Command" );
		return new X509Name( oid, value );
	}

	private X509Name genServerIssuerDN()
	{
		Vector<DERObjectIdentifier> oid = genCNOvector();
		Vector<String> value = new Vector();
		value.add( getServerHostName() );
		value.add( "Command" );
		return new X509Name( oid, value );
	}

	private PKCS10CertificationRequest genCommandRequest( KeyPair commandKeypair ) throws Exception
	{
		return new PKCS10CertificationRequest( "SHA256withRSA", new X500Principal( "CN=Command Certificate" ), commandKeypair.getPublic(), new DERSet(), commandKeypair.getPrivate() );
	}

	private PKCS10CertificationRequest genServerRequest( KeyPair serverKeypair, Collection<GeneralName> Entries ) throws Exception
	{
		Vector<DERObjectIdentifier> oids = new Vector();
		Vector<X509Extension> values = new Vector();

		ASN1EncodableVector vec = new ASN1EncodableVector();

		for ( GeneralName gn : Entries )
		{
			vec.add( gn );
		}
		oids.add( X509Extensions.SubjectAlternativeName );

		GeneralNames subjectAltNames = new GeneralNames( new DERSequence( vec ) );
		values.add( new X509Extension( false, new DEROctetString( subjectAltNames ) ) );

		X509Extensions extensions = new X509Extensions( oids, values );

		Attribute attribute = new Attribute( PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, new DERSet( extensions ) );

		return new PKCS10CertificationRequest( "SHA256withRSA", new X500Principal( "CN=" + getServerHostName() ), serverKeypair.getPublic(), new DERSet( attribute ), serverKeypair.getPrivate() );
	}

	private void AddExtensions( X509Extensions exts, X509V3CertificateGenerator gn )
	{
		Enumeration<?> e = exts.oids();
		while ( e.hasMoreElements() )
		{
			DERObjectIdentifier oid = ( DERObjectIdentifier ) e.nextElement();
			X509Extension ext = exts.getExtension( oid );
			gn.addExtension( oid, ext.isCritical(), ext.getValue().getOctets() );
		}
	}

	private X509Certificate genRootCert( KeyPair rootKeys ) throws Exception
	{
		X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();

		String ALGORITHM = "SHA256WITHRSAENCRYPTION";

		certificateGenerator.setSerialNumber( BigInteger.valueOf( System.currentTimeMillis() ) );
		certificateGenerator.setIssuerDN( genRootIssuerDN() );

		certificateGenerator.setNotBefore( certStartDate );
		certificateGenerator.setNotAfter( certEndDate );

		certificateGenerator.setSubjectDN( genRootIssuerDN() );
		certificateGenerator.setPublicKey( rootKeys.getPublic() );

		certificateGenerator.addExtension( X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure( rootKeys.getPublic() ) );

		certificateGenerator.addExtension( X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure( rootKeys.getPublic() ) );

		certificateGenerator.addExtension( X509Extensions.BasicConstraints, true, new BasicConstraints( true ) );

		certificateGenerator.addExtension( X509Extensions.KeyUsage, true, new KeyUsage( 134 ) );

		certificateGenerator.setSignatureAlgorithm( "SHA256WITHRSAENCRYPTION" );

		return certificateGenerator.generate( rootKeys.getPrivate(), "BC" );
	}

	private X509Certificate genCommandCert( PKCS10CertificationRequest request, X500PrivateCredential superCredential ) throws Exception
	{
		X509Certificate superCert = superCredential.getCertificate();

		if ( !request.verify( "BC" ) )
		{
			LOG.warn( "Provider is not BC!" );
		}

		X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();

		String ALGORITHM = "SHA256WithRSAEncryption";

		certificateGenerator.setSerialNumber( BigInteger.valueOf( System.currentTimeMillis() ) );
		certificateGenerator.setIssuerDN( superCert.getSubjectX500Principal() );

		certificateGenerator.setNotBefore( certStartDate );
		certificateGenerator.setNotAfter( certEndDate );

		certificateGenerator.setSignatureAlgorithm( "SHA256WithRSAEncryption" );

		certificateGenerator.addExtension( X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure( superCert ) );

		certificateGenerator.setSubjectDN( genCommandIssuerDN() );
		certificateGenerator.setPublicKey( request.getPublicKey() );

		certificateGenerator.addExtension( X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure( request.getPublicKey() ) );

		certificateGenerator.addExtension( X509Extensions.BasicConstraints, true, new BasicConstraints( true ) );

		certificateGenerator.addExtension( X509Extensions.KeyUsage, true, new KeyUsage( 166 ) );

		ASN1Set requestInfo = request.getCertificationRequestInfo().getAttributes();

		for ( int i = 0; i != requestInfo.size(); i++ )
		{
			Attribute attribute = Attribute.getInstance( requestInfo.getObjectAt( i ) );

			if ( attribute.getAttrType().equals( PKCSObjectIdentifiers.pkcs_9_at_extensionRequest ) )
			{
				X509Extensions ext = X509Extensions.getInstance( attribute.getAttrValues().getObjectAt( 0 ) );

				AddExtensions( ext, certificateGenerator );
			}
		}

		X509Certificate issuedCert = certificateGenerator.generate( superCredential.getPrivateKey() );

		return issuedCert;
	}

	private X509Certificate genServerCert( PKCS10CertificationRequest certificationRequest, X500PrivateCredential superCredential ) throws Exception
	{
		X509Certificate superCertificate = superCredential.getCertificate();

		String BOUNCY_CASTLE = "BC";
		String ALGORITHM = "SHA256WithRSAEncryption";

		if ( !certificationRequest.verify( "BC" ) )
		{
			LOG.warn( "Certification Request: Provider is not a BC" );
		}

		X509V3CertificateGenerator x509v3Certificate = new X509V3CertificateGenerator();

		x509v3Certificate.setSubjectDN( genServerIssuerDN() );

		x509v3Certificate.setSignatureAlgorithm( "SHA256WithRSAEncryption" );

		x509v3Certificate.setPublicKey( certificationRequest.getPublicKey() );

		x509v3Certificate.addExtension( X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure( superCertificate ) );

		x509v3Certificate.setNotBefore( certStartDate );
		x509v3Certificate.setNotAfter( certEndDate );

		x509v3Certificate.addExtension( X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure( certificationRequest.getPublicKey() ) );

		x509v3Certificate.addExtension( X509Extensions.BasicConstraints, true, new BasicConstraints( false ) );

		x509v3Certificate.setSerialNumber( BigInteger.valueOf( System.currentTimeMillis() ) );
		x509v3Certificate.setIssuerDN( superCertificate.getSubjectX500Principal() );

		x509v3Certificate.addExtension( X509Extensions.KeyUsage, true, new KeyUsage( 160 ) );

		ASN1Set certificationRequestInfo = certificationRequest.getCertificationRequestInfo().getAttributes();

		for ( int i = 0; i != certificationRequestInfo.size(); i++ )
		{
			Attribute attr = Attribute.getInstance( certificationRequestInfo.getObjectAt( i ) );

			if ( attr.getAttrType().equals( PKCSObjectIdentifiers.pkcs_9_at_extensionRequest ) )
			{
				X509Extensions ext = X509Extensions.getInstance( attr.getAttrValues().getObjectAt( 0 ) );

				AddExtensions( ext, x509v3Certificate );
			}
		}

		X509Certificate issuedCert = x509v3Certificate.generate( superCredential.getPrivateKey() );

		return issuedCert;
	}

	private X500PrivateCredential genRootCred() throws Exception
	{
		int keyLength = getPublicKeyLength();
		KeyPair kp = Utils.generateRSAKeyPair( keyLength );
		X509Certificate rootCert = genRootCert( kp );
		return new X500PrivateCredential( rootCert, kp.getPrivate(), "marchnetworks" );
	}

	private X500PrivateCredential genCommandCred( X500PrivateCredential cRoot ) throws Exception
	{
		int keyLength = getPublicKeyLength();
		KeyPair kp = Utils.generateRSAKeyPair( keyLength );
		PKCS10CertificationRequest commandCSR = genCommandRequest( kp );

		X509Certificate commandCert = genCommandCert( commandCSR, cRoot );

		return new X500PrivateCredential( commandCert, kp.getPrivate(), "command" );
	}

	public X500PrivateCredential genServerCred( X500PrivateCredential cCommand, Collection<GeneralName> gnEntries ) throws Exception
	{
		int keyLength = getPublicKeyLength();
		KeyPair kp = Utils.generateRSAKeyPair( keyLength );
		PKCS10CertificationRequest serverCSR = genServerRequest( kp, gnEntries );

		X509Certificate serverCert = genServerCert( serverCSR, cCommand );

		return new X500PrivateCredential( serverCert, kp.getPrivate(), "server" );
	}

	private KeyStore genKeyStore( X500PrivateCredential cRoot, X500PrivateCredential cCommand, X500PrivateCredential cServer ) throws Exception
	{
		LOG.debug( "Generating KeyStore" );

		KeyStore store = KeyStore.getInstance( KeyStore.getDefaultType() );
		store.load( null, null );

		Certificate[] serverChain = new Certificate[3];
		serverChain[0] = cServer.getCertificate();
		serverChain[1] = cCommand.getCertificate();
		serverChain[2] = cRoot.getCertificate();

		store.setKeyEntry( "server_privatekey", cServer.getPrivateKey(), SERVER_PASSWORD, serverChain );
		store.setCertificateEntry( cServer.getAlias(), cServer.getCertificate() );

		store.setKeyEntry( "command_privatekey", cCommand.getPrivateKey(), COMMAND_PASSWORD, new Certificate[] {cCommand.getCertificate(), cRoot.getCertificate()} );
		store.setCertificateEntry( cCommand.getAlias(), cCommand.getCertificate() );

		store.setKeyEntry( "marchnetworks_privatekey", cRoot.getPrivateKey(), ROOT_PASSWORD, new Certificate[] {cRoot.getCertificate()} );
		store.setCertificateEntry( cRoot.getAlias(), cRoot.getCertificate() );

		return store;
	}

	private KeyStore genTrustStore( X500PrivateCredential cRoot, X500PrivateCredential cCommand, X500PrivateCredential cServer ) throws Exception
	{
		LOG.debug( "Generating TrustStore" );

		KeyStore trustStore = KeyStore.getInstance( KeyStore.getDefaultType() );
		trustStore.load( null, null );

		trustStore.setCertificateEntry( "server", cServer.getCertificate() );
		trustStore.setCertificateEntry( "command", cCommand.getCertificate() );
		trustStore.setCertificateEntry( "marchnetworks", cRoot.getCertificate() );

		return trustStore;
	}

	private void SaveKeyStoreToDisk( String filepath, KeyStore ks, char[] KeyStorePassword ) throws Exception
	{
		FileOutputStream os = new FileOutputStream( filepath );
		ks.store( os, KeyStorePassword );
		os.flush();
		os.close();
	}

	private void RegenRootCerts( Collection<GeneralName> gnEntries ) throws Exception
	{
		LOG.info( "Regenerating root certificate..." );
		privateCredentialRoot = genRootCred();

		RegenCommandCerts( gnEntries );
	}

	private void RegenCommandCerts( Collection<GeneralName> gnEntries ) throws Exception
	{
		LOG.info( "Regenerating command certificate..." );
		privateCredentialCommand = genCommandCred( privateCredentialRoot );

		RegenServerCerts( gnEntries );
	}

	private void RegenServerCerts( Collection<GeneralName> gnEntries ) throws Exception
	{
		LOG.info( "Regenerating server certificate..." );
		privateCredentialServer = genServerCred( privateCredentialCommand, gnEntries );

		ReplaceUserCerts( m_ConfigDirCerts );
	}

	private void ReplaceUserCerts( Collection<UserCertificate> certs ) throws Exception
	{
		LOG.info( "Regenerating the keystore and truststore..." );
		keystore = genKeyStore( privateCredentialRoot, privateCredentialCommand, privateCredentialServer );
		truststore = genTrustStore( privateCredentialRoot, privateCredentialCommand, privateCredentialServer );

		if ( !certs.isEmpty() )
		{
			LOG.info( "Adding user-supplied certificates to truststore..." );
			for ( UserCertificate c : certs )
			{
				truststore.setCertificateEntry( c.getAlias(), c.getCertificate() );
			}
		}

		LOG.info( "Saving keystore and truststore to disk..." );
		SaveKeyStoreToDisk( keyStorePath + File.separator + keyStoreName, keystore, KEY_STORE_PASSWORD );
		SaveKeyStoreToDisk( keyStorePath + File.separator + trustStoreName, truststore, TRUST_STORE_PASSWORD );
	}

	public String[] signAgentCSRArray( String CSRPem ) throws Exception
	{
		ArrayList<Certificate> list = signAgentCSR_buildList( CSRPem );
		return Utils.convertCertsToPEMStringArray( list );
	}

	private ArrayList<Certificate> signAgentCSR_buildList( String CSRPem ) throws Exception
	{
		Certificate agentCert = signDeviceCSRByCommand( CSRPem );
		ArrayList<Certificate> list = new ArrayList();
		list.add( privateCredentialRoot.getCertificate() );
		list.add( privateCredentialCommand.getCertificate() );
		list.add( agentCert );
		return list;
	}

	private X509Certificate signDeviceCSRByCommand( String csrPEM ) throws Exception
	{
		PKCS10CertificationRequest pkcs10Request = genCSRFromPEMString( csrPEM );
		X500PrivateCredential superCredential = privateCredentialCommand;

		X509Certificate superCert = superCredential.getCertificate();

		String BOUNCY_CASTLE = "BC";
		String ALGORITHM = "SHA256WithRSAEncryption";

		if ( !pkcs10Request.verify( "BC" ) )
		{
			LOG.error( "Certification Request: Provider is not a BC." );
		}

		X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();

		certificateGenerator.setNotBefore( certStartDate );
		certificateGenerator.setNotAfter( certEndDate );

		certificateGenerator.addExtension( X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure( superCert ) );

		certificateGenerator.addExtension( X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure( pkcs10Request.getPublicKey() ) );

		certificateGenerator.addExtension( X509Extensions.BasicConstraints, true, new BasicConstraints( true ) );

		certificateGenerator.setSubjectDN( pkcs10Request.getCertificationRequestInfo().getSubject() );

		certificateGenerator.setPublicKey( pkcs10Request.getPublicKey() );

		certificateGenerator.setSignatureAlgorithm( "SHA256WithRSAEncryption" );

		certificateGenerator.setSerialNumber( BigInteger.valueOf( System.currentTimeMillis() ) );
		certificateGenerator.setIssuerDN( superCert.getSubjectX500Principal() );

		certificateGenerator.addExtension( X509Extensions.KeyUsage, true, new KeyUsage( 166 ) );

		ASN1Set attributes = pkcs10Request.getCertificationRequestInfo().getAttributes();

		for ( int i = 0; i != attributes.size(); i++ )
		{
			Attribute attr = Attribute.getInstance( attributes.getObjectAt( i ) );

			if ( attr.getAttrType().equals( PKCSObjectIdentifiers.pkcs_9_at_extensionRequest ) )
			{
				X509Extensions ext = X509Extensions.getInstance( attr.getAttrValues().getObjectAt( 0 ) );

				AddExtensions( ext, certificateGenerator );
			}
		}

		X509Certificate issuedCert = certificateGenerator.generate( superCredential.getPrivateKey() );

		return issuedCert;
	}

	private PKCS10CertificationRequest genCSRFromPEMString( String csrPEM ) throws Exception
	{
		ByteArrayOutputStream ostr = null;
		PrintStream opstr = null;
		PKCS10CertificationRequest pkcs10 = null;

		BufferedReader reader = new BufferedReader( new StringReader( csrPEM ) );

		ostr = new ByteArrayOutputStream();
		opstr = new PrintStream( ostr );
		String temp;
		while ( ( ( temp = reader.readLine() ) != null ) && ( !temp.equals( "-----BEGIN CERTIFICATE REQUEST-----" ) ) )
		{
		}

		if ( !temp.equals( "-----BEGIN CERTIFICATE REQUEST-----" ) )
		{
			throw new IOException( "Missing -----BEGIN CERTIFICATE REQUEST----- boundary" );
		}

		while ( ( ( temp = reader.readLine() ) != null ) && ( !temp.equals( "-----END CERTIFICATE REQUEST-----" ) ) )
		{
			opstr.print( temp );
		}

		if ( !temp.equals( "-----END CERTIFICATE REQUEST-----" ) )
			throw new IOException( "Missing -----END CERTIFICATE REQUEST----- boundary" );
		opstr.close();

		byte[] certbuf = Base64.decode( ostr.toByteArray() );
		ostr.close();

		pkcs10 = new PKCS10CertificationRequest( certbuf );

		return pkcs10;
	}

	private int getPublicKeyLength()
	{
		if ( publicKeyLength == 0 )
		{
			publicKeyLength = 1024;
			String s = keyStorePath + File.separator + "march.server.config.xml";
			try
			{
				AppConfig appConfig = new AppConfigImpl( s );
				String keyLength = appConfig.getProperty( ConfigProperty.CERT_PUBLIC_KEY_LENGTH );
				if ( ( keyLength != null ) && ( Integer.parseInt( keyLength ) > 1024 ) )
				{
					publicKeyLength = Integer.parseInt( keyLength );
					LOG.info( "Using custom public key length size of {} bits", publicKeyLength );
				}
			}
			catch ( IOException e )
			{
				LOG.warn( "Failed to read config file. Details:{}", e.getMessage() );
			}
		}
		return publicKeyLength;
	}
}
