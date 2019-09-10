package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.common.device.data.MassRegistrationInfo;
import com.marchnetworks.common.event.Event;

import java.util.List;

public class MassRegistrationEvent extends Event
{
	private List<MassRegistrationInfo> massRegistrationInfo;

	public MassRegistrationEvent( List<MassRegistrationInfo> massRegistrationInfo )
	{
		setMassRegistrationInfo( massRegistrationInfo );
	}

	public List<MassRegistrationInfo> getMassRegistrationInfo()
	{
		return massRegistrationInfo;
	}

	public void setMassRegistrationInfo( List<MassRegistrationInfo> massRegistrationInfo )
	{
		this.massRegistrationInfo = massRegistrationInfo;
	}
}

