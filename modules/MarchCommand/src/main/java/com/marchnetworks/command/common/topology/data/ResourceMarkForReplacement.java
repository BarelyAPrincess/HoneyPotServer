package com.marchnetworks.command.common.topology.data;

import javax.xml.bind.annotation.XmlElement;

public class ResourceMarkForReplacement
{
	private Long deviceResourceId;
	private boolean markForReplacement;

	public ResourceMarkForReplacement()
	{
	}

	public ResourceMarkForReplacement( Long deviceResourceId, boolean markForReplacement )
	{
		this.deviceResourceId = deviceResourceId;
		this.markForReplacement = markForReplacement;
	}

	@XmlElement( required = true )
	public Long getDeviceResourceId()
	{
		return deviceResourceId;
	}

	public void setDeviceResourceId( Long deviceResourceId )
	{
		this.deviceResourceId = deviceResourceId;
	}

	public boolean isMarkForReplacement()
	{
		return markForReplacement;
	}

	public void setMarkForReplacement( boolean markForReplacement )
	{
		this.markForReplacement = markForReplacement;
	}
}
