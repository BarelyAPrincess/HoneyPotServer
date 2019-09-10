package com.marchnetworks.command.api.audit;

public class UserContext
{
	private String userName;

	private String userRemoteAddress;

	public UserContext( String userName, String userRemoteAddress )
	{
		this.userName = userName;
		this.userRemoteAddress = userRemoteAddress;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName( String userName )
	{
		this.userName = userName;
	}

	public String getUserRemoteAddress()
	{
		return userRemoteAddress;
	}

	public void setUserRemoteAddress( String userRemoteAddress )
	{
		this.userRemoteAddress = userRemoteAddress;
	}
}
