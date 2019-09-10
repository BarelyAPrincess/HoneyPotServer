package com.marchnetworks.management.topology.model;

import com.marchnetworks.alarm.model.AlarmSourceEntity;
import com.marchnetworks.alarm.model.AlarmSourceMBean;
import com.marchnetworks.command.common.alarm.data.AlarmSourceView;
import com.marchnetworks.command.common.topology.data.AlarmSourceResource;
import com.marchnetworks.command.common.topology.data.Resource;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table( name = "ALARM_SOURCE_RESOURCE" )
public class AlarmSourceResourceEntity extends ResourceEntity
{
	@OneToOne( cascade = {javax.persistence.CascadeType.DETACH}, targetEntity = AlarmSourceEntity.class )
	@JoinColumn( name = "ALARM_SOURCE", nullable = false, unique = true )
	private AlarmSourceMBean alarmSource;

	public AlarmSourceResourceEntity()
	{
	}

	public AlarmSourceResourceEntity( AlarmSourceResource dataObject )
	{
		super( dataObject );
	}

	protected Resource newDataObject()
	{
		AlarmSourceResource ret = new AlarmSourceResource();
		AlarmSourceView alarmSourceData = alarmSource.toDataObject();

		ret.setAlarmSource( alarmSourceData );
		ret.setAlarmSourceId( alarmSourceData.getId().toString() );

		return ret;
	}

	public Class<AlarmSourceResource> getDataObjectClass()
	{
		return AlarmSourceResource.class;
	}

	public void setAlarmSource( AlarmSourceMBean alarmSource )
	{
		this.alarmSource = alarmSource;
	}
}

