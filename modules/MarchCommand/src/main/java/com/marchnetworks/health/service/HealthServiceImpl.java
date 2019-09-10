package com.marchnetworks.health.service;

import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.common.transport.data.Event;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.types.AlertUserStateEnum;
import com.marchnetworks.health.comms.AlertManagerIF;
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

public class HealthServiceImpl implements HealthServiceIF, InitializationListener
{
	private AlertManagerIF alertManager;

	public void onAppInitialized()
	{
		getAlertManager().init();
	}

	public boolean isHealthAlert( Event alertInfoModel )
	{
		return getAlertManager().isHealthAlert( alertInfoModel );
	}

	public DeviceAlertInput createDeviceHealthAlert( String deviceId, Event alertInfoModel, boolean hasCapability )
	{
		return getAlertManager().createDeviceHealthAlert( deviceId, alertInfoModel, hasCapability );
	}

	public DeviceAlertInput createDeviceHealthAlert( String deviceId, AlertEntry alertEntry )
	{
		return getAlertManager().createDeviceHealthAlert( deviceId, alertEntry );
	}

	public void processHealthAlert( AlertInput alertInput )
	{
		getAlertManager().processHealthAlert( alertInput );
	}

	public void processHealthAlerts( List<AlertInput> alertInputs )
	{
		getAlertManager().processHealthAlerts( alertInputs );
	}

	public AlertData[] getOpenAlerts( String userName ) throws HealthFault
	{
		return getAlertManager().getOpenAlerts( userName );
	}

	public void setUserState( long Id, AlertUserStateEnum userState ) throws HealthFault
	{
		getAlertManager().setAlertUserState( Long.valueOf( Id ), userState );
	}

	public void setUserStates( long[] Ids, AlertUserStateEnum userState ) throws HealthFault
	{
		getAlertManager().setAlertUserStates( Ids, userState );
	}

	public AlertSearchResults searchHistoricalClosedAlerts( String userName, AlertSearchQuery searchQuery ) throws HealthFault
	{
		return getAlertManager().searchHistoricalClosedAlerts( userName, searchQuery );
	}

	public AlertData getAlertById( long ID ) throws HealthFault
	{
		return getAlertManager().getAlertById( ID );
	}

	public void purgeOldAlerts( Long days )
	{
		getAlertManager().purgeOldAlerts( days );
	}

	public void processDeviceRegistered( String deviceId, boolean isMassRegister )
	{
		getAlertManager().processDeviceRegistered( deviceId, isMassRegister );
	}

	public void processDeviceUnregistered( String deviceId )
	{
		getAlertManager().processDeviceUnregistered( deviceId );
	}

	public void processDeviceChannelRemoved( String deviceId, String channelId )
	{
		getAlertManager().processDeviceChannelRemoved( deviceId, channelId );
	}

	public void processDeviceReconciliation( String deviceId )
	{
		getAlertManager().processDeviceReconciliation( deviceId );
	}

	public void processAlertClosureDispatch( String deviceId )
	{
		getAlertManager().processAlertClosureDispatch( deviceId );
	}

	public void processDeviceAlertClosure( String deviceId, String deviceAlertId )
	{
		getAlertManager().processDeviceAlertClosure( deviceId, deviceAlertId );
	}

	public void processSetAlertConfig( String deviceId )
	{
		getAlertManager().processSetAlertConfig( deviceId );
	}

	public List<Long> getReferencedDeletedDevices()
	{
		return getAlertManager().getReferencedDeletedDevices();
	}

	public AlertThresholdData[] getAlertThresholds() throws HealthFault
	{
		return getAlertManager().getAlertThresholds();
	}

	public DefaultAlertThresholdData[] getDefaultAlertThresholds() throws HealthFault
	{
		return getAlertManager().getDefaultAlertThresholds();
	}

	public void setAlertThresholds( AlertThresholdData[] alertThreshold ) throws HealthFault
	{
		getAlertManager().setAlertThresholds( alertThreshold );
	}

	public HealthNotificationSummaryData[] getAllAlertsByUser( String userName, long startTime, long endTime )
	{
		return getAlertManager().getAllAlertsByUser( userName, startTime, endTime );
	}

	public void sendAlertClosuresToDevice( String deviceId, List<String> closedAlertIds )
	{
		getAlertManager().sendAlertClosuresToDevice( deviceId, closedAlertIds );
	}

	private AlertManagerIF getAlertManager()
	{
		if ( alertManager == null )
		{
			alertManager = ( ( AlertManagerIF ) ApplicationContextSupport.getBean( "alertManager" ) );
		}
		return alertManager;
	}
}
