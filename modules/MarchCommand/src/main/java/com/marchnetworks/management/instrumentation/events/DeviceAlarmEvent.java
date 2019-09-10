package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.common.transport.data.Pair;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DeviceAlarmEvent extends AbstractDeviceEvent
{
	private String alarmSourceID;
	private String value;
	private DeviceAlarmEventType alarmEventType;

	public DeviceAlarmEvent( DeviceAlarmEventType alarmEventType, String deviceId, long timestamp, String alarmSourceID, String value, Pair[] details )
	{
		super( DeviceAlarmEvent.class.getName(), deviceId, timestamp );
		initMembers( alarmEventType, alarmSourceID, value, details );
	}

	protected DeviceAlarmEvent( String type, DeviceAlarmEventType alarmEventType, String deviceId, long timestamp, String alarmSourceID, String value, Pair[] details )
	{
		super( type, deviceId, timestamp );
		initMembers( alarmEventType, alarmSourceID, value, details );
	}

	private void initMembers( DeviceAlarmEventType alarmEventType, String alarmSourceID, String value, Pair[] details )
	{
		this.alarmEventType = alarmEventType;
		this.alarmSourceID = alarmSourceID;
		this.value = value;

		m_deviceExtraInfo = new HashMap();
		if ( details != null )
		{
			Set<String> associationsList = new LinkedHashSet();
			for ( Pair pair : details )
			{
				String pName = pair.getName();
				String pValue = pair.getValue();

				if ( pName.equals( "assocId" ) )
				{
					associationsList.add( pValue );
				}
				else
				{
					m_deviceExtraInfo.put( pName, pValue );
				}
			}
			if ( !associationsList.isEmpty() )
			{
				m_deviceExtraInfo.put( "assocId", associationsList );
			}
		}
	}

	public String getAlarmSourceID()
	{
		return alarmSourceID;
	}

	public String getValue()
	{
		return value;
	}

	public DeviceAlarmEventType getType()
	{
		return alarmEventType;
	}
}

