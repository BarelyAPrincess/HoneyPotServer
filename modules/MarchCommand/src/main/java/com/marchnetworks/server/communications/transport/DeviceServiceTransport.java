package com.marchnetworks.server.communications.transport;

import com.marchnetworks.command.common.transport.data.Event;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.server.communications.transport.datamodel.AlarmEntryCloseRecord;
import com.marchnetworks.server.communications.transport.datamodel.AlarmSource;
import com.marchnetworks.server.communications.transport.datamodel.AlertConfig;
import com.marchnetworks.server.communications.transport.datamodel.AlertEntry;
import com.marchnetworks.server.communications.transport.datamodel.AudioOutput;
import com.marchnetworks.server.communications.transport.datamodel.Capabilities;
import com.marchnetworks.server.communications.transport.datamodel.ChannelDetails;
import com.marchnetworks.server.communications.transport.datamodel.DeviceDetails;
import com.marchnetworks.server.communications.transport.datamodel.GenericParameter;
import com.marchnetworks.server.communications.transport.datamodel.GetParametersResult;
import com.marchnetworks.server.communications.transport.datamodel.RegistrationDetails;
import com.marchnetworks.server.communications.transport.datamodel.Switch;

import java.net.URL;
import java.util.Map;

public abstract interface DeviceServiceTransport
{
	public static final String EVENT_NAME_SYSTEM_MAXCHANNELSSUPPORTED = "system.maxChannelsSupported";
	public static final String EVENT_NAME_LICENSE_CHANNELS_SOFT_TOTAL = "license.channels.soft.total";
	public static final String CONFIG_HASH_HEADER = "x-hash";
	public static final String TASK_ID_HEADER = "x-id";
	public static final String REASON_HEADER = "x-reason";
	public static final String SESSION_ID_HEADER = "Set-Cookie";
	public static final String SESSION_TIMEOUT_HEADER = "x-session-duration";
	public static final String DATE_HEADER = "Date";
	public static final String CONFIG_URL_PATH_ELEMENT = "config";
	public static final String UPGRADE_URL_PATH_ELEMENT = "upgrade";
	public static final String UPGRADE_URL_NAME_PARAM = "name";
	public static final String UPGRADE_URL_KEY_PARAM = "key";
	public static final String LOGOUT_URL_PATH_ELEMENT = "Logout";
	public static final String INFO_URL_PATH_ELEMENT = "info";
	public static final String PING_URL_PATH_ELEMENT = "Ping";
	public static final String CHANNEL_URL_PATH_ELEMENT = "channel";
	public static final String UPGRADE_URL_CHANNELS_PARAM = "channels";
	public static final String URL_TYPE_PARAM = "type";
	public static final String URL_DEVICE_ID_PARAM = "deviceId";
	public static final String URL_SUBSCRIPTION_ID_PARAM = "id";
	public static final String URL_NOCB_PARAM = "noCB";
	public static final String URL_RESTART_TYPE = "restart";
	public static final String URL_SUBSCRIBE_TYPE = "subscribe";
	public static final String URL_TEST_TYPE = "test";
	public static final String EVENT_DETAILS_CONFIG_HASH = "hash";
	public static final String EVENT_DETAILS_TASK_ID = "taskId";
	public static final String EVENT_DETAILS_REASON = "reason";
	public static final String EVENT_DETAILS_PRESET_NAME = "name";
	public static final String EVENT_DETAILS_PAIR = "details";
	public static final String EVENT_DETAILS_USER_PAIR = "user";
	public static final String EVENT_DETAILS_ADDRESS_PAIR = "address";
	public static final String EVENT_DETAILS_USERNAME = "username";
	public static final String EVENT_DETAILS_IP_ADDRESS = "ipaddress";
	public static final String EVENT_DETAILS_AUDIT_ENTRY_ID = "audit_entry_id";
	public static final String EVENT_DETAILS_MODE = "mode";
	public static final String EVENT_DETAILS_MODE_MANUAL = "manual";
	public static final String EVENT_DETAILS_AUDIT_NAME = "audit_name";
	public static final String EVENT_DETAILS_AUDIT_RESOURCE_NAMES = "resource_names";
	public static final String EVENT_DETAILS = "details";
	public static final String LICENSE_TOTAL_KEY = "total";
	public static final String LICENSE_INUSE_KEY = "inuse";
	public static final String EVENT_VALUE_OK = "ok";
	public static final String EVENT_VALUE_FAILED = "failed";
	public static final String EVENT_VALUE_IN_PROGRESS = "inprogress";
	public static final String EVENT_VALUE_IN_USE = "inuse";
	public static final String EVENT_VALUE_ON = "on";
	public static final String EVENT_VALUE_OFF = "off";
	public static final String NAME_LICENSE_CHANNELS_SOFT_PARAMETER = "license.channels.soft";
	public static final String NAME_LICENSE_CHANNELS_SOFT_TOTAL_PARAMETER = "license.channels.soft.total";
	public static final String SERVER_ID_PARAMETER = "client.server.id";
	public static final String SYSTEM_DEVICE_STATION_ID_PARAMETER = "system.station.id";
	public static final String AGENT_NOTIFY_TIMEOUT_PARAMETER = "agent.svrNotifyTimeoutSec";
	public static final String AGENT_NOTIFY_MAX_PARAMETER = "agent.svrNotifyMaxSec";
	public static final String AGENT_NOTIFY_MIN_PARAMETER = "agent.svrNotifyMinSec";
	public static final String AGENT_NOTIFY_TEST_FREQUENCY_PARAMETER = "agent.svrTestFrequencySec";
	public static final String AGENT_NOTIFY_REACTIVATE_PARAMETER = "agent.svrNotifyReactivateSec";

	public abstract String register( String[] paramArrayOfString1, String paramString1, String paramString2, String[] paramArrayOfString2, String[] paramArrayOfString3 ) throws DeviceException;

	public abstract RegistrationDetails getRegistrationDetails() throws DeviceException;

	public abstract DeviceDetails getSystemDetails() throws DeviceException;

	public abstract void unregister() throws DeviceException;

	public abstract String getConfigHash() throws DeviceException;

	public abstract String subscribeEventsNotify( String[] paramArrayOfString, double paramDouble1, String paramString, double paramDouble2, long paramLong ) throws DeviceException;

	public abstract void modifySubscription( String paramString, String[] paramArrayOfString ) throws DeviceException;

	public abstract Event[] getWaitingEvents( String paramString, double paramDouble ) throws DeviceException;

	public abstract void unsubscribe( String paramString ) throws DeviceException;

	public abstract ChannelDetails[] getAllChannelDetails() throws DeviceException;

	public abstract ChannelDetails getChannelDetails( String paramString ) throws DeviceException;

	public abstract void setCertificateChain( String[] paramArrayOfString ) throws DeviceException;

	public abstract void setParameters( GenericParameter[] paramArrayOfGenericParameter ) throws DeviceException;

	public abstract GetParametersResult getParameters( String[] paramArrayOfString ) throws DeviceException;

	public abstract Capabilities getServiceCapabilities() throws DeviceException;

	public abstract AlarmSource[] getAlarmSources() throws DeviceException;

	public abstract void closeAlarmEntries( AlarmEntryCloseRecord[] paramArrayOfAlarmEntryCloseRecord ) throws DeviceException;

	public abstract AudioOutput[] getAudioOutputs() throws DeviceException;

	public abstract Switch[] getSwitches() throws DeviceException;

	public abstract void configureTransport( URL paramURL, String paramString, int paramInt, Map<String, Object> paramMap );

	public abstract String getAlertConfigId() throws DeviceException;

	public abstract void setAlertConfig( AlertConfig paramAlertConfig ) throws DeviceException;

	public abstract void closeAlerts( String[] paramArrayOfString ) throws DeviceException;

	public abstract AlertEntry[] getAlerts() throws DeviceException;

	public abstract void updateRegistrationDetails( String[] paramArrayOfString ) throws DeviceException;
}

