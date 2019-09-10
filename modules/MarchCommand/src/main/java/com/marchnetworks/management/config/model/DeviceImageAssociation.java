package com.marchnetworks.management.config.model;

import com.marchnetworks.management.config.DeviceImageState;

public class DeviceImageAssociation
{
	private DeviceImageState currentState = DeviceImageState.UNASSOCIATED;

	public DeviceImageState getCurrentState()
	{
		return currentState;
	}

	public void setCurrentState( DeviceImageState val )
	{
		currentState = val;
	}
}
