package com.marchnetworks.license.serverId;

import com.marchnetworks.license.Crypto;
import com.marchnetworks.license.LicenseUtils;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;
import com.marchnetworks.license.exception.ServerIdGenerateException;
import com.marchnetworks.license.serverId.criteria.MAC;
import com.marchnetworks.license.serverId.criteria.UID;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

public class ServerId_v1 extends ServerId
{
	public ServerId_v1()
	{
		m_iVersion = 1;
	}

	public void generate( String CreatedBy ) throws ServerIdGenerateException
	{
		UID u = new UID();
		u.generate();
		gen( u, CreatedBy, null );
	}

	public void generate( UID uid, String CreatedBy, Date creationDate ) throws ServerIdGenerateException
	{
		gen( uid, CreatedBy, creationDate );
	}

	protected void gen( UID uid, String CreatedBy, Date creationDate ) throws ServerIdGenerateException
	{
		Set<Tier> tiers = new TreeSet<Tier>();

		Set<Criterion> c1 = new TreeSet<Criterion>();
		c1.add( uid );
		MAC m = new MAC();
		m.generate();
		c1.add( m );
		Tier t1 = new Tier( 1, c1 );
		tiers.add( t1 );

		if ( creationDate == null )
		{
			Calendar c = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
			creationDate = c.getTime();
		}
		set( creationDate, 1, CreatedBy, tiers );
	}

	public static String findUUIDValue( ServerId sid )
	{
		Iterator<Tier> tiers = sid.getTiers().iterator();
		while ( tiers.hasNext() )
		{
			Tier t = tiers.next();
			Criterion c = t.findCriterion( "UUID" );
			if ( c != null )
			{
				return c.getValue();
			}
		}

		return null;
	}

	public void load( byte[] input, Crypto crypto ) throws LicenseException
	{
		DocuVer dv = decryptAndParseVersion( input, crypto );

		if ( dv.version != 1 )
		{
			throw new LicenseException( "Wrong ServerId version", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		NodeList nl = dv.d.getElementsByTagName( "serverId" );
		if ( nl.getLength() != 1 )
			throw new LicenseException( "Malformed Server Id", LicenseExceptionType.LICENSE_PARSE_FAILED );
		Node n = nl.item( 0 );

		String sDate = "";
		String sCreatedBy = "";
		ArrayList<Node> nTiers = new ArrayList();

		nl = n.getChildNodes();
		for ( int i = 0; i < nl.getLength(); i++ )
		{
			n = nl.item( i );
			String s = n.getNodeName();

			if ( s.equals( "date" ) )
			{
				sDate = n.getTextContent();
			}
			else if ( s.equals( "CreatedBy" ) )
			{
				sCreatedBy = n.getTextContent();
			}
			else if ( s.equals( "tier" ) )
			{
				nTiers.add( n );
			}
		}

		Date dDate = LicenseUtils.SAMLstring2Date( sDate );
		if ( dDate == null )
		{
			throw new LicenseException( "Malformed date: " + sDate, LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		if ( nTiers.size() != 1 )
		{
			throw new LicenseException( "Invalid number of tiers", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		UID cUID = new UID();
		MAC cMAC = new MAC();
		n = ( Node ) nTiers.get( 0 );
		NodeList nlTier1 = n.getChildNodes();

		for ( int i = 0; i < nlTier1.getLength(); i++ )
		{
			n = nlTier1.item( i );

			if ( n.getNodeName().equals( "UUID" ) )
			{
				cUID.fromValue( n.getTextContent() );
			}
			else if ( n.getNodeName().equals( "MAC" ) )
			{

				StringBuilder sb = new StringBuilder( 256 );
				nl = n.getChildNodes();
				for ( int j = 0; j < nl.getLength(); j++ )
				{
					Node nn = nl.item( j );
					if ( nn.getNodeName().equals( "mac" ) )
					{
						sb.append( "<mac>" );
						sb.append( nn.getTextContent() );
						sb.append( "</mac>" );
					}
				}
				String s = sb.toString();
				cMAC.fromValue( s );
			}
		}

		if ( !cUID.isLoaded() )
			throw new LicenseException( "Missing or error loading UID criteria", LicenseExceptionType.LICENSE_PARSE_FAILED );

		if ( !cMAC.isLoaded() )
			throw new LicenseException( "Missing or error loading MAC criteria", LicenseExceptionType.LICENSE_PARSE_FAILED );

		Set<Criterion> sT1 = new TreeSet();
		sT1.add( cUID );
		sT1.add( cMAC );
		Tier t1 = new Tier( 1, sT1 );

		Set<Tier> sTiers = new TreeSet();
		sTiers.add( t1 );

		set( dDate, 1, sCreatedBy, sTiers );
	}

	public boolean isLoaded()
	{
		return m_dDate != null;
	}
}
