package com.marchnetworks.health.input;

import com.marchnetworks.command.api.alert.AlertDefinitionEnum;
import com.marchnetworks.common.utils.DateUtils;

public class ServerAlertInput extends AlertInput
{
	private String serverId;

	public ServerAlertInput( AlertDefinitionEnum definition, String sourceId, long timestamp, String info, String value, boolean deviceState )
	{
		super( definition, sourceId, -1L, timestamp, timestamp, info, value, deviceState );

		serverId = "1";
	}

	public ServerAlertInput( AlertDefinitionEnum definition, String sourceId, String info, String value, boolean deviceState )
	{
		this( definition, sourceId, DateUtils.getCurrentUTCTimeInMillis(), info, value, deviceState );
	}

	public String getServerId()
	{
		return serverId;
	}
}
