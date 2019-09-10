package com.marchnetworks.web.test;

public class MassRegistrationInput
{
	private String ipAddress;

	private String username;

	private String password;

	private String[] path;

	public String getIpAddress()
	{
		return ipAddress;
	}

	public void setIpAddress( String ipAddress )
	{
		this.ipAddress = ipAddress;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername( String username )
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword( String password )
	{
		this.password = password;
	}

	public String[] getPath()
	{
		return path;
	}

	public void setPath( String[] path )
	{
		this.path = path;
	}
}
