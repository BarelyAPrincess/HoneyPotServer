package com.marchnetworks.server.communications.transport.datamodel;

public class AlarmSource
{
	protected String id;

	protected String type;

	protected String name;

	protected String[] assocIds;

	protected String state;

	protected String extState;

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
	{
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String[] getAssocIds()
	{
		return assocIds;
	}

	public void setAssocIds( String[] assocIds )
	{
		this.assocIds = assocIds;
	}

	public String getState()
	{
		return state;
	}

	public void setState( String state )
	{
		this.state = state;
	}

	public String getExtState()
	{
		return extState;
	}

	public void setExtState( String extState )
	{
		this.extState = extState;
	}
}

