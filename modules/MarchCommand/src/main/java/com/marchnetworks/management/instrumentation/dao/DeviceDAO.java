package com.marchnetworks.management.instrumentation.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;
import com.marchnetworks.management.instrumentation.model.Device;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

public abstract interface DeviceDAO extends GenericDAO<Device, String>
{
	public abstract Device findById( String paramString );

	public abstract Device findByIdEager( String paramString );

	public abstract Device findByIdEagerDetached( String paramString );

	public abstract CompositeDevice findByAddress( String paramString );

	public abstract Set<String> findAllStationIds();

	public abstract CompositeDevice findByStationId( String paramString );

	public abstract Device findByTimeCreated( long paramLong );

	public abstract List<CompositeDevice> findDeviceListFromConnectionTime( int paramInt );

	public abstract Device findByAddressAndParent( String paramString, CompositeDevice paramCompositeDevice );

	public abstract List<CompositeDevice> findAllRegisteredDevices();

	public abstract List<CompositeDevice> findAllRegisteredAndReplacingDevices();

	public abstract Device findByNetworkAddressAndParent( String[] paramArrayOfString, CompositeDevice paramCompositeDevice );

	public abstract List<CompositeDevice> findAllCompositeDevices();

	public abstract Integer updateDeviceCapabilities( String paramString, List<String> paramList );

	public abstract Integer updateLastConnectionTime( String paramString, Calendar paramCalendar );

	public abstract Integer updateRegistrationStatus( String paramString1, RegistrationStatus paramRegistrationStatus, String paramString2 );

	public abstract Integer updateDeviceAddressByDeviceId( String paramString1, String paramString2 );

	public abstract void updateAllTimeDeltas( long paramLong, List<Long> paramList );

	public abstract void updateTimeDelta( String paramString, long paramLong );

	public abstract CompositeDevice findLastTestDevice();
}

