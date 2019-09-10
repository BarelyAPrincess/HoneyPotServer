package com.marchnetworks.license;

import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LicenseUtils
{
	private static final DateFormat EXPIRY_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );
	private static final DateTimeFormatter SAML_FORMAT = DateTimeFormat.forPattern( "yyyy-MM-dd'T'HH:mm:ss'Z'" ).withZoneUTC();
	// private static final FastDateFormat SAML_FORMAT = FastDateFormat.getInstance( "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone( "UTC" ) );

	static
	{
		EXPIRY_FORMAT.setLenient( false );
	}

	public static Date SAMLstring2Date( String s )
	{
		// try
		{
			return SAML_FORMAT.parseDateTime( s ).toDate();

			// DateFormat SAML_FORMAT = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );
			// SAML_FORMAT.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
			// return SAML_FORMAT.parse( s );
		}
		// catch ( ParseException e )
		// {
		// }
		// return null;
	}

	public static String date2SAMLstring( Date d )
	{
		return LocalDateTime.fromDateFields( d ).toString( SAML_FORMAT );
		// return SAML_FORMAT.format( d );
	}

	public static String date2expiryString( Date d )
	{
		if ( d == null )
			return "";
		return EXPIRY_FORMAT.format( d );
	}

	public static Date expiryString2Date( String s )
	{
		try
		{
			return EXPIRY_FORMAT.parse( s );
		}
		catch ( ParseException e )
		{
		}
		return null;
	}

	public static XmlTag findNextTag( String xml, String tag, int start )
	{
		XmlTag result = new XmlTag();
		result.a = -1;

		result.a = xml.indexOf( "<" + tag + ">", start );
		if ( result.a == -1 )
			return result;
		result.b = ( result.a + tag.length() + 2 );

		result.c = xml.indexOf( "</" + tag + ">", start );
		if ( result.c == -1 )
		{
			result.a = -1;
			return result;
		}
		result.d = ( result.c + tag.length() + 3 );

		return result;
	}

	public static String findVersion( Document d, String root ) throws LicenseException
	{
		NodeList nl = d.getChildNodes();
		if ( nl.getLength() != 1 )
			return null;
		Node n = nl.item( 0 );
		String name = n.getNodeName();
		if ( name == null )
			throw new LicenseException( "Root node has no name", LicenseExceptionType.LICENSE_PARSE_FAILED );
		if ( !name.equals( root ) )
			throw new LicenseException( "Root node name is not " + root, LicenseExceptionType.LICENSE_PARSE_FAILED );
		NamedNodeMap nnm = n.getAttributes();
		Node nd = nnm.getNamedItem( "version" );
		if ( nd == null )
			throw new LicenseException( "No version attribute found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		return nd.getNodeValue();
	}

	public static class XmlTag
	{
		public int a;
		public int b;
		public int c;
		public int d;
	}
}
