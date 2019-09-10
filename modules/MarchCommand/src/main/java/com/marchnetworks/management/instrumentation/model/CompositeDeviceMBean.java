package com.marchnetworks.management.instrumentation.model;

import com.marchnetworks.command.common.transport.data.AddressZone;
import com.marchnetworks.command.common.transport.data.LocalZone;

import java.util.List;
import java.util.Map;

public abstract interface CompositeDeviceMBean extends DeviceMBean
{
	public abstract Map<String, DeviceMBean> getChildDeviceMBeans();

	public abstract String getTimeZoneInfo();

	public abstract Long getTimeDelta();

	public abstract LocalZone getLocalZone();

	public abstract AddressZone[] getUserAddressZones();

	public abstract String getDeviceEventSubscriptionId();

	public abstract Long getDeviceEventSequenceId();

	public abstract String[] getEventSubscriptionPrefixes();

	public abstract String getAssignedDeviceId();

	public abstract String getStationId();

	public abstract Map<String, String> getGlobalSettings();

	public abstract List<String> getCapabilities();

	public abstract Integer getNotifyInterval();
}

