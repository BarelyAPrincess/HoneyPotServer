package com.marchnetworks.management.config.service;

public class GroupUpdateView
{
	private Long id;
	private Long groupId;
	private String imageId;

	public GroupUpdateView()
	{
		id = null;
		groupId = null;
		imageId = null;
	}

	public GroupUpdateView( Long groupId )
	{
		id = null;
		this.groupId = groupId;
		imageId = null;
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public Long getGroupId()
	{
		return groupId;
	}

	public void setGroupId( Long groupId )
	{
		this.groupId = groupId;
	}

	public String getImageId()
	{
		return imageId;
	}

	public void setImageId( String id )
	{
		imageId = id;
	}

	public Long getImageIdLong()
	{
		return Long.valueOf( imageId );
	}
}
