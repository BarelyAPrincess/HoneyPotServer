package com.marchnetworks.casemanagementservice.data;

import java.util.List;

public class LocalGroup
{
	private Long id;
	private String name;
	private String description;
	private List<String> usernames;

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription( String description )
	{
		this.description = description;
	}

	public List<String> getUsersnames()
	{
		return usernames;
	}

	public void setUsernames( List<String> usernames )
	{
		this.usernames = usernames;
	}
}
