package com.marchnetworks.license;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.jdom.DefaultJDOMFactory;
import org.jdom.JDOMFactory;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.security.auth.x500.X500PrivateCredential;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;

public class DeviceLicenseGenEngine
{
	static
	{
		Init.init();
	}

	private static Logger LOG = LoggerFactory.getLogger( DeviceLicenseGenEngine.class );
	private static final String JSR_105_PROVIDER = "org.jcp.xml.dsig.internal.dom.XMLDSigRI";
	private static final String LICENSE_PROTOCOL_NS_URI = "marchnetworks:license:device";
	private static final FastDateFormat DATE_TIME_FORMAT = FastDateFormat.getInstance( "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone( "UTC" ) );

	private final Namespace CONTENT_NS = Namespace.getNamespace( "marchnetworks:license:content:device" );

	private final Namespace TYPES_NS = Namespace.getNamespace( "marchnetworks:license:content:device:types" );

	private X500PrivateCredential m_X500PrivateCredential;

	public DeviceLicenseGenEngine( X500PrivateCredential pc )
	{
		m_X500PrivateCredential = pc;
	}

	public String generateSignedLicense( String expiry, String deviceID, String licenseCount, String typeName, String notBefore, String notAfter )
	{
		String Result = null;
		try
		{
			org.jdom.Document doc = createLicenseResponse( expiry, deviceID, typeName, licenseCount, notBefore, notAfter );
			String xml = signXML( doc, m_X500PrivateCredential.getCertificate().getPublicKey(), m_X500PrivateCredential.getPrivateKey() );

			Result = canonicalLicense( xml );

		}
		catch ( Exception e )
		{
			LOG.error( "Critical error generating device license: ", e );
		}
		return Result;
	}

	private String canonicalLicense( String xml ) throws Exception
	{
		Canonicalizer canonicalizer = Canonicalizer.getInstance( "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments" );
		byte[] canon = canonicalizer.canonicalize( xml.getBytes() );
		return new String( canon );
	}

	private org.jdom.Document createLicenseResponse( String expiry, String deviceID, String typeName, String licenseCount, String notBefore, String notOnOrAfter ) throws Exception
	{
		StringBuffer sb = new StringBuffer( 512 );
		sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<license version=\"1.0\" xmlns=\"marchnetworks:license:device\">\n\t<content xmlns=\"marchnetworks:license:content:device\">\n\t\t<issuer>March Networks Command License Authority</issuer>\n\t\t<licenseType>" );

		sb.append( expiry );
		sb.append( "</licenseType>\n\t\t<licenseID>" );
		sb.append( String.valueOf( BigInteger.valueOf( System.currentTimeMillis() ) ) );
		sb.append( "</licenseID>\n\t\t<date>" );
		sb.append( getDateAndTime() );
		sb.append( "</date>\n\t\t<deviceID>" );
		sb.append( deviceID );
		sb.append( "</deviceID>\n\t</content>\n</license>" );

		org.jdom.Document responseDoc = createJdomDoc( sb.toString() );
		org.jdom.Element content = responseDoc.getRootElement().getChild( "content", CONTENT_NS );

		JDOMFactory factory = new DefaultJDOMFactory();
		org.jdom.Element types = factory.element( "types", TYPES_NS );
		content.addContent( types );
		org.jdom.Element type = factory.element( "type" );
		type.setAttribute( "Decision", "granted" );
		types.addContent( type );

		org.jdom.Element name = factory.element( "Name" );
		name.setText( typeName );
		type.addContent( name );

		org.jdom.Element count = factory.element( "Count" );
		count.setText( licenseCount );
		type.addContent( count );

		org.jdom.Element conditions = factory.element( "Conditions" );
		conditions.setAttribute( "NotBefore", notBefore );
		conditions.setAttribute( "NotAfter", notOnOrAfter );
		type.addContent( conditions );

		return responseDoc;
	}

	protected static String signXML( org.jdom.Document doc, PublicKey pub, PrivateKey priv ) throws Exception
	{
		org.jdom.Element signedElement = signElement( doc.getRootElement(), priv, pub );
		doc.setRootElement( ( org.jdom.Element ) signedElement.detach() );

		XMLOutputter xmlOutputter = new XMLOutputter();
		return xmlOutputter.outputString( doc );
	}

	private static org.jdom.Element signElement( org.jdom.Element element, PrivateKey privKey, PublicKey pubKey ) throws Exception
	{
		String providerName = System.getProperty( "jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI" );
		XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance( "DOM", ( Provider ) Class.forName( providerName ).newInstance() );

		List<Transform> envelopedTransform = Collections.singletonList( sigFactory.newTransform( "http://www.w3.org/2000/09/xmldsig#enveloped-signature", ( TransformParameterSpec ) null ) );

		Reference ref = sigFactory.newReference( "", sigFactory.newDigestMethod( "http://www.w3.org/2000/09/xmldsig#sha1", null ), envelopedTransform, null, null );

		SignatureMethod signatureMethod;

		if ( ( pubKey instanceof DSAPublicKey ) )
		{
			signatureMethod = sigFactory.newSignatureMethod( "http://www.w3.org/2000/09/xmldsig#dsa-sha1", null );
		}
		else
		{
			if ( ( pubKey instanceof RSAPublicKey ) )
				signatureMethod = sigFactory.newSignatureMethod( "http://www.w3.org/2000/09/xmldsig#rsa-sha1", null );
			else
				throw new Exception( "Error signing license element: Unsupported type of key" );
		}

		CanonicalizationMethod canonicalizationMethod = sigFactory.newCanonicalizationMethod( "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments", ( C14NMethodParameterSpec ) null );

		SignedInfo signedInfo = sigFactory.newSignedInfo( canonicalizationMethod, signatureMethod, Collections.singletonList( ref ) );

		KeyInfoFactory keyInfoFactory = sigFactory.getKeyInfoFactory();
		KeyValue keyValuePair = keyInfoFactory.newKeyValue( pubKey );

		KeyInfo keyInfo = keyInfoFactory.newKeyInfo( Collections.singletonList( keyValuePair ) );

		org.w3c.dom.Element w3cElement = toDom( element );

		DOMSignContext dsc = new DOMSignContext( privKey, w3cElement );

		Node xmlSigInsertionPoint = getXmlSignatureInsertLocation( w3cElement );
		dsc.setNextSibling( xmlSigInsertionPoint );

		XMLSignature signature = sigFactory.newXMLSignature( signedInfo, keyInfo );

		signature.sign( dsc );

		return toJdom( w3cElement );
	}

	private static Node getXmlSignatureInsertLocation( org.w3c.dom.Element elem )
	{
		Node insertLocation = null;
		NodeList nodeList = elem.getElementsByTagNameNS( "marchnetworks:license:device", "*" );

		if ( nodeList.getLength() != 0 )
		{
			insertLocation = nodeList.item( nodeList.getLength() - 1 );
		}
		else
		{
			insertLocation = nodeList.item( 0 );
		}
		return insertLocation;
	}

	protected static org.jdom.Document createJdomDoc( String xmlString ) throws Exception
	{
		SAXBuilder builder = new SAXBuilder();
		org.jdom.Document doc = builder.build( new ByteArrayInputStream( xmlString.getBytes() ) );
		return doc;
	}

	protected static org.jdom.Element toJdom( org.w3c.dom.Element e )
	{
		DOMBuilder builder = new DOMBuilder();
		org.jdom.Element jdomElem = builder.build( e );
		return jdomElem;
	}

	protected static String getDateAndTime()
	{
		Date date = new Date();
		return DATE_TIME_FORMAT.format( date );
	}

	protected static org.w3c.dom.Document toDom( org.jdom.Document doc ) throws Exception
	{
		XMLOutputter xmlOutputter = new XMLOutputter();
		StringWriter elemStrWriter = new StringWriter();
		xmlOutputter.output( doc, elemStrWriter );
		byte[] xmlBytes = elemStrWriter.toString().getBytes();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware( true );
		return dbf.newDocumentBuilder().parse( new ByteArrayInputStream( xmlBytes ) );
	}

	protected static org.w3c.dom.Element toDom( org.jdom.Element element ) throws Exception
	{
		return toDom( element.getDocument() ).getDocumentElement();
	}
}
