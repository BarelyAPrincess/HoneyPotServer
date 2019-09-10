package com.marchnetworks.command.api.security;

import java.util.List;

public class AuthorizationContent
{
	private String resource;
	private List<String> rights;

	public String getResource()
	{
		return resource;
	}

	public void setResource( String resource )
	{
		this.resource = resource;
	}

	public List<String> getRights()
	{
		return rights;
	}

	public void setRights( List<String> rights )
	{
		this.rights = rights;
	}
}
