package com.marchnetworks.management.instrumentation.data;

import java.util.StringTokenizer;

public class RegistrationState
{
	private String serverAddress;
	private String contextPath;
	private String deviceId;
	private String assignedDeviceId;
	private long deviceCreateTime;

	public RegistrationState()
	{
	}

	public boolean isRegistered()
	{
		return ( serverAddress != null ) && ( !"".equals( serverAddress ) );
	}

	public RegistrationState( String serverAddress, String contextPath, String assignedDeviceId )
	{
		this.serverAddress = serverAddress;
		this.contextPath = contextPath;
		this.assignedDeviceId = assignedDeviceId;
		parseAssignedDeviceId( assignedDeviceId );
	}

	public String getServerAddress()
	{
		return serverAddress;
	}

	public void setServerAddress( String serverAddress )
	{
		this.serverAddress = serverAddress;
	}

	public String getContextPath()
	{
		return contextPath;
	}

	public void setContextPath( String contextPath )
	{
		this.contextPath = contextPath;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	private void parseAssignedDeviceId( String assignedDeviceId )
	{
		if ( assignedDeviceId != null )
		{
			StringTokenizer st = new StringTokenizer( assignedDeviceId, "__" );
			deviceId = null;
			if ( st.hasMoreTokens() )
			{
				deviceId = st.nextToken();
			}
			if ( st.hasMoreTokens() )
			{
				String deviceCreateTimeString = st.nextToken();
				deviceCreateTime = Long.parseLong( deviceCreateTimeString );
			}
		}
	}

	public long getDeviceCreateTime()
	{
		return deviceCreateTime;
	}

	public void setDeviceCreateTime( long deviceCreateTime )
	{
		this.deviceCreateTime = deviceCreateTime;
	}

	public String getAssignedDeviceId()
	{
		return assignedDeviceId;
	}

	public void setAssignedDeviceId( String assignedDeviceId )
	{
		this.assignedDeviceId = assignedDeviceId;
	}
}

