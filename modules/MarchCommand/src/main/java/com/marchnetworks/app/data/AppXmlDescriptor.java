package com.marchnetworks.app.data;

import com.marchnetworks.common.utils.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AppXmlDescriptor
{
	private String identityFile;
	private String version;
	private String targetSDKVersion;
	private String minimumCESVersion;
	private String serverFile;
	private String clientFile;

	public void parsefromBytes( byte[] xml ) throws AppParseException
	{
		try
		{
			Document document = XmlUtils.getDocumentFromBytes( xml );
			if ( document == null )
			{
				throw new AppParseException( "Could not parse App xml descriptor" );
			}
			Element root = document.getDocumentElement();

			identityFile = getRequiredField( root, "identityFile" );
			version = getRequiredField( root, "version" );
			targetSDKVersion = getRequiredField( root, "targetSDKVersion" );
			minimumCESVersion = getRequiredField( root, "minimumCESVersion" );
			serverFile = XmlUtils.getStringValue( root, "serverFile" );
			clientFile = XmlUtils.getStringValue( root, "clientFile" );
		}
		catch ( Exception e )
		{
			throw new AppParseException( "Could not parse App xml descriptor", e );
		}
	}

	private String getRequiredField( Element root, String field ) throws AppParseException
	{
		String result = XmlUtils.getStringValue( root, field );
		if ( result == null )
		{
			throw new AppParseException( "Missing required field " + field + " in App descriptor" );
		}
		return result;
	}

	public String getIdentityFile()
	{
		return identityFile;
	}

	public void setIdentityFile( String identityFile )
	{
		this.identityFile = identityFile;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion( String version )
	{
		this.version = version;
	}

	public String getTargetSDKVersion()
	{
		return targetSDKVersion;
	}

	public void setTargetSDKVersion( String targetSDKVersion )
	{
		this.targetSDKVersion = targetSDKVersion;
	}

	public String getMinimumCESVersion()
	{
		return minimumCESVersion;
	}

	public void setMinimumCESVersion( String minimumCESVersion )
	{
		this.minimumCESVersion = minimumCESVersion;
	}

	public String getServerFile()
	{
		return serverFile;
	}

	public void setServerFile( String serverFile )
	{
		this.serverFile = serverFile;
	}

	public String getClientFile()
	{
		return clientFile;
	}

	public void setClientFile( String clientFile )
	{
		this.clientFile = clientFile;
	}
}
