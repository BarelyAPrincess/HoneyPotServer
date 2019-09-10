package com.marchnetworks.license.model;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.license.LicenseUtils;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Date;

public class ServerLicenseImportType
{
	protected String m_sLicenseID;
	protected LicenseType m_Type;
	protected Expiry m_Expiry;
	protected boolean commerical;
	protected boolean open;
	protected Date m_dStart;
	protected Date m_dEnd;
	protected int m_iCount;
	protected ApplicationIdentityToken appToken;

	public static ServerLicenseImportType parseServerLicenseType( Node nLicense ) throws LicenseException
	{
		String sLicenseId = null;
		String sType = null;
		String sExpiry = null;
		Node nConditions = null;
		String sCount = null;
		Date dStart = null;
		Date dEnd = null;

		NodeList nd = nLicense.getChildNodes();
		for ( int i = 0; i < nd.getLength(); i++ )
		{
			Node n = nd.item( i );
			String s = n.getNodeName();

			if ( s.equals( "licenseId" ) )
			{
				sLicenseId = n.getTextContent();
			}
			else if ( s.equals( "type" ) )
			{
				sType = n.getTextContent();
			}
			else if ( s.equals( "expiry" ) )
			{
				sExpiry = n.getTextContent();
			}
			else if ( s.equals( "Conditions" ) )
			{
				nConditions = n;
			}
			else if ( s.equals( "count" ) )
			{
				sCount = n.getTextContent();
			}
		}

		if ( CommonAppUtils.isNullOrEmptyString( sLicenseId ) )
			throw new LicenseException( "No <licenseId> found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		if ( CommonAppUtils.isNullOrEmptyString( sType ) )
			throw new LicenseException( "No <type> found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		if ( CommonAppUtils.isNullOrEmptyString( sExpiry ) )
			throw new LicenseException( "No <expiry> found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		if ( CommonAppUtils.isNullOrEmptyString( sCount ) )
		{
			throw new LicenseException( "No <count> found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		LicenseType type = LicenseType.getByValue( sType );
		if ( type == null )
		{
			throw new LicenseException( "Unknown license type: " + sType, LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		Expiry expiry = Expiry.getByValue( sExpiry );
		if ( expiry == null )
		{
			throw new LicenseException( "Unknown expiry: " + sExpiry, LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		if ( expiry == Expiry.TRIAL )
		{
			if ( nConditions == null )
			{
				throw new LicenseException( "No <Conditions> found for trial license", LicenseExceptionType.LICENSE_PARSE_FAILED );
			}

			NamedNodeMap nnm = nConditions.getAttributes();
			Node n = nnm.getNamedItem( "NotBefore" );
			if ( n == null )
				throw new LicenseException( "No NotBefore date found", LicenseExceptionType.LICENSE_PARSE_FAILED );
			String s = n.getNodeValue();
			if ( CommonAppUtils.isNullOrEmptyString( s ) )
			{
				throw new LicenseException( "No NotBefore date found", LicenseExceptionType.LICENSE_PARSE_FAILED );
			}
			dStart = LicenseUtils.expiryString2Date( s );
			if ( dStart == null )
			{
				throw new LicenseException( "Malformed date: " + s, LicenseExceptionType.LICENSE_PARSE_FAILED );
			}

			n = nnm.getNamedItem( "NotAfter" );
			if ( n == null )
				throw new LicenseException( "No NotAfter date found", LicenseExceptionType.LICENSE_PARSE_FAILED );
			s = n.getNodeValue();
			if ( CommonAppUtils.isNullOrEmptyString( s ) )
			{
				throw new LicenseException( "No NotAfter date found", LicenseExceptionType.LICENSE_PARSE_FAILED );
			}
			dEnd = LicenseUtils.expiryString2Date( s );
			if ( dEnd == null )
			{
				throw new LicenseException( "Malformed date: " + s, LicenseExceptionType.LICENSE_PARSE_FAILED );
			}
		}

		int iCount = 0;
		try
		{
			iCount = Integer.parseInt( sCount );
		}
		catch ( NumberFormatException e )
		{
			throw new LicenseException( "Malformed count: " + sCount, LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		ServerLicenseImportType slt = new ServerLicenseImportType();
		slt.m_sLicenseID = sLicenseId;
		slt.m_Type = type;
		slt.m_Expiry = expiry;
		slt.m_dStart = dStart;
		slt.m_dEnd = dEnd;
		slt.m_iCount = iCount;
		return slt;
	}

	public String getLicenseId()
	{
		return m_sLicenseID;
	}

	public LicenseType getType()
	{
		return m_Type;
	}

	public Expiry getExpiry()
	{
		return m_Expiry;
	}

	public Date getStart()
	{
		return m_dStart;
	}

	public Date getEnd()
	{
		return m_dEnd;
	}

	public int getCount()
	{
		return m_iCount;
	}
}
