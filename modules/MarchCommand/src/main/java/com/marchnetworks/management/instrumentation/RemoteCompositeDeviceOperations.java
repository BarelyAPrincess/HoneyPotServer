package com.marchnetworks.management.instrumentation;

import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.management.instrumentation.data.DeviceSubscriptionType;
import com.marchnetworks.management.instrumentation.data.RegistrationState;
import com.marchnetworks.management.instrumentation.model.DeviceCapability;
import com.marchnetworks.server.communications.transport.datamodel.AlarmEntryCloseRecord;
import com.marchnetworks.server.communications.transport.datamodel.AlarmSource;
import com.marchnetworks.server.communications.transport.datamodel.AlertConfig;
import com.marchnetworks.server.communications.transport.datamodel.AlertEntry;
import com.marchnetworks.server.communications.transport.datamodel.AudioOutput;
import com.marchnetworks.server.communications.transport.datamodel.ConfigurationEnvelope;
import com.marchnetworks.server.communications.transport.datamodel.DeviceDetails;
import com.marchnetworks.server.communications.transport.datamodel.DeviceInfo;
import com.marchnetworks.server.communications.transport.datamodel.GenericParameter;
import com.marchnetworks.server.communications.transport.datamodel.Switch;

import java.io.InputStream;
import java.util.List;

public abstract interface RemoteCompositeDeviceOperations extends RemoteDeviceOperations
{
	public abstract String subscribeToDeviceEvents( DeviceSubscriptionType paramDeviceSubscriptionType ) throws DeviceException;

	public abstract void unSubscribeEvents( String paramString ) throws DeviceException;

	public abstract void modifyEventSubscription( String paramString, String[] paramArrayOfString ) throws DeviceException;

	public abstract String register( DeviceSubscriptionType paramDeviceSubscriptionType ) throws DeviceException;

	public abstract DeviceDetails getDeviceDetailsInfo() throws DeviceException;

	public abstract void unregister() throws DeviceException;

	public abstract RegistrationState retrieveRegistrationState() throws DeviceException;

	public abstract DeviceDetails retrieveChildDeviceInfo( String paramString ) throws DeviceException;

	public abstract String upgrade( String paramString1, String paramString2, InputStream paramInputStream, String paramString3 ) throws DeviceException;

	public abstract String upgrade( List<String> paramList, String paramString1, InputStream paramInputStream, String paramString2 ) throws DeviceException;

	public abstract String configure( byte[] paramArrayOfByte ) throws DeviceException;

	public abstract String configure( String paramString, byte[] paramArrayOfByte ) throws DeviceException;

	public abstract ConfigurationEnvelope retrieveChildDeviceConfiguration( String paramString ) throws DeviceException;

	public abstract void fetchEvents() throws DeviceException;

	public abstract DeviceDetails retrieveAllChannelDetails() throws DeviceException;

	public abstract Integer retrieveIntParam( String paramString ) throws DeviceException;

	public abstract List<String> getDeviceCapabilities() throws DeviceException;

	@DeviceCapability( name = "alarms" )
	public abstract List<AlarmSource> getAlarmSources() throws DeviceException;

	@DeviceCapability( name = "alarms" )
	public abstract void closeAlarmEntries( List<AlarmEntryCloseRecord> paramList ) throws DeviceException;

	public abstract long getDeviceTime() throws DeviceException;

	@DeviceCapability( name = "switch" )
	public abstract List<Switch> getSwitches() throws DeviceException;

	@DeviceCapability( name = "audioOut" )
	public abstract List<AudioOutput> getAudioOutputs() throws DeviceException;

	public abstract GenericParameter[] retrieveParamValues( String... paramVarArgs ) throws DeviceException;

	public abstract void setParamValues( GenericParameter[] paramArrayOfGenericParameter ) throws DeviceException;

	@DeviceCapability( name = "alert" )
	public abstract void setAlertConfig( AlertConfig paramAlertConfig ) throws DeviceException;

	@DeviceCapability( name = "alert" )
	public abstract String getAlertConfigId() throws DeviceException;

	@DeviceCapability( name = "alert" )
	public abstract void closeAlerts( List<String> paramList ) throws DeviceException;

	@DeviceCapability( name = "alert" )
	public abstract List<AlertEntry> getAlerts() throws DeviceException;

	public abstract String getSessionIdWithESMToken( String paramString );

	public abstract DeviceInfo getDeviceInfo( boolean paramBoolean1, boolean paramBoolean2 ) throws DeviceException;

	@DeviceCapability( name = "register.2" )
	public abstract void updateRegistrationDetails( List<String> paramList ) throws DeviceException;
}

