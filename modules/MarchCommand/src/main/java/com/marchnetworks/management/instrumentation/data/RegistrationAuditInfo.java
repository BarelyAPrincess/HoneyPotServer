package com.marchnetworks.management.instrumentation.data;

public class RegistrationAuditInfo
{
	private String deviceId;

	private RegistrationAuditEnum registrationAuditEnum;

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public RegistrationAuditEnum getRegistrationAuditEnum()
	{
		return registrationAuditEnum;
	}

	public void setRegistrationAuditEnum( RegistrationAuditEnum registrationAuditEnum )
	{
		this.registrationAuditEnum = registrationAuditEnum;
	}
}

