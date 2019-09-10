package com.marchnetworks.security.saml;

import com.marchnetworks.command.api.security.SamlException;
import com.marchnetworks.command.api.security.SamlException.SamlExceptionTypeEnum;

import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.security.AccessControlException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

public class XmlDigitalSigner
{
	private static final String JSR_105_PROVIDER = "org.jcp.xml.dsig.internal.dom.XMLDSigRI";
	private static final String SAML_PROTOCOL_NS_URI_V20 = "urn:oasis:names:tc:SAML:2.0:protocol";

	private static Node getXmlSignatureInsertLocation( org.w3c.dom.Element elem )
	{
		Node insertLocation = null;
		NodeList nodeList = elem.getElementsByTagNameNS( "urn:oasis:names:tc:SAML:2.0:protocol", "Extensions" );
		if ( nodeList.getLength() != 0 )
		{
			insertLocation = nodeList.item( nodeList.getLength() - 1 );
		}
		else
		{
			nodeList = elem.getElementsByTagNameNS( "urn:oasis:names:tc:SAML:2.0:protocol", "Status" );
			insertLocation = nodeList.item( nodeList.getLength() - 1 );
		}
		return insertLocation;
	}

	private static org.jdom.Element signSamlElement( org.jdom.Element element, PrivateKey privKey, PublicKey pubKey ) throws SamlException
	{
		try
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
				{
					signatureMethod = sigFactory.newSignatureMethod( "http://www.w3.org/2000/09/xmldsig#rsa-sha1", null );
				}
				else
				{
					throw new SamlException( "Error signing SAML element: Unsupported type of key", SamlExceptionTypeEnum.BAD_REQUEST );
				}
			}

			CanonicalizationMethod canonicalizationMethod = sigFactory.newCanonicalizationMethod( "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments", ( C14NMethodParameterSpec ) null );

			SignedInfo signedInfo = sigFactory.newSignedInfo( canonicalizationMethod, signatureMethod, Collections.singletonList( ref ) );

			KeyInfoFactory keyInfoFactory = sigFactory.getKeyInfoFactory();
			KeyValue keyValuePair = keyInfoFactory.newKeyValue( pubKey );

			KeyInfo keyInfo = keyInfoFactory.newKeyInfo( Collections.singletonList( keyValuePair ) );

			org.w3c.dom.Element w3cElement = SamlTokenUtils.toDom( element );

			DOMSignContext dsc = new DOMSignContext( privKey, w3cElement );

			Node xmlSigInsertionPoint = getXmlSignatureInsertLocation( w3cElement );
			dsc.setNextSibling( xmlSigInsertionPoint );

			XMLSignature signature = sigFactory.newXMLSignature( signedInfo, keyInfo );
			signature.sign( dsc );

			return SamlTokenUtils.toJdom( w3cElement );
		}
		catch ( Exception e )
		{
			if ( e instanceof IllegalAccessException || e instanceof InstantiationException || e instanceof MarshalException || e instanceof ClassNotFoundException || e instanceof XMLSignatureException || e instanceof KeyException )
			{
				throw new SamlException( "Error signing SAML element: " + e.getMessage(), SamlExceptionTypeEnum.INTERNAL_SERVER_ERROR );
			}
			else if ( e instanceof InvalidAlgorithmParameterException || e instanceof NoSuchAlgorithmException || e instanceof AccessControlException )
			{
				throw new SamlException( "Error signing SAML element: " + e.getMessage(), SamlExceptionTypeEnum.BAD_REQUEST );
			}
			else
				throw new RuntimeException( e );
		}
	}

	public static String signXML( String samlResponse, PublicKey publicKey, PrivateKey privateKey ) throws SamlException
	{
		Document doc = SamlTokenUtils.createJdomDoc( samlResponse );
		if ( doc != null )
		{
			org.jdom.Element signedElement = signSamlElement( doc.getRootElement(), privateKey, publicKey );
			doc.setRootElement( ( org.jdom.Element ) signedElement.detach() );
			XMLOutputter xmlOutputter = new XMLOutputter();
			return xmlOutputter.outputString( doc );
		}
		throw new SamlException( "Error signing SAML Response: Null document", SamlExceptionTypeEnum.BAD_REQUEST );
	}

	public static boolean validateXml( String xml, PublicKey publicKey ) throws SamlException
	{
		org.w3c.dom.Element doc = SamlTokenUtils.toDom( SamlTokenUtils.createJdomDoc( xml ).getRootElement() );

		NodeList nl = doc.getElementsByTagNameNS( "http://www.w3.org/2000/09/xmldsig#", "Signature" );
		if ( nl.getLength() == 0 )
		{
			throw new SamlException( "Cannot find Signature element", SamlExceptionTypeEnum.BAD_REQUEST );
		}

		XMLSignatureFactory fac = XMLSignatureFactory.getInstance( "DOM" );

		DOMValidateContext valContext = new DOMValidateContext( new KeyValueKeySelector(), nl.item( 0 ) );

		XMLSignature signature;

		try
		{
			signature = fac.unmarshalXMLSignature( valContext );
		}
		catch ( MarshalException e )
		{
			throw new SamlException( "Error unmarshalling xml signature.", e, SamlExceptionTypeEnum.INTERNAL_SERVER_ERROR );
		}

		boolean coreValidity = false;
		try
		{
			coreValidity = signature.validate( valContext );

			if ( !coreValidity )
			{
				System.err.println( "Signature failed core validation" );

				boolean sv = signature.getSignatureValue().validate( valContext );

				System.out.println( "signature validation status: " + sv );

				Iterator<?> i = signature.getSignedInfo().getReferences().iterator();
				for ( int j = 0; i.hasNext(); j++ )
				{
					boolean refValid = ( ( Reference ) i.next() ).validate( valContext );
					System.out.println( "ref[" + j + "] validity status: " + refValid );
				}
			}
			else
			{
				System.out.println( "Signature passed core validation" );
			}
		}
		catch ( XMLSignatureException e )
		{
			throw new SamlException( "Error validating xml signature.", e, SamlExceptionTypeEnum.INTERNAL_SERVER_ERROR );
		}

		return coreValidity;
	}

	private static class KeyValueKeySelector extends KeySelector
	{
		public KeySelectorResult select( KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method, XMLCryptoContext context ) throws KeySelectorException
		{
			if ( keyInfo == null )
			{
				throw new KeySelectorException( "Null KeyInfo object!" );
			}
			SignatureMethod sm = ( SignatureMethod ) method;
			List<?> list = keyInfo.getContent();

			for ( int i = 0; i < list.size(); i++ )
			{
				XMLStructure xmlStructure = ( XMLStructure ) list.get( i );
				if ( ( xmlStructure instanceof KeyValue ) )
				{
					PublicKey pk = null;
					try
					{
						pk = ( ( KeyValue ) xmlStructure ).getPublicKey();
					}
					catch ( KeyException ke )
					{
						throw new KeySelectorException( ke );
					}

					if ( algEquals( sm.getAlgorithm(), pk.getAlgorithm() ) )
					{
						return new SimpleKeySelectorResult( pk );
					}
				}
			}
			throw new KeySelectorException( "No KeyValue element found!" );
		}

		static boolean algEquals( String algURI, String algName )
		{
			if ( ( algName.equalsIgnoreCase( "DSA" ) ) && ( algURI.equalsIgnoreCase( "http://www.w3.org/2000/09/xmldsig#dsa-sha1" ) ) )
				return true;
			if ( ( algName.equalsIgnoreCase( "RSA" ) ) && ( algURI.equalsIgnoreCase( "http://www.w3.org/2000/09/xmldsig#rsa-sha1" ) ) )
			{
				return true;
			}
			return false;
		}
	}

	private static class SimpleKeySelectorResult implements KeySelectorResult
	{
		private PublicKey pk;

		SimpleKeySelectorResult( PublicKey pk )
		{
			this.pk = pk;
		}

		public Key getKey()
		{
			return pk;
		}
	}
}

