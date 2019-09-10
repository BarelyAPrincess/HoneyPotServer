package com.marchnetworks.server.communications.transport.datamodel;

import com.marchnetworks.command.common.CommonAppUtils;

public class DeviceInfo
{
	public boolean ready;
	public boolean readyConnected;
	public boolean readyDetails;
	public boolean readyRegistrationDetails;
	public boolean readySsl;
	public boolean readyGatewayMobile;
	public String id;
	public String manufacturer;
	public String family;
	public String model;
	public String serial;
	public String version;
	public boolean registrationEnabled;
	public String registrationServer;
	public String registrationDeviceId;
	public int httpPort;
	public int httpsPort;
	public int streamPort;
	public String interfaceVersion;
	public String productName;
	public int agentMediaPort;

	public DeviceInfo( String infoString )
	{
		if ( CommonAppUtils.isNullOrEmptyString( infoString ) )
		{
			return;
		}
		String[] infos = infoString.split( "\r\n" );

		for ( String info : infos )
		{
			String[] pair = info.split( ":", 2 );
			String name = pair[0].trim();
			String value = null;
			if ( pair.length > 1 )
			{
				value = pair[1].trim();

				DeviceInfoEnum deviceInfoEnum = DeviceInfoEnum.fromValue( name );
				if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_READY )
				{
					ready = Boolean.parseBoolean( value );
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_READY_CONNECTED )
				{
					readyConnected = Boolean.parseBoolean( value );
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_READY_DETAILS )
				{
					readyDetails = Boolean.parseBoolean( value );
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_READY_REGISTRATION_DETAILS )
				{
					readyRegistrationDetails = Boolean.parseBoolean( value );
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_READY_SSL )
				{
					readySsl = Boolean.parseBoolean( value );
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_READY_GATEWAY_MOBILE )
				{
					readyGatewayMobile = Boolean.parseBoolean( value );
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_DETAILS_ID )
				{
					id = value;
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_DETAILS_MANUFACTURER )
				{
					manufacturer = value;
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_DETAILS_FAMILY )
				{
					family = value;
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_DETAILS_MODEL )
				{
					model = value;
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_DETAILS_SERIAL )
				{
					serial = value;
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_DETAILS_VERSION )
				{
					version = value;
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_REGISTRATION_ENABLED )
				{
					registrationEnabled = Boolean.parseBoolean( value );
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_REGISTRATION_SERVER )
				{
					registrationServer = value;
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_REGISTRATION_DEVICE_ID )
				{
					registrationDeviceId = value;
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_INTERFACE_HTTP_PORT )
				{
					httpPort = Integer.parseInt( value );
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_INTERFACE_HTTPS_PORT )
				{
					httpsPort = Integer.parseInt( value );
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_INTERFACE_STREAM_PORT )
				{
					streamPort = Integer.parseInt( value );
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_INTERFACE_VERSION )
				{
					interfaceVersion = value;
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.SYSTEM_PRODUCT_NAME_PRODUCT_NAME )
				{
					productName = value;
				}
				else if ( deviceInfoEnum == DeviceInfoEnum.AGENT_MEDIA_PORT )
				{
					agentMediaPort = Integer.parseInt( value );
				}
			}
		}
	}
}

