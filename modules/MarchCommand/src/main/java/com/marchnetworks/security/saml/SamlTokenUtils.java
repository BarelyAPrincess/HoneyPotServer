package com.marchnetworks.security.saml;

import com.marchnetworks.command.api.security.SamlException;
import com.marchnetworks.command.api.security.SamlException.SamlExceptionTypeEnum;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.utils.DateUtils;

import org.apache.commons.lang.time.FastDateFormat;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SamlTokenUtils
{
	private static Random random = new Random();
	private static final char[] charMapping = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p'};

	private static final FastDateFormat DATE_TIME_FORMAT = FastDateFormat.getInstance( "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone( "UTC" ) );
	public static final Namespace SAMLP_NS = Namespace.getNamespace( "urn:oasis:names:tc:SAML:2.0:protocol" );
	public static final Namespace SAML_NS = Namespace.getNamespace( "urn:oasis:names:tc:SAML:2.0:assertion" );

	public static final String SAMLP_STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";

	public static final String AUTH_DECISION_DEVICE_RESOURCE = "device:";

	public static final String AUTH_DECISION_CHANNEL_RESOURCE = "channel:";

	public static final String AUTH_DECISION_PTZ_RESOURCE = "ptz:";

	public static final String AUTH_DECISION_ALARM_SOURCE_RESOURCE = "alarmsource:";

	public static final String AUTH_DECISION_SWITCH_RESOURCE = "switch:";

	public static final String AUTH_DECISION_AUDIOOUTPUT_RESOURCE = "audiooutput:";

	public static String readFileContents( String path ) throws SamlException
	{
		StringBuffer contents = new StringBuffer();
		BufferedReader input = null;
		try
		{
			input = new BufferedReader( new FileReader( new File( path ) ) );
			String line = null;
			while ( ( line = input.readLine() ) != null )
			{
				contents.append( line );
			}
			input.close();
			return contents.toString();
		}
		catch ( FileNotFoundException e )
		{
			throw new SamlException( "File not found: " + path, SamlExceptionTypeEnum.BAD_REQUEST );
		}
		catch ( IOException e )
		{
			throw new SamlException( "Error reading file: " + path, SamlExceptionTypeEnum.INTERNAL_SERVER_ERROR );
		}
	}

	public static org.w3c.dom.Document toDom( org.jdom.Document doc ) throws SamlException
	{
		try
		{
			XMLOutputter xmlOutputter = new XMLOutputter();
			StringWriter elemStrWriter = new StringWriter();
			xmlOutputter.output( doc, elemStrWriter );
			byte[] xmlBytes = CommonAppUtils.encodeStringToBytes( elemStrWriter.toString() );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware( true );
			return dbf.newDocumentBuilder().parse( new ByteArrayInputStream( xmlBytes ) );
		}
		catch ( IOException | ParserConfigurationException | SAXException e )
		{
			throw new SamlException( "Error converting JDOM document to W3 DOM document: " + e.getMessage(), SamlExceptionTypeEnum.BAD_REQUEST );
		}
	}

	public static org.w3c.dom.Element toDom( org.jdom.Element element ) throws SamlException
	{
		return toDom( element.getDocument() ).getDocumentElement();
	}

	public static org.jdom.Element toJdom( org.w3c.dom.Element e )
	{
		DOMBuilder builder = new DOMBuilder();
		org.jdom.Element jdomElem = builder.build( e );
		return jdomElem;
	}

	public static org.jdom.Document createJdomDoc( String xmlString ) throws SamlException
	{
		try
		{
			SAXBuilder builder = new SAXBuilder();
			return builder.build( new ByteArrayInputStream( CommonAppUtils.encodeStringToBytes( xmlString ) ) );
		}
		catch ( IOException | JDOMException e )
		{
			throw new SamlException( "Error creating JDOM document from XML string: " + e.getMessage(), SamlExceptionTypeEnum.INTERNAL_SERVER_ERROR );
		}
	}

	public static String createID()
	{
		byte[] bytes = new byte[20];
		random.nextBytes( bytes );
		char[] chars = new char[40];
		for ( int i = 0; i < bytes.length; i++ )
		{
			int left = bytes[i] >> 4 & 0xF;
			int right = bytes[i] & 0xF;
			chars[( i * 2 )] = charMapping[left];
			chars[( i * 2 + 1 )] = charMapping[right];
		}
		return String.valueOf( chars );
	}

	public static String getDateAndTime()
	{
		Calendar currentTime = Calendar.getInstance();

		return DATE_TIME_FORMAT.format( currentTime.getTime() );
	}

	public static String getFormattedConditionTime( int timeAdditionInMinutes, Long timeDelta )
	{
		long deviceTime = DateUtils.getCurrentUTCTimeInMillis() + ( timeDelta == null ? 0L : timeDelta.longValue() );
		deviceTime += timeAdditionInMinutes * 60 * 1000;
		return DATE_TIME_FORMAT.format( deviceTime );
	}
}

