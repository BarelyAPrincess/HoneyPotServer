package com.marchnetworks.server.communications.transport.datamodel;

public enum DeviceInfoEnum
{
	SYSTEM_READY( "system.ready" ),
	SYSTEM_READY_CONNECTED( "system.ready.connected" ),
	SYSTEM_READY_DETAILS( "system.ready.details" ),
	SYSTEM_READY_REGISTRATION_DETAILS( "system.ready.registrationDetails" ),
	SYSTEM_READY_SSL( "system.ready.ssl" ),
	SYSTEM_READY_GATEWAY_MOBILE( "system.ready.gateway.mobile" ),
	SYSTEM_DETAILS_ID( "system.details.id" ),
	SYSTEM_DETAILS_MANUFACTURER( "system.details.manufacturer" ),
	SYSTEM_DETAILS_FAMILY( "system.details.family" ),
	SYSTEM_DETAILS_MODEL( "system.details.model" ),
	SYSTEM_DETAILS_SERIAL( "system.details.serial" ),
	SYSTEM_DETAILS_VERSION( "system.details.version" ),
	SYSTEM_REGISTRATION_ENABLED( "system.registration.enabled" ),
	SYSTEM_REGISTRATION_SERVER( "system.registration.server" ),
	SYSTEM_REGISTRATION_DEVICE_ID( "system.registration.deviceId" ),
	SYSTEM_INTERFACE_HTTP_PORT( "system.interface.httpPort" ),
	SYSTEM_INTERFACE_HTTPS_PORT( "system.interface.httpsPort" ),
	SYSTEM_INTERFACE_STREAM_PORT( "system.interface.streamPort" ),
	SYSTEM_INTERFACE_VERSION( "system.interface.version" ),
	SYSTEM_PRODUCT_NAME_PRODUCT_NAME( "system.productname" ),
	AGENT_MEDIA_PORT( "agentmediaport" );

	private String value;

	private DeviceInfoEnum( String value )
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue( String value )
	{
		this.value = value;
	}

	public static DeviceInfoEnum fromValue( String value )
	{
		if ( value != null )
		{
			for ( DeviceInfoEnum def : values() )
			{
				if ( value.equalsIgnoreCase( value ) )
				{
					return def;
				}
			}
		}
		return null;
	}
}

