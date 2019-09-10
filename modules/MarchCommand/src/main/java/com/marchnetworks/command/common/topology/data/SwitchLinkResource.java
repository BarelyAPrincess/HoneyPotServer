package com.marchnetworks.command.common.topology.data;

import javax.xml.bind.annotation.XmlElement;

public class SwitchLinkResource extends LinkResource
{
	private String switchId;
	private Long deviceResourceId;

	public String getSwitchId()
	{
		return switchId;
	}

	public void setSwitchId( String switchId )
	{
		this.switchId = switchId;
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
}
