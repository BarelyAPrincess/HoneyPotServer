package com.marchnetworks.license.model;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.license.Crypto;
import com.marchnetworks.license.LicenseUtils;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ServerLicenseImport
{
	protected static final String V10_LICENSE_SEPARATOR = "--License Seperator--";
	public static final String ERROR_V10LICENSE = "Version 1.0 license is not supported";
	protected String m_sSourceXml;
	protected String m_sVersion;
	protected String m_sIssuer;
	protected Date m_Date;
	protected byte[] m_bServerId;
	protected List<LicenseImport> m_lLicenses;
	protected byte[] m_bSignature;

	public String getVersion()
	{
		return m_sVersion;
	}

	public String getIssuer()
	{
		return m_sIssuer;
	}

	public Date getDate()
	{
		return m_Date;
	}

	public byte[] getServerId()
	{
		return m_bServerId;
	}

	public List<LicenseImport> getLicenseImportList()
	{
		return m_lLicenses;
	}

	public List<RecordingLicenseImport> getRecordingLicenses()
	{
		List<RecordingLicenseImport> result = new ArrayList();
		for ( LicenseImport license : m_lLicenses )
		{
			if ( ( license instanceof RecordingLicenseImport ) )
			{
				result.add( ( RecordingLicenseImport ) license );
			}
		}
		return result;
	}

	public List<AppLicenseImport> getAppLicenses()
	{
		List<AppLicenseImport> result = new ArrayList();
		for ( LicenseImport license : m_lLicenses )
		{
			if ( ( license instanceof AppLicenseImport ) )
			{
				result.add( ( AppLicenseImport ) license );
			}
		}
		return result;
	}

	public boolean requiresServerId()
	{
		List<AppLicenseImport> appLicenses = getAppLicenses();
		if ( appLicenses.isEmpty() )
		{
			return true;
		}
		for ( AppLicenseImport appLicense : appLicenses )
		{
			if ( !appLicense.isOpen() )
			{
				return true;
			}
		}
		return false;
	}

	public String getSourceXml()
	{
		return m_sSourceXml;
	}

	public byte[] getSignature()
	{
		return m_bSignature;
	}

	public String getContentString()
	{
		int i = m_sSourceXml.indexOf( "<content>" ) + 9;
		int j = m_sSourceXml.indexOf( "</content>" );
		return m_sSourceXml.substring( i, j );
	}

	public static ServerLicenseImport parseServerLicenseImport( String xml ) throws LicenseException
	{
		Document d = null;

		String[] licenses = xml.split( "--License Seperator--" );

		if ( licenses.length > 1 )
		{
			throw new LicenseException( "Version 1.0 license is not supported", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			DocumentBuilder db = factory.newDocumentBuilder();
			InputSource inStream = new InputSource();
			inStream.setCharacterStream( new StringReader( licenses[0] ) );
			d = db.parse( inStream );
		}
		catch ( Exception e )
		{
			throw new LicenseException( "Error parsing XML structure of license: ", LicenseExceptionType.LICENSE_PARSE_FAILED, e );
		}

		String version;

		try
		{
			version = LicenseUtils.findVersion( d, "portallicense" );
		}
		catch ( LicenseException e1 )
		{
			try
			{
				version = LicenseUtils.findVersion( d, "license" );
			}
			catch ( LicenseException e )
			{
				throw new LicenseException( "No license header or version found", LicenseExceptionType.LICENSE_PARSE_FAILED );
			}
		}

		if ( !version.equals( "1.1" ) )
		{
			if ( version.equals( "1.0" ) )
			{
				throw new LicenseException( "Version 1.0 license is not supported", LicenseExceptionType.LICENSE_PARSE_FAILED );
			}
			throw new LicenseException( "Version " + version + " license is not supported", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		String sIssuer = null;
		String sDate = null;
		Date dDate = null;
		String sServerId = null;
		byte[] bServerId = null;
		String sSignature = null;
		byte[] bSignature = null;
		ArrayList<Node> nLicenses = new ArrayList();
		nLicenses.ensureCapacity( 3 );

		NodeList nd = d.getElementsByTagName( "content" );
		if ( nd.getLength() != 1 )
			throw new LicenseException( "Malformed license", LicenseExceptionType.LICENSE_PARSE_FAILED );
		Node n = nd.item( 0 );

		nd = n.getChildNodes();
		for ( int i = 0; i < nd.getLength(); i++ )
		{
			n = nd.item( i );
			String s = n.getNodeName();

			if ( s.equals( "issuer" ) )
			{
				sIssuer = n.getTextContent();
			}
			else if ( s.equals( "date" ) )
			{
				sDate = n.getTextContent();
			}
			else if ( s.equals( "serverId" ) )
			{
				sServerId = n.getTextContent();
			}
			else if ( s.equals( "license" ) )
			{
				nLicenses.add( n );
			}
		}

		if ( CommonAppUtils.isNullOrEmptyString( sIssuer ) )
			throw new LicenseException( "No <issuer> found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		if ( CommonAppUtils.isNullOrEmptyString( sDate ) )
			throw new LicenseException( "No <date> found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		if ( nLicenses.isEmpty() )
		{
			throw new LicenseException( "No licenses found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		dDate = LicenseUtils.SAMLstring2Date( sDate );
		if ( dDate == null )
		{
			throw new LicenseException( "Bad date string: " + sDate, LicenseExceptionType.LICENSE_PARSE_FAILED );
		}
		try
		{
			bServerId = Crypto.stringBase64ToByte( sServerId );
		}
		catch ( IOException e )
		{
			throw new LicenseException( "Bad serverId string", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		nd = d.getElementsByTagName( "signature" );
		if ( nd.getLength() < 1 )
			throw new LicenseException( "No signature found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		if ( nd.getLength() > 1 )
			throw new LicenseException( "Multiple signatures found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		sSignature = nd.item( 0 ).getTextContent();

		if ( CommonAppUtils.isNullOrEmptyString( sSignature ) )
			throw new LicenseException( "No signature found", LicenseExceptionType.LICENSE_PARSE_FAILED );

		try
		{
			bSignature = Crypto.stringBase64ToByte( sSignature );
		}
		catch ( IOException e )
		{
			throw new LicenseException( "Bad signature", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		ArrayList<LicenseImport> slts = new ArrayList();
		slts.ensureCapacity( nLicenses.size() );
		for ( Node nl : nLicenses )
		{
			slts.add( LicenseImport.parseServerLicenseImport( nl ) );
		}

		if ( checkDuplicateLicenseTypes( slts ) )
		{
			throw new LicenseException( "Duplicate license types found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		ServerLicenseImport result = new ServerLicenseImport();
		result.m_sSourceXml = xml;
		result.m_sVersion = version;
		result.m_sIssuer = sIssuer;
		result.m_Date = dDate;
		result.m_bServerId = bServerId;
		result.m_bSignature = bSignature;
		result.m_lLicenses = Collections.unmodifiableList( slts );
		return result;
	}

	protected static boolean checkDuplicateLicenseTypes( List<LicenseImport> list )
	{
		for ( int i = 0; i < list.size(); i++ )
		{
			LicenseImport first = ( LicenseImport ) list.get( i );
			if ( ( first instanceof RecordingLicenseImport ) )
			{
				LicenseType t = ( ( RecordingLicenseImport ) first ).getType();

				for ( int j = 0; j < list.size(); j++ )
				{
					if ( j != i )
					{

						LicenseImport second = ( LicenseImport ) list.get( j );
						if ( ( ( second instanceof RecordingLicenseImport ) ) && ( t == ( ( RecordingLicenseImport ) second ).getType() ) )
							return true;
					}
				}
			}
		}
		return false;
	}
}
