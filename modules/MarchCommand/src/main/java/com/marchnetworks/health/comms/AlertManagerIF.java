package com.marchnetworks.health.comms;

import com.marchnetworks.command.common.transport.data.Event;
import com.marchnetworks.common.types.AlertUserStateEnum;
import com.marchnetworks.health.data.AlertData;
import com.marchnetworks.health.data.AlertThresholdData;
import com.marchnetworks.health.data.DefaultAlertThresholdData;
import com.marchnetworks.health.data.HealthNotificationSummaryData;
import com.marchnetworks.health.input.AlertInput;
import com.marchnetworks.health.input.DeviceAlertInput;
import com.marchnetworks.health.search.AlertSearchQuery;
import com.marchnetworks.health.search.AlertSearchResults;
import com.marchnetworks.server.communications.transport.datamodel.AlertEntry;
import com.marchnetworks.server.event.health.HealthFault;

import java.util.List;

public interface AlertManagerIF
{
	void init();

	boolean isHealthAlert( Event paramEvent );

	DeviceAlertInput createDeviceHealthAlert( String paramString, Event paramEvent, boolean paramBoolean );

	DeviceAlertInput createDeviceHealthAlert( String paramString, AlertEntry paramAlertEntry );

	void processHealthAlert( AlertInput paramAlertInput );

	void processHealthAlerts( List<AlertInput> paramList );

	AlertData[] getOpenAlerts( String paramString ) throws HealthFault;

	void setAlertUserStates( long[] paramArrayOfLong, AlertUserStateEnum paramAlertUserStateEnum ) throws HealthFault;

	void setAlertUserState( Long paramLong, AlertUserStateEnum paramAlertUserStateEnum ) throws HealthFault;

	AlertSearchResults searchHistoricalClosedAlerts( String paramString, AlertSearchQuery paramAlertSearchQuery ) throws HealthFault;

	AlertData getAlertById( long paramLong ) throws HealthFault;

	void purgeOldAlerts( Long paramLong );

	void processDeviceRegistered( String paramString, boolean paramBoolean );

	void processDeviceUnregistered( String paramString );

	void processDeviceChannelRemoved( String paramString1, String paramString2 );

	void processDeviceReconciliation( String paramString );

	void processAlertClosureDispatch( String paramString );

	void processSetAlertConfig( String paramString );

	void processDeviceAlertClosure( String paramString1, String paramString2 );

	List<Long> getReferencedDeletedDevices();

	AlertThresholdData[] getAlertThresholds() throws HealthFault;

	DefaultAlertThresholdData[] getDefaultAlertThresholds() throws HealthFault;

	void setAlertThresholds( AlertThresholdData[] paramArrayOfAlertThresholdData ) throws HealthFault;

	HealthNotificationSummaryData[] getAllAlertsByUser( String paramString, long paramLong1, long paramLong2 );

	void sendAlertClosuresToDevice( String paramString, List<String> paramList );
}
