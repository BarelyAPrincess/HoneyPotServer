package com.marchnetworks.command.common.extractor.data;

public class RecorderAuth
{
	private String AuthType;

	private String AuthData;

	public RecorderAuth()
	{
	}

	public RecorderAuth( String authType, String token )
	{
		AuthType = authType;
		AuthData = token;
	}

	public String getAuthType()
	{
		return AuthType;
	}

	public void setAuthType( String authType )
	{
		AuthType = authType;
	}

	public String getAuthData()
	{
		return AuthData;
	}

	public void setAuthData( String authData )
	{
		AuthData = authData;
	}
}
