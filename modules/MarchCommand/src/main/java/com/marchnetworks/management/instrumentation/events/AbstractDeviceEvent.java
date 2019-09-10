package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.types.DeviceExceptionTypes;

import java.util.Map;

public abstract class AbstractDeviceEvent extends Event
{
	protected String m_deviceId;
	protected transient Map<String, Object> m_deviceExtraInfo;
	protected DeviceExceptionTypes m_DeviceExceptionType;

	public AbstractDeviceEvent( String type, String deviceId )
	{
		super( type );
		m_deviceId = deviceId;
	}

	public AbstractDeviceEvent( String type, String deviceId, long timestamp )
	{
		super( type, timestamp );
		m_deviceId = deviceId;
	}

	public String getDeviceId()
	{
		return m_deviceId;
	}

	public void setDeviceId( String deviceId )
	{
		m_deviceId = deviceId;
	}

	public Map<String, Object> getDeviceExtraInfo()
	{
		return m_deviceExtraInfo;
	}

	public void setDeviceExtraInfo( Map<String, Object> deviceExtraInfo )
	{
		m_deviceExtraInfo = deviceExtraInfo;
	}

	public DeviceExceptionTypes getDeviceExceptionType()
	{
		return m_DeviceExceptionType;
	}

	public void setDeviceExceptionType( DeviceExceptionTypes deviceExceptionType )
	{
		m_DeviceExceptionType = deviceExceptionType;
	}
}

