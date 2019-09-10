package com.marchnetworks.management.instrumentation.model;

import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.transport.data.ConfigurationURL;

import java.util.Map;

public abstract interface DeviceMBean
{
	public abstract String getParentDeviceId();

	public abstract CompositeDeviceMBean getParentDevice();

	public abstract String getDeviceId();

	public abstract String getManufacturer();

	public abstract String getManufacturerName();

	public abstract String getModel();

	public abstract String getModelName();

	public abstract String getSoftwareVersion();

	public abstract String getHardwareVersion();

	public abstract String getSerial();

	public abstract String getRegistrationStatusInString();

	public abstract RegistrationStatus getRegistrationStatus();

	public abstract long getTimeCreatedInMillis();

	public abstract String getTimeRegStatusChangedInString();

	public abstract String getRegistrationErrorMessage();

	public abstract Long getLastCommunicationTimeInMillis();

	public abstract ConnectState getConnectState();

	public abstract Map<String, ChannelMBean> getChannelMBeans();

	public abstract String getSubModel();

	public abstract String getSubModelName();

	public abstract String getFamily();

	public abstract String getFamilyName();

	public abstract String getName();

	public abstract String[] getNames();

	public abstract String getMacAddress();

	public abstract String[] getMacAddresses();

	public abstract String getAddress();

	public abstract String[] getIpAddresses();

	public abstract Integer getChannelsMax();

	public abstract Integer getChannelsInUse();

	public abstract ConfigurationURL getConfigurationUrl();

	public abstract String getPatchList();

	public abstract boolean isR5();

	public abstract boolean isRootDevice();
}

