package com.marchnetworks.security.smartcard;

import com.marchnetworks.command.common.Base64;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.management.data.FileStorageView;
import com.marchnetworks.management.file.service.FileStorageException;
import com.marchnetworks.management.file.service.FileStorageService;
import com.marchnetworks.management.user.UserService;

import org.apache.commons.lang.ArrayUtils;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionDestroyedEvent;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import javax.crypto.Cipher;

public class CertStringGenerator implements SmartCardCertificateService, ApplicationListener<SessionDestroyedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger( CertStringGenerator.class );

	private ArrayList<CertificateValidationSession> certSessions;

	private UserService userService;
	private FileStorageService fileService;
	private Random generator;
	private byte[] buffer;

	public CertStringGenerator()
	{
		generator = new Random();
		certSessions = new ArrayList();
	}

	public String certRequestString( Authentication authResult, String sessionId )
	{
		String encodedBytes = null;
		byte[] envelope = null;

		MemberView aMember = userService.getMember( authResult.getName() );
		CertificateValidationSession aSession = new CertificateValidationSession();
		aSession.setSessionId( sessionId.getBytes() );
		FileStorageView aFile = null;
		try
		{
			aFile = fileService.getFileStorage( aMember.getDetailsView().getCertificateId() );
		}
		catch ( FileStorageException e )
		{
			LOG.warn( "Error loading certificate file: " + e.getMessage() );
		}
		X509Certificate aCert = loadCertificate( aFile.getTheBytes() );

		byte[] encodedCertId = convertCertId( aCert.getSerialNumber().toByteArray() );
		aSession.setCertId( encodedCertId );

		try
		{
			PublicKey aKey = aCert.getPublicKey();

			Cipher cipher = Cipher.getInstance( "RSA" );
			cipher.init( 1, aKey );

			buffer = new byte[117];
			generator.nextBytes( buffer );
			String randomBuffer = authResult.getName() + buffer;
			aSession.setValidationString( randomBuffer.getBytes( "UTF-8" ) );
			aSession.setAuthentication( authResult );

			envelope = generateEnvelope( randomBuffer.getBytes(), aCert );
			byte[] encodedValidationString = Base64.encodeBytesToBytes( envelope );
			certSessions.add( aSession );

			encodedBytes = new String( encodedCertId, "UTF-8" ) + " " + new String( encodedValidationString, "UTF-8" );
		}
		catch ( Exception e )
		{
			LOG.warn( "Error in encryption process during login: " + e.getMessage() );
		}

		return encodedBytes;
	}

	public byte[] convertCertId( byte[] byteArray )
	{
		ArrayUtils.reverse( byteArray );
		return Base64.encodeBytesToBytes( Arrays.copyOf( byteArray, 16 ) );
	}

	public X509Certificate loadCertificate( byte[] theCert )
	{
		try
		{
			InputStream is = new ByteArrayInputStream( theCert );
			CertificateFactory factory = CertificateFactory.getInstance( "X.509" );
			X509Certificate generateCertificate = ( X509Certificate ) factory.generateCertificate( is );
			is.close();
			return generateCertificate;
		}
		catch ( CertificateException e )
		{
			LOG.warn( "Error loading certificate file: " + e.getMessage() );
		}
		catch ( FileNotFoundException e )
		{
			LOG.warn( "Error loading certificate file: " + e.getMessage() );
		}
		catch ( IOException localIOException )
		{
		}

		return null;
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}

	public FileStorageService getFileService()
	{
		return fileService;
	}

	public void setFileService( FileStorageService fileService )
	{
		this.fileService = fileService;
	}

	public boolean isValid( byte[] sessionId, byte[] certId, byte[] decodedValidationString )
	{
		return certSessions.contains( new CertificateValidationSession( sessionId, certId, decodedValidationString ) );
	}

	public Authentication getAuthentication( String sessionId )
	{
		for ( CertificateValidationSession aSession : certSessions )
		{
			if ( Arrays.equals( aSession.getSessionId(), sessionId.getBytes() ) )
				return aSession.getAuthentication();
		}
		return null;
	}

	public void onApplicationEvent( SessionDestroyedEvent event )
	{
		String sessionId = event.getId();
		remove( sessionId );
	}

	public void remove( String id )
	{
		Iterator<CertificateValidationSession> anIterator = certSessions.iterator();
		while ( anIterator.hasNext() )
		{
			if ( ( ( CertificateValidationSession ) anIterator.next() ).getSessionId().equals( id ) )
			{
				anIterator.remove();
			}
		}
	}

	public byte[] generateEnvelope( byte[] data, X509Certificate aCert ) throws GeneralSecurityException, IOException
	{
		String algorithm = CMSEnvelopedDataGenerator.RC2_CBC;
		int keysize = 128;
		CMSEnvelopedDataGenerator fact = new CMSEnvelopedDataGenerator();
		fact.addKeyTransRecipient( aCert );
		CMSProcessableByteArray content = new CMSProcessableByteArray( data );
		try
		{
			CMSEnvelopedData envdata = fact.generate( content, algorithm, keysize, "BC" );

			return envdata.getEncoded();
		}
		catch ( Exception e )
		{
			LOG.warn( "Error generating envelope: " + e.getMessage() );
		}

		return null;
	}
}

