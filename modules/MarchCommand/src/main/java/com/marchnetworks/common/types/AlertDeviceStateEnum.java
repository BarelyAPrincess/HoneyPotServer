package com.marchnetworks.common.types;

public enum AlertDeviceStateEnum
{
	RESOLVED( false ),
	UNRESOLVED( true );

	private boolean m_AlertRaised;

	private AlertDeviceStateEnum( boolean a_AlertRaised )
	{
		m_AlertRaised = a_AlertRaised;
	}

	public boolean isAlertRaised()
	{
		return m_AlertRaised;
	}
}
