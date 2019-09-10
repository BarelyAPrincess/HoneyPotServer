package com.marchnetworks.command.api.rest;

public enum DeviceRestContentType
{
	APPLICATION_OCTET_STREAM( "application/octet-stream" ),
	APPLICATION_XML( "application/xml" ),
	APPLICATION_JSON( "application/json; charset=utf8" ),
	TEXT_PLAIN( "text/plain" );

	private String type;

	private DeviceRestContentType( String type )
	{
		this.type = type;
	}

	public String getType()
	{
		return type;
	}
}
