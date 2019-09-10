package com.marchnetworks.license.model;

import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class AppLicenseImport extends LicenseImport
{
	private AppLicenseType type;
	private String feature;
	private boolean commercial;
	private boolean open;
	private List<String> resourceTypes;
	private ApplicationIdentityToken identity;

	public AppLicenseImport( AppLicenseType type, Node nLicense ) throws LicenseException
	{
		super( nLicense );

		Node nConditions = null;
		Node foundToken = null;
		String foundCommercial = "true";
		String foundOpen = "false";
		String foundfeature = null;
		String foundResourceTypes = null;

		NodeList nd = nLicense.getChildNodes();
		for ( int i = 0; i < nd.getLength(); i++ )
		{
			Node n = nd.item( i );
			String s = n.getNodeName();

			if ( s.equals( "Conditions" ) )
			{
				nConditions = n;
			}
			else if ( s.equals( "appIdToken" ) )
			{
				foundToken = n;
			}
			else if ( s.equals( "feature" ) )
			{
				foundfeature = n.getTextContent();
			}
			else if ( s.equals( "resourceTypes" ) )
			{
				foundResourceTypes = n.getTextContent();
			}
		}
		if ( foundToken == null )
		{
			throw new LicenseException( "No <appIdToken> found", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}

		if ( nConditions != null )
		{
			NamedNodeMap nnm = nConditions.getAttributes();

			Node n = nnm.getNamedItem( "Open" );
			if ( n != null )
			{
				foundOpen = n.getNodeValue();
			}

			n = nnm.getNamedItem( "Commercial" );
			if ( n != null )
			{
				foundCommercial = n.getNodeValue();
			}
		}

		this.type = type;
		feature = foundfeature;
		commercial = Boolean.parseBoolean( foundCommercial );
		open = Boolean.parseBoolean( foundOpen );
		if ( foundResourceTypes != null )
		{
			resourceTypes = CollectionUtils.stringToList( foundResourceTypes );
		}
		identity = new ApplicationIdentityToken( foundToken );
	}

	public AppLicenseType getType()
	{
		return type;
	}

	public void setType( AppLicenseType type )
	{
		this.type = type;
	}

	public String getFeature()
	{
		return feature;
	}

	public void setFeature( String feature )
	{
		this.feature = feature;
	}

	public boolean isCommercial()
	{
		return commercial;
	}

	public void setCommercial( boolean commercial )
	{
		this.commercial = commercial;
	}

	public boolean isOpen()
	{
		return open;
	}

	public void setOpen( boolean open )
	{
		this.open = open;
	}

	public List<String> getResourceTypes()
	{
		return resourceTypes;
	}

	public void setResourceTypes( List<String> resourceTypes )
	{
		this.resourceTypes = resourceTypes;
	}

	public ApplicationIdentityToken getIdentity()
	{
		return identity;
	}

	public void setIdentity( ApplicationIdentityToken identity )
	{
		this.identity = identity;
	}

	public String getLicenseTypeName()
	{
		return type.name();
	}
}
