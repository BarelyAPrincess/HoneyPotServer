package com.marchnetworks.license.model;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.utils.XmlUtils;
import com.marchnetworks.license.LicenseUtils;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Date;

public abstract class LicenseImport
{
	private String licenseId;
	private Expiry expiry;
	private Date start;
	private Date end;
	private int count;

	public LicenseImport( Node nLicense ) throws LicenseException
	{
		String sLicenseId = null;
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
		if ( CommonAppUtils.isNullOrEmptyString( sExpiry ) )
			throw new LicenseException( "No <expiry> found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		if ( CommonAppUtils.isNullOrEmptyString( sCount ) )
		{
			throw new LicenseException( "No <count> found", LicenseExceptionType.LICENSE_PARSE_FAILED );
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

		licenseId = sLicenseId;
		this.expiry = expiry;
		start = dStart;
		end = dEnd;
		count = iCount;
	}

	public static LicenseImport parseServerLicenseImport( Node nLicense ) throws LicenseException
	{
		LicenseImport result = null;

		String type = XmlUtils.getStringValue( ( Element ) nLicense, "type" );
		if ( CommonAppUtils.isNullOrEmptyString( type ) )
		{
			throw new LicenseException( "No <type> found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		LicenseType recordingType = LicenseType.getByValue( type );
		AppLicenseType appType = AppLicenseType.getByValue( type );
		if ( ( recordingType == null ) && ( appType == null ) )
		{
			throw new LicenseException( "Unknown license type: " + type, LicenseExceptionType.LICENSE_PARSE_FAILED );
		}
		if ( recordingType != null )
		{
			result = new RecordingLicenseImport( recordingType, nLicense );
		}
		else
		{
			result = new AppLicenseImport( appType, nLicense );
		}
		return result;
	}

	public abstract String getLicenseTypeName();

	public String getLicenseId()
	{
		return licenseId;
	}

	public void setLicenseId( String licenseId )
	{
		this.licenseId = licenseId;
	}

	public Expiry getExpiry()
	{
		return expiry;
	}

	public void setExpiry( Expiry expiry )
	{
		this.expiry = expiry;
	}

	public boolean isTrialLicense()
	{
		return expiry.equals( Expiry.TRIAL );
	}

	public Date getStart()
	{
		return start;
	}

	public void setStart( Date start )
	{
		this.start = start;
	}

	public Date getEnd()
	{
		return end;
	}

	public void setEnd( Date end )
	{
		this.end = end;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount( int count )
	{
		this.count = count;
	}
}
