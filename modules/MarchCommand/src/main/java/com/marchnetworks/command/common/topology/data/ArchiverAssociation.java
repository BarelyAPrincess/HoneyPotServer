package com.marchnetworks.command.common.topology.data;

import javax.xml.bind.annotation.XmlElement;

public class ArchiverAssociation
{
	private Long archiverResourceId;
	private Long[] deviceResourceIds;

	public ArchiverAssociation()
	{
	}

	public ArchiverAssociation( Long archiverResourceId, Long[] deviceResourceIds )
	{
		setArchiverResourceId( archiverResourceId );
		setDeviceResourceIds( deviceResourceIds );
	}

	public Long[] getDeviceResourceIds()
	{
		return deviceResourceIds;
	}

	public void setDeviceResourceIds( Long[] deviceResourceIds )
	{
		this.deviceResourceIds = deviceResourceIds;
	}

	@XmlElement( required = true )
	public Long getArchiverResourceId()
	{
		return archiverResourceId;
	}

	public void setArchiverResourceId( Long archiverResourceId )
	{
		this.archiverResourceId = archiverResourceId;
	}
}
