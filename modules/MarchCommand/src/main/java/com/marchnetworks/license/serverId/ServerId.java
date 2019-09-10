package com.marchnetworks.license.serverId;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.license.Crypto;
import com.marchnetworks.license.LicenseUtils;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;
import com.marchnetworks.license.exception.ServerIdGenerateException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public abstract class ServerId
{
	protected Date m_dDate;
	protected int m_iVersion;
	protected String m_sCreatedBy;
	protected Set<Tier> m_sTiers;

	public ServerId()
	{
		m_dDate = null;
		m_iVersion = 0;
		m_sCreatedBy = "";
		m_sTiers = null;
	}

	public Date getDate()
	{
		return m_dDate;
	}

	public String getCreatedBy()
	{
		return m_sCreatedBy;
	}

	public Set<Tier> getTiers()
	{
		return m_sTiers;
	}

	public int getVersion()
	{
		return m_iVersion;
	}

	public Criterion getCriterion( String name )
	{
		Criterion Result = null;
		for ( Tier t : m_sTiers )
		{
			Result = t.findCriterion( name );
			if ( Result != null )
				return Result;
		}
		return Result;
	}

	public boolean IsSameServer( ServerId B )
	{
		return IsSameServer( this, B );
	}

	public static boolean IsSameServer( ServerId serverIdA, ServerId serverIdB )
	{
		if ( serverIdA.m_iVersion != serverIdB.m_iVersion )
		{
			return false;
		}

		if ( serverIdA.m_sTiers.size() != serverIdB.m_sTiers.size() )
		{
			return false;
		}

		Iterator<Tier> ia = serverIdA.m_sTiers.iterator();
		Iterator<Tier> ib = serverIdB.m_sTiers.iterator();

		int level = 1;
		double score = 0.0D;
		for ( level = 1; level <= serverIdA.m_sTiers.size(); level++ )
		{
			Tier ta = ia.next();
			Tier tb = ib.next();

			if ( level == 1 )
			{
				if ( !ta.equals( tb ) )
				{
					return false;
				}
			}
			else
			{
				score += ta.computeDifferenceScore( level, tb );
			}

			if ( score >= 1.0D )
			{
				return false;
			}
			level++;
		}

		return true;
	}

	public abstract void generate( String paramString ) throws ServerIdGenerateException;

	public byte[] export( Crypto crypto ) throws Exception
	{
		String xml = buildXml();
		return crypto.cmdSymEncrypt( xml.getBytes( "UTF-8" ) );
	}

	public abstract void load( byte[] paramArrayOfByte, Crypto paramCrypto ) throws LicenseException;

	public abstract boolean isLoaded();

	protected void set( Date d, int version, String createdBy, Set<Tier> tiers )
	{
		m_dDate = d;
		m_iVersion = version;
		m_sCreatedBy = createdBy;
		m_sTiers = tiers;
	}

	protected String buildXml()
	{
		StringBuilder sb = new StringBuilder();

		sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );

		sb.append( "<serverId version=\"" );
		sb.append( m_iVersion );
		sb.append( "\">\n" );

		sb.append( "\t<date>" );
		sb.append( LicenseUtils.date2SAMLstring( m_dDate ) );
		sb.append( "\t</date>\n" );

		sb.append( "\t<CreatedBy>" );
		sb.append( m_sCreatedBy );
		sb.append( "</CreatedBy>\n" );

		for ( Tier t : m_sTiers )
		{
			sb.append( t.toXml() );
		}
		sb.append( "</serverId>\n" );
		return sb.toString();
	}

	protected static DocuVer decryptAndParseVersion( byte[] input, Crypto crypto ) throws LicenseException
	{
		Document d = null;
		byte[] bXml;

		try
		{
			bXml = crypto.cmdSymDecrypt( input );
		}
		catch ( Exception e )
		{
			throw new LicenseException( "ServerId encoding error", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		String sXml = CommonAppUtils.encodeToUTF8String( bXml );
		if ( sXml == null )
		{
			throw new LicenseException( "ServerId encoding error", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			DocumentBuilder db = factory.newDocumentBuilder();
			InputSource inStream = new InputSource();
			inStream.setCharacterStream( new StringReader( sXml ) );
			d = db.parse( inStream );
		}
		catch ( Exception e )
		{
			throw new LicenseException( "ServerId encoding error", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		String sVersion;
		int iVersion;

		try
		{
			sVersion = LicenseUtils.findVersion( d, "serverId" );
		}
		catch ( LicenseException e )
		{
			throw new LicenseException( "No Server Id version found", LicenseExceptionType.LICENSE_PARSE_FAILED, e );
		}

		try
		{
			iVersion = Integer.parseInt( sVersion );
		}
		catch ( NumberFormatException e )
		{
			throw new LicenseException( "Version number error: " + sVersion, LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		DocuVer dv = new DocuVer();
		dv.d = d;
		dv.version = iVersion;

		return dv;
	}

	protected static class DocuVer
	{
		Document d;
		int version;
	}
}
