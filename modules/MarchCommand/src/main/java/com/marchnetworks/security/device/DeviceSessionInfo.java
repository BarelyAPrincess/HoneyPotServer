package com.marchnetworks.security.device;

class DeviceSessionInfo
{
	private String deviceId;

	private String sessionId;

	private long sessionExpirationTime;

	private int sessionTimeoutInSeconds;

	public String getDeviceId()
	{
		return deviceId;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public long getSessionExpirationTime()
	{
		return sessionExpirationTime;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public void setSessionId( String sessionId )
	{
		this.sessionId = sessionId;
	}

	public void setSessionExpirationTime( long sessionExpirationTime )
	{
		this.sessionExpirationTime = sessionExpirationTime;
	}

	public void extendSessionExpirationTime()
	{
		sessionExpirationTime = ( System.currentTimeMillis() + sessionTimeoutInSeconds * 1000L );
	}

	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + ( deviceId == null ? 0 : deviceId.hashCode() );

		return result;
	}

	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		DeviceSessionInfo other = ( DeviceSessionInfo ) obj;
		if ( deviceId == null )
		{
			if ( deviceId != null )
				return false;
		}
		else if ( !deviceId.equals( deviceId ) )
			return false;
		return true;
	}

	public int getSessionTimeoutInSeconds()
	{
		return sessionTimeoutInSeconds;
	}

	public void setSessionTimeoutInSeconds( int sessionTimeoutInSeconds )
	{
		this.sessionTimeoutInSeconds = sessionTimeoutInSeconds;
	}
}

