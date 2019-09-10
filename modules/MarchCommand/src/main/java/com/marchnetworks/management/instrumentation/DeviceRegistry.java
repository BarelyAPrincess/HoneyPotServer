package com.marchnetworks.management.instrumentation;

import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.management.instrumentation.model.ChannelMBean;
import com.marchnetworks.management.instrumentation.model.CompositeDeviceMBean;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract interface DeviceRegistry
{
	public abstract DeviceMBean getDevice( String paramString );

	public abstract DeviceMBean getDeviceEagerDetached( String paramString );

	public abstract Collection<DeviceMBean> getAllRootDevices();

	public abstract <T extends CompositeDeviceMBean> T getDeviceByAddress( String paramString );

	public abstract <T extends CompositeDeviceMBean> T getDeviceByStationId( String paramString );

	public abstract Set<String> findAllStationIds();

	public abstract DeviceMBean getDeviceByTime( long paramLong );

	public abstract void removeDevice( String paramString );

	public abstract void removeDeviceChannels( String paramString );

	public abstract ChannelMBean getChannel( Long paramLong );

	public abstract DeviceMBean getDeviceByChannel( String paramString1, String paramString2 );

	public abstract ConnectState getConnectState( String paramString );

	public abstract void updateConnectState( String paramString, ConnectState paramConnectState );

	public abstract void putConnectState( String paramString, ConnectState paramConnectState );

	public abstract void updateLastConnectionTime( String paramString, Calendar paramCalendar );

	public abstract void updateDeviceCapabilities( String paramString, List<String> paramList );
}

