package com.marchnetworks.audit.data;

public class AuditSearchQuery
{
	private String userRemoteAddress;

	private String[] usernames;

	private String[] eventNames;

	private long startTime;
	private long endTime;
	private Long[] resourceIds;

	public String getUserRemoteAddress()
	{
		return userRemoteAddress;
	}

	public void setUserRemoteAddress( String userRemoteAddress )
	{
		this.userRemoteAddress = userRemoteAddress;
	}

	public String[] getUsernames()
	{
		return usernames;
	}

	public void setUsernames( String[] usernames )
	{
		this.usernames = usernames;
	}

	public String[] getEventNames()
	{
		return eventNames;
	}

	public void setEventNames( String[] eventNames )
	{
		this.eventNames = eventNames;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime( long startTime )
	{
		this.startTime = startTime;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void setEndTime( long endTime )
	{
		this.endTime = endTime;
	}

	public Long[] getResourceIds()
	{
		return resourceIds;
	}

	public void setResourceIds( Long[] resourceIds )
	{
		this.resourceIds = resourceIds;
	}
}
