package com.marchnetworks.management.config.events;

import com.marchnetworks.management.config.service.ConfigView;

import java.util.ArrayList;
import java.util.List;

public class MultipleDeviceConfigApplyNotification extends ConfigNotification
{
	private List<ConfigView> configurationList = new ArrayList();

	private static final long serialVersionUID = -6050275577503749813L;

	public MultipleDeviceConfigApplyNotification( String deviceId, ConfigNotificationType type )
	{
		super( deviceId, type );
	}

	public List<ConfigView> getConfigurationList()
	{
		return configurationList;
	}

	public void setConfigurationList( List<ConfigView> configurationList )
	{
		this.configurationList = configurationList;
	}
}
