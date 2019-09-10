package com.marchnetworks.health.data;

import com.marchnetworks.command.api.alert.AlertCategoryEnum;
import com.marchnetworks.common.types.AlertSeverityEnum;
import com.marchnetworks.common.types.AlertUserStateEnum;

public class ServerAlertData extends AlertData
{
	private String serverId;
	private String serverType;
	private String serverAddress;

	public ServerAlertData()
	{
	}

	public ServerAlertData( String alertCode, long alertTime, long lastInstanceTime, long count, long alertResolvedTime, boolean deviceState, String sourceId, String sourceDesc, AlertSeverityEnum severity, AlertCategoryEnum category, String info, long id, AlertUserStateEnum userState, long closedTime, String serverId, String serverType, String serverAddress )
	{
		super( alertCode, alertTime, lastInstanceTime, count, alertResolvedTime, deviceState, sourceId, sourceDesc, severity, category, info, id, userState, closedTime );

		this.serverId = serverId;
		this.serverType = serverType;
		this.serverAddress = serverAddress;
	}

	public String getServerId()
	{
		return serverId;
	}

	public void setServerId( String serverId )
	{
		this.serverId = serverId;
	}

	public String getServerType()
	{
		return serverType;
	}

	public void setServerType( String serverType )
	{
		this.serverType = serverType;
	}

	public String getServerAddress()
	{
		return serverAddress;
	}

	public void setServerAddress( String serverAddress )
	{
		this.serverAddress = serverAddress;
	}
}
