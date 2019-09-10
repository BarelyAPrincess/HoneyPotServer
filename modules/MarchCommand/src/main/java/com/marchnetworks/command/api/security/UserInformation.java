package com.marchnetworks.command.api.security;

import java.security.Principal;
import java.util.List;

public class UserInformation implements Principal
{
	private String username;
	private List<String> authorities;

	public UserInformation( String username, List<String> authorities )
	{
		this.username = username;
		this.authorities = authorities;
	}

	public List<String> getAuthorities()
	{
		return authorities;
	}

	public void setAuthorities( List<String> authorities )
	{
		this.authorities = authorities;
	}

	public boolean equals( Object object )
	{
		if ( this == object )
		{
			return true;
		}
		if ( ( object instanceof UserInformation ) )
		{
			UserInformation other = ( UserInformation ) object;
			return username.equals( other.getName() );
		}
		return false;
	}

	public int hashCode()
	{
		return username.hashCode();
	}

	public String getName()
	{
		return username;
	}

	public String toString()
	{
		return username;
	}
}
