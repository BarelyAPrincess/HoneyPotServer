package com.marchnetworks.common.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlUtils
{
	private static final Logger LOG = LoggerFactory.getLogger( XmlUtils.class );

	public static String getStringValue( Element element, String childName )
	{
		Node node = element.getElementsByTagName( childName ).item( 0 );
		if ( node != null )
		{
			Node child = node.getFirstChild();
			if ( child != null )
			{
				return child.getNodeValue().trim();
			}
		}
		return null;
	}

	public static Boolean getBooleanValue( Element element, String childName )
	{
		String value = getStringValue( element, childName );
		if ( value != null )
			return Boolean.valueOf( Boolean.parseBoolean( value ) );
		return null;
	}

	public static Integer getIntValue( Element element, String childName )
	{
		String value = getStringValue( element, childName );
		if ( value != null )
			return Integer.valueOf( Integer.parseInt( value ) );
		return null;
	}

	public static Long getLongValue( Element element, String childName )
	{
		String value = getStringValue( element, childName );
		if ( value != null )
			return Long.valueOf( Long.parseLong( value ) );
		return null;
	}

	public static XMLGregorianCalendar getXmlGregorianCalendarFromTime( long timeInMillis )
	{
		DatatypeFactory factory = null;
		try
		{
			factory = DatatypeFactory.newInstance();
		}
		catch ( DatatypeConfigurationException e )
		{
			return null;
		}

		return factory.newXMLGregorianCalendar( DateUtils.getGregorianCalendarFromTimeInMillis( timeInMillis ) );
	}

	public static DocumentBuilder getDocumentBuilder()
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try
		{
			db = dbf.newDocumentBuilder();
		}
		catch ( ParserConfigurationException e )
		{
			LOG.error( "Error getting document builder", e );
		}
		return db;
	}

	public static Document getDocumentFromString( String xmlData, Charset encoding )
	{
		ByteArrayInputStream bais = new ByteArrayInputStream( xmlData.getBytes( encoding ) );
		Document doc = null;
		try
		{
			DocumentBuilder db = getDocumentBuilder();
			if ( db != null )
			{
				doc = db.parse( bais );
			}
		}
		catch ( Exception e )
		{
			LOG.error( "Error getting document from xml string", e );
		}
		return doc;
	}

	public static Document getDocumentFromBytes( byte[] xml )
	{
		ByteArrayInputStream inputStream = new ByteArrayInputStream( xml );
		Document doc = null;
		try
		{
			DocumentBuilder db = getDocumentBuilder();
			if ( db != null )
			{
				doc = db.parse( inputStream );
			}
		}
		catch ( Exception e )
		{
			LOG.error( "Error getting document from xml input stream", e );
		}
		return doc;
	}

	public static String getStringFromDocumentNode( Node xmlNode, Charset encoding )
	{
		String xml = null;
		try
		{
			Transformer xmlTransformer = TransformerFactory.newInstance().newTransformer();
			if ( encoding != null )
			{
				xmlTransformer.setOutputProperty( "encoding", encoding.name() );
			}
			StringWriter writer = new StringWriter();
			xmlTransformer.transform( new DOMSource( xmlNode ), new StreamResult( writer ) );
			xml = writer.toString();
		}
		catch ( TransformerConfigurationException localTransformerConfigurationException )
		{
		}
		catch ( TransformerFactoryConfigurationError localTransformerFactoryConfigurationError )
		{
		}
		catch ( TransformerException localTransformerException )
		{
		}

		return xml;
	}

	public static String getValueFromAttributeMap( String nodeName, NamedNodeMap attributeMap )
	{
		String nodeValue = null;
		if ( ( attributeMap != null ) && ( attributeMap.getNamedItem( nodeName ) != null ) )
		{
			nodeValue = attributeMap.getNamedItem( nodeName ).getNodeValue();
		}
		return nodeValue;
	}

	public static Document getDocumentFromFile( String filepath )
	{
		File f = new File( filepath );
		byte[] xmlBytes;

		if ( !f.exists() )
		{
			return null;
		}
		try
		{
			xmlBytes = FileUtils.readFileToByteArray( f );
		}
		catch ( IOException e1 )
		{
			return null;
		}

		return getDocumentFromBytes( xmlBytes );
	}
}