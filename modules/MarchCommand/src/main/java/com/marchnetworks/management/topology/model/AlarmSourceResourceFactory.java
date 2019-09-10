package com.marchnetworks.management.topology.model;

import com.marchnetworks.alarm.model.AlarmSourceMBean;
import com.marchnetworks.alarm.service.AlarmService;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.AlarmSourceResource;
import com.marchnetworks.command.common.topology.data.Resource;

public class AlarmSourceResourceFactory extends AbstractResourceFactory
{
	private AlarmService alarmService;

	public ResourceEntity newResource( Resource resourceData ) throws TopologyException
	{
		AlarmSourceResource alarmResource = ( AlarmSourceResource ) resourceData;

		AlarmSourceResourceEntity resource = new AlarmSourceResourceEntity( alarmResource );
		String alarmSourceId = alarmResource.getAlarmSourceId();

		if ( alarmSourceId != null )
		{
			AlarmSourceMBean alarmSource = alarmService.getAlarmSource( alarmSourceId );
			if ( alarmSource == null )
			{
				throw new TopologyException( TopologyExceptionTypeEnum.ALARM_SOURCE_NOT_FOUND );
			}
			resource.setAlarmSource( alarmSource );
			resource.setName( alarmSource.getName() );
		}
		else
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Null alarm source id." );
		}
		return resource;
	}

	public void onCreateAssociation( ResourceEntity resource, ResourceEntity parentResource ) throws TopologyException
	{
		if ( !( parentResource instanceof DeviceResourceEntity ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Alarm Resource " + resource.getIdAsString() + " cannot be put under " + parentResource.getName() );
		}
	}

	public void setAlarmService( AlarmService alarmService )
	{
		this.alarmService = alarmService;
	}
}

