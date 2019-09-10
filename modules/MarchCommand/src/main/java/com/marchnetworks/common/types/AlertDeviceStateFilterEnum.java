package com.marchnetworks.common.types;

public enum AlertDeviceStateFilterEnum
{
	RESOLVED( AlertDeviceStateEnum.RESOLVED ),
	UNRESOLVED( AlertDeviceStateEnum.UNRESOLVED ),
	ALL( null );

	private AlertDeviceStateEnum m_RealValue = null;

	private AlertDeviceStateFilterEnum( AlertDeviceStateEnum a_RealValue )
	{
		m_RealValue = a_RealValue;
	}

	public AlertDeviceStateEnum getRealValue()
	{
		return m_RealValue;
	}
}
