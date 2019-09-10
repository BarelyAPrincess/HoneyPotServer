package com.marchnetworks.command.common.topology.data;

import javax.xml.bind.annotation.XmlElement;

public class ResourcePathNode
{
	private Long id;
	private String name;

	public ResourcePathNode( Long id, String name )
	{
		this.id = id;
		this.name = name;
	}

	@XmlElement( required = true )
	public Long getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public void setName( String name )
	{
		this.name = name;
	}
}
