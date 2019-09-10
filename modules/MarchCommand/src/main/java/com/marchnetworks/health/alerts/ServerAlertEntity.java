package com.marchnetworks.health.alerts;

import com.marchnetworks.command.api.alert.AlertCategoryEnum;
import com.marchnetworks.common.utils.ServerUtils;
import com.marchnetworks.health.data.AlertData;
import com.marchnetworks.health.data.ServerAlertData;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table( name = "SERVER_ALERT" )
public class ServerAlertEntity extends AlertEntity
{
	private static final long serialVersionUID = 6916425394893905004L;
	@Column( name = "SERVERID" )
	private String serverId;

	public ServerAlertEntity()
	{
	}

	public ServerAlertEntity( String serverId, String alertCode, AlertCategoryEnum category, String sourceId, String sourceDesc, long alertTime, long lastTime, String info )
	{
		super( alertCode, category, sourceId, sourceDesc, alertTime, lastTime, true, info );

		this.serverId = serverId;
	}

	public String getServerId()
	{
		return serverId;
	}

	public AlertData toDataObject()
	{
		return new ServerAlertData( getAlertCode(), getAlertTime(), getLastInstanceTime(), getCount(), getAlertResolvedTime(), getDeviceState(), getSourceId(), getSourceDesc(), getSeverity(), getCategory(), getInfo(), getId(), getUserState(), getLastUserStateChangedTime(), ServerUtils.HOSTNAME_CACHED, "CES", ServerUtils.ADDRESS_CACHED );
	}
}
