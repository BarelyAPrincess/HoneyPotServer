package com.marchnetworks.alarm.data;

import com.marchnetworks.alarm.alarmdetails.AlarmDetailEnum;
import com.marchnetworks.command.common.alarm.data.AlarmSourceView;
import com.marchnetworks.common.device.DeletedDeviceData;

import java.util.Set;

public class DeletedSourceAlarmEntry extends AlarmEntryView
{
	private AlarmSourceView deletedAlarmSource;
	private DeletedDeviceData deletedDeviceData;

	public DeletedSourceAlarmEntry()
	{
	}

	public DeletedSourceAlarmEntry( String id, String deviceAlarmEntryId, long firstInstanceTime, long lastInstanceTime, int count, long closedTime, String closedByUser, String closedText, Set<String> handlingUsers, Set<String> associatedChannels, AlarmSourceView deletedAlarmSource, DeletedDeviceData deletedDeviceData, Set<AlarmDetailEnum> alarmDetails )
	{
		super( id, deviceAlarmEntryId, null, firstInstanceTime, lastInstanceTime, count, closedTime, closedByUser, closedText, handlingUsers, associatedChannels, alarmDetails );

		this.deletedAlarmSource = deletedAlarmSource;
		this.deletedDeviceData = deletedDeviceData;
	}

	public AlarmSourceView getDeletedAlarmSource()
	{
		return deletedAlarmSource;
	}

	public void setDeletedAlarmSource( AlarmSourceView deletedAlarmSource )
	{
		this.deletedAlarmSource = deletedAlarmSource;
	}

	public DeletedDeviceData getDeletedDeviceData()
	{
		return deletedDeviceData;
	}

	public void setDeletedDeviceData( DeletedDeviceData deletedDeviceData )
	{
		this.deletedDeviceData = deletedDeviceData;
	}
}
