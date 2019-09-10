package com.marchnetworks.management.firmware.data;

import javax.xml.bind.annotation.XmlElement;

public class GroupFirmware
{
	private FirmwareGroupEnum group;
	private String firmwareId;

	@XmlElement( required = true )
	public FirmwareGroupEnum getGroup()
	{
		return group;
	}

	public void setGroup( FirmwareGroupEnum group )
	{
		this.group = group;
	}

	public String getTargetFirmwareId()
	{
		return firmwareId;
	}

	public void setTargetFirmwareId( String firmwareId )
	{
		this.firmwareId = firmwareId;
	}
}

