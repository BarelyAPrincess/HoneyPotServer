package com.marchnetworks.management.instrumentation;

import com.marchnetworks.command.common.device.data.MassRegistrationInfo;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.transport.data.GenericValue;
import com.marchnetworks.common.device.DeletedDevice;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.management.instrumentation.data.DeviceSubscriptionType;
import com.marchnetworks.management.instrumentation.data.RegistrationAuditInfo;
import com.marchnetworks.management.instrumentation.events.ServerIdHashEvent;
import com.marchnetworks.management.instrumentation.model.CompositeDeviceMBean;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;
import com.marchnetworks.server.communications.transport.datamodel.AlarmEntryCloseRecord;
import com.marchnetworks.server.communications.transport.datamodel.AlarmSource;
import com.marchnetworks.server.communications.transport.datamodel.AlertConfig;
import com.marchnetworks.server.communications.transport.datamodel.AlertEntry;
import com.marchnetworks.server.communications.transport.datamodel.ConfigurationEnvelope;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public abstract interface DeviceService
{
	public abstract void replacementConfigApplied( DeviceMBean paramDeviceMBean ) throws DeviceException;

	public abstract void replacementConfigFailed( String paramString );

	public abstract void handleReplaceFirmwareCheckFailure( DeviceMBean paramDeviceMBean );

	public abstract void retryReplaceDevice( String paramString );

	public abstract void scheduleRetryReplacement( String paramString ) throws DeviceException;

	public abstract void resumeReplacement( DeviceMBean paramDeviceMBean );

	public abstract RegistrationAuditInfo registerDeviceResource( String paramString1, String paramString2, Map<String, Object> paramMap ) throws DeviceException;

	public abstract void beginRegister( String paramString );

	public abstract void register( String paramString, Map<String, Object> paramMap );

	public abstract void retryRegistration( String paramString1, String paramString2, Map<String, Object> paramMap ) throws DeviceException;

	public abstract void replaceDevice( String paramString, Map<String, Object> paramMap );

	public abstract void unregister( DeviceResource paramDeviceResource ) throws DeviceException;

	public abstract void scheduleDeviceUnregistration( DeviceResource paramDeviceResource );

	public abstract void sendLicenseXML( String paramString1, String paramString2 ) throws DeviceException;

	public abstract void addChannelToDevice( String paramString1, String paramString2 );

	public abstract void removeChannelFromDevice( String paramString1, String paramString2 );

	public abstract void updateChannelFromDevice( String paramString1, String paramString2 );

	public abstract void updateChannelState( String paramString1, String paramString2, String paramString3 );

	public abstract String configure( String paramString1, byte[] paramArrayOfByte, String paramString2 ) throws DeviceException;

	public abstract ConfigurationEnvelope retrieveConfiguration( String paramString ) throws DeviceException;

	public abstract String retrieveConfigurationHash( String paramString ) throws DeviceException;

	public abstract String upgrade( String paramString1, String paramString2, String paramString3, InputStream paramInputStream, String paramString4 ) throws DeviceException;

	public abstract String upgrade( String paramString1, List<String> paramList, String paramString2, String paramString3, InputStream paramInputStream, String paramString4 ) throws DeviceException;

	public abstract void startUpdateDeviceAddress( String paramString1, String paramString2 ) throws DeviceException;

	public abstract void markForReplacement( String paramString, Boolean paramBoolean ) throws DeviceException;

	public abstract void updateDeviceAddress( String paramString1, String paramString2 );

	public abstract String convertToDeviceIdFromChannelId( String paramString1, String paramString2 );

	public abstract DeletedDevice createDeletedDevice( String paramString );

	public abstract void removeDeletedDevices( List<Long> paramList );

	public abstract String findChannelNameFromId( String paramString1, String paramString2 );

	public abstract String findRootDevice( String paramString );

	public abstract List<String> findChannelIdsFromDevice( String paramString );

	public abstract void resubscribeDevice( String paramString, Map<String, Object> paramMap );

	public abstract void modifyDeviceSubscription( String paramString, DeviceSubscriptionType paramDeviceSubscriptionType );

	public abstract void addSubscription( String[] paramArrayOfString );

	public abstract void removeSubscription( String[] paramArrayOfString );

	public abstract List<String> findOfflineDevices();

	public abstract void sendDeviceLicense( Long paramLong, String paramString ) throws DeviceException;

	public abstract int grabAllocatedLicenses( Long paramLong ) throws DeviceException;

	public abstract List<CompositeDeviceMBean> getAllCompositeDevices();

	public abstract List<String> findChannelIdsFromDeviceAndChildren( String paramString );

	public abstract List<AlarmSource> getAlarmSources( String paramString ) throws DeviceException;

	public abstract void closeAlarmEntries( String paramString, List<AlarmEntryCloseRecord> paramList ) throws DeviceException;

	public abstract List<CompositeDeviceMBean> findDevicesByDisconnectionTime( int paramInt );

	public abstract void updateDeviceDetails( String paramString ) throws DeviceException;

	public abstract String retrieveAlertConfigId( String paramString ) throws DeviceException;

	public abstract void setAlertConfig( String paramString, AlertConfig paramAlertConfig, boolean paramBoolean ) throws DeviceException;

	public abstract void closeAlerts( String paramString, List<String> paramList ) throws DeviceException;

	public abstract List<AlertEntry> getAlerts( String paramString ) throws DeviceException;

	public abstract void sendServerId( Long paramLong, String paramString, ServerIdHashEvent paramServerIdHashEvent, boolean paramBoolean ) throws DeviceException;

	public abstract void fetchEvents( String paramString1, String paramString2, String paramString3, long paramLong, boolean paramBoolean );

	public abstract void massRegister( List<MassRegistrationInfo> paramList );

	public abstract void stopMassRegistration();

	public abstract void massUpdateTimeDelta( long paramLong );

	public abstract boolean updateDeviceTimeDelta( String paramString, long paramLong );

	public abstract void updateDeviceTimeDeltaAsync( String paramString, long paramLong );

	public abstract GenericValue fetchDeviceParameterValue( String paramString1, String paramString2 );
}

