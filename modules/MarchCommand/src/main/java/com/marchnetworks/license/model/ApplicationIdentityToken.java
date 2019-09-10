package com.marchnetworks.license.model;

import com.marchnetworks.common.utils.XmlUtils;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class ApplicationIdentityToken
{
	private AppType appType;
	private String id;
	private String developer;
	private String developerEmail;
	private String developerUrl;
	private String name;
	private String description;
	private String accessCode;

	public ApplicationIdentityToken()
	{
	}

	public void generateId()
	{
		if ( ( id == null ) || ( id.equals( "" ) ) )
			id = UUID.randomUUID().toString();
	}

	@XmlElement( required = true )
	public AppType getAppType()
	{
		return appType;
	}

	public void setAppType( AppType appType )
	{
		this.appType = appType;
	}

	public void setId( String appId )
	{
		id = appId;
	}

	public String getId()
	{
		return id;
	}

	public String getDeveloper()
	{
		return developer;
	}

	public void setDeveloper( String devName )
	{
		developer = devName;
	}

	public String getDeveloperEmail()
	{
		return developerEmail;
	}

	public void setDeveloperEmail( String devEmail )
	{
		developerEmail = devEmail;
	}

	public String getDeveloperUrl()
	{
		return developerUrl;
	}

	public void setDeveloperUrl( String devUrl )
	{
		developerUrl = devUrl;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String appName )
	{
		name = appName;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription( String appDescription )
	{
		description = appDescription;
	}

	@XmlTransient
	public String getAccessCode()
	{
		return accessCode;
	}

	public void setAccessCode( String accessCode )
	{
		this.accessCode = accessCode;
	}

	private String getRequiredField( Element root, String field ) throws LicenseException
	{
		String result = XmlUtils.getStringValue( root, field );
		if ( result == null )
		{
			throw new LicenseException( "No <" + field + "> found in identity token", LicenseExceptionType.LICENSE_PARSE_FAILED );
		}
		return result;
	}

	public ApplicationIdentityToken( Node token ) throws LicenseException
	{
		this( token, true );
	}

	public ApplicationIdentityToken( Node token, boolean hasAccessCode ) throws LicenseException
	{
		Element tokenElement = ( Element ) token;
		appType = AppType.getByValue( getRequiredField( tokenElement, "AppType" ) );
		id = getRequiredField( tokenElement, "AppId" );
		developer = getRequiredField( tokenElement, "Developer" );
		developerEmail = getRequiredField( tokenElement, "DeveloperEmail" );
		developerUrl = getRequiredField( tokenElement, "DeveloperUrl" );
		name = getRequiredField( tokenElement, "AppName" );
		description = getRequiredField( tokenElement, "AppDescription" );
		if ( hasAccessCode )
		{
			accessCode = getRequiredField( tokenElement, "AccessCode" );
		}
	}
}
