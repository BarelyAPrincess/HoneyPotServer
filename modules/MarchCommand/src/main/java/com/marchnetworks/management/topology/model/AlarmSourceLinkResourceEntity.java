package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.data.AlarmSourceLinkResource;
import com.marchnetworks.command.common.topology.data.Resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table( name = "ALARM_SOURCE_LINK_RESOURCE" )
public class AlarmSourceLinkResourceEntity extends LinkResourceEntity
{
	@Column( name = "ALARM_SOURCE_ID" )
	private String alarmSourceId;
	@Column( name = "DEVICE_RESOURCE_ID" )
	private Long deviceResourceId;

	public AlarmSourceLinkResourceEntity()
	{
	}

	public AlarmSourceLinkResourceEntity( AlarmSourceLinkResource dataObject )
	{
		super( dataObject );
		alarmSourceId = dataObject.getAlarmSourceId();
		deviceResourceId = dataObject.getDeviceResourceId();
	}

	protected Resource newDataObject()
	{
		AlarmSourceLinkResource ret = new AlarmSourceLinkResource();
		super.newDataObject( ret );
		ret.setAlarmSourceId( alarmSourceId );
		ret.setDeviceResourceId( deviceResourceId );
		return ret;
	}

	public void readFromDataObject( Resource dataObject )
	{
		super.readFromDataObject( dataObject );

		AlarmSourceLinkResource alarmSourceLink = ( AlarmSourceLinkResource ) dataObject;
		setAlarmSourceId( alarmSourceLink.getAlarmSourceId() );
		setDeviceResourceId( alarmSourceLink.getDeviceResourceId() );
	}

	public Class<AlarmSourceLinkResource> getDataObjectClass()
	{
		return AlarmSourceLinkResource.class;
	}

	public String getAlarmSourceId()
	{
		return alarmSourceId;
	}

	public void setAlarmSourceId( String alarmSourceId )
	{
		this.alarmSourceId = alarmSourceId;
	}

	public Long getDeviceResourceId()
	{
		return deviceResourceId;
	}

	public void setDeviceResourceId( Long deviceResourceId )
	{
		this.deviceResourceId = deviceResourceId;
	}

	public boolean isContainer()
	{
		return false;
	}
}

