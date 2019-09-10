package com.marchnetworks.command.common.topology.data;

import com.marchnetworks.command.common.alarm.data.AlarmSourceView;

public class AlarmSourceResource extends Resource
{
	private String alarmSourceId;
	private AlarmSourceView alarmSource;

	public void update( Resource updatedResource )
	{
		if ( ( updatedResource instanceof AlarmSourceResource ) )
		{
			super.update( updatedResource );
			AlarmSourceResource updatedAlarmResource = ( AlarmSourceResource ) updatedResource;
			alarmSourceId = updatedAlarmResource.getAlarmSourceId();
			alarmSource = updatedAlarmResource.getAlarmSource();
		}
	}

	public LinkType getLinkType()
	{
		return LinkType.ALARM_SOURCE;
	}

	public AlarmSourceView getAlarmSource()
	{
		return alarmSource;
	}

	public void setAlarmSource( AlarmSourceView alarmSource )
	{
		this.alarmSource = alarmSource;
	}

	public String getAlarmSourceId()
	{
		return alarmSourceId;
	}

	public void setAlarmSourceId( String alarmSourceId )
	{
		this.alarmSourceId = alarmSourceId;
	}
}
