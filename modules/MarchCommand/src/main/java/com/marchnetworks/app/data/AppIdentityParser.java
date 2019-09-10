package com.marchnetworks.app.data;

import com.marchnetworks.command.common.Base64;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.XmlUtils;
import com.marchnetworks.license.LicenseService;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.model.ApplicationIdentityToken;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AppIdentityParser
{
	public static ApplicationIdentityToken parseIdentity( byte[] xml ) throws AppParseException
	{
		Document document = XmlUtils.getDocumentFromBytes( xml );
		if ( document == null )
		{
			throw new AppParseException( "Could not parse App identity xml" );
		}
		Element root = document.getDocumentElement();

		String signature = XmlUtils.getStringValue( root, "signature" );
		boolean signatureValid = false;
		try
		{
			LicenseService licenseService = ( LicenseService ) ApplicationContextSupport.getBean( "licenseService_internal" );
			signatureValid = licenseService.validate( getContentString( xml ), Base64.decode( signature ) );
		}
		catch ( Exception e )
		{
			throw new AppParseException( "Signature check failed in App identity", e );
		}
		if ( !signatureValid )
		{
			throw new AppParseException( "Signature check failed in App identity" );
		}

		NodeList contentList = root.getElementsByTagName( "content" );
		Node content = contentList.item( 0 );
		ApplicationIdentityToken identity;

		try
		{
			identity = new ApplicationIdentityToken( content, false );
		}
		catch ( LicenseException e )
		{
			throw new AppParseException( e.getMessage(), e );
		}

		return identity;
	}

	public static String getContentString( byte[] xml )
	{
		String xmlString = CommonAppUtils.encodeToUTF8String( xml );
		int i = xmlString.indexOf( "<content>" ) + 9;
		int j = xmlString.indexOf( "</content>" );
		return xmlString.substring( i, j );
	}
}
