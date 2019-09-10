package com.marchnetworks.management.firmware.service;

import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.firmware.data.ChannelGroupFirmware;
import com.marchnetworks.management.firmware.data.Firmware;
import com.marchnetworks.management.firmware.data.FirmwareGroupEnum;
import com.marchnetworks.management.firmware.data.GroupFirmware;
import com.marchnetworks.management.firmware.data.UpdateStateEnum;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceConfigurationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateChangeEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;

import java.util.List;

public abstract interface FirmwareService
{
	public abstract void setDeviceFirmwares( Firmware[] paramArrayOfFirmware ) throws FirmwareException;

	public abstract Firmware[] findAllDeviceFirmwares() throws FirmwareException;

	public abstract void setGroupFirmwares( GroupFirmware[] paramArrayOfGroupFirmware ) throws FirmwareException;

	public abstract GroupFirmware[] findAllGroupFirmwares() throws FirmwareException;

	public abstract Firmware getDeviceFirmware( String paramString );

	public abstract GroupFirmware getGroupFirmware( FirmwareGroupEnum paramFirmwareGroupEnum );

	public abstract boolean isFileAssociated( String paramString );

	public abstract void applyFirmwareAsync( Firmware paramFirmware );

	public abstract void applyFirmware( String paramString1, String paramString2, String paramString3 );

	public abstract void applyMultipleChannelFirmware( ChannelGroupFirmware paramChannelGroupFirmware );

	public abstract Firmware getDeviceTargetFirmware( String paramString1, String paramString2 ) throws FirmwareException;

	public abstract void deleteDeviceFirmware( String paramString );

	public abstract void onFirmwareFileRemoved( String paramString );

	public abstract void onFirmwareScheduleStart( Long paramLong );

	public abstract void onFirmwareScheduleEnd( Long paramLong );

	public abstract void onFirmwareScheduleDeleted( Long paramLong );

	public abstract void updateDeviceFirmwareState( String paramString1, UpdateStateEnum paramUpdateStateEnum, String paramString2 );

	public abstract void updateChannelsFirmwareState( List<String> paramList, UpdateStateEnum paramUpdateStateEnum, String paramString );

	public abstract String getDefaultFirmwareVersion( String paramString1, String paramString2 );

	public abstract Long checkGroupFirmware( String paramString );

	public abstract Firmware prepareGroupFirmware( String paramString, Long paramLong );

	public abstract boolean canStartFirmwareUpgradeOnReconnect( String paramString );

	public abstract void handleIPCameraUpgradeEvent( String paramString );

	public abstract void handleUpgradeEvent( String paramString, AbstractDeviceConfigurationEvent paramAbstractDeviceConfigurationEvent );

	public abstract void handleSystemChangedEvent( String paramString );

	public abstract void handleDeferredUpgradeEvent( String paramString );

	public abstract void handleDeviceUpgradeEvent( AbstractDeviceConfigurationEvent paramAbstractDeviceConfigurationEvent, String paramString );

	public abstract void handleDeviceRegistrationEvent( DeviceRegistrationEvent paramDeviceRegistrationEvent, String paramString );

	public abstract void handleConnectionStateChnageEvent( DeviceConnectionStateChangeEvent paramDeviceConnectionStateChangeEvent );

	public abstract void handleScheduleEvent( Event paramEvent );

	public abstract void checkUpgradeTaskTimeout();

	public abstract void removeChanneFromUpgradeTask( String paramString );

	public abstract boolean isUpgradeInProgress( String paramString );
}

