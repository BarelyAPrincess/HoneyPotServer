package com.marchnetworks.alarm.task;

import com.marchnetworks.alarm.data.AlarmEntryView;
import com.marchnetworks.alarm.service.AlarmService;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmClosuresDispatchEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEventPool;
import com.marchnetworks.server.communications.transport.datamodel.AlarmEntryCloseRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AlarmClosureRecordsTask implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( AlarmClosureRecordsTask.class );

	private Long deviceId;
	private Collection<AlarmEntryView> closedAlarmEntries;

	public AlarmClosureRecordsTask( Long deviceId, Collection<AlarmEntryView> closedEntries )
	{
		this.deviceId = deviceId;
		closedAlarmEntries = closedEntries;
	}

	public void run()
	{
		if ( ( closedAlarmEntries == null ) || ( closedAlarmEntries.isEmpty() ) )
		{
			LOG.debug( "No alarm entry closures to send to device {}. Aborting task." );
			return;
		}

		List<AlarmEntryCloseRecord> transportCloseRecords = new ArrayList();
		for ( AlarmEntryView alarmEntryClosed : closedAlarmEntries )
		{
			AlarmEntryCloseRecord transportCloseRecord = new AlarmEntryCloseRecord();

			transportCloseRecord.setClosedTime( alarmEntryClosed.getClosedTimeInMillis() );
			transportCloseRecord.setClosedUser( alarmEntryClosed.getClosedByUser() );
			transportCloseRecord.setClosingText( alarmEntryClosed.getClosedText() );
			transportCloseRecord.setEntryId( alarmEntryClosed.getDeviceAlarmEntryId() );

			transportCloseRecords.add( transportCloseRecord );
			LOG.debug( "Added closure dispatch for source {} entry {} closedTime {} ", new Object[] {alarmEntryClosed.getAlarmSourceId(), alarmEntryClosed.getId(), Long.valueOf( alarmEntryClosed.getClosedTime() )} );
		}
		try
		{
			getDeviceService().closeAlarmEntries( deviceId.toString(), transportCloseRecords );

			getAlarmService().markAlarmEntriesReconciled( closedAlarmEntries );
		}
		catch ( DeviceException e )
		{
			LOG.warn( "Error when trying to send Alarm Closures to device {} . Cause : {}", new Object[] {deviceId, e.getMessage()} );

			DeferredEvent de = new DeferredEvent( new DeviceAlarmClosuresDispatchEvent( deviceId.toString() ), ConnectState.ONLINE.toString(), 86400000L );
			getDferredEventPool().add( deviceId.toString(), de );
		}
	}

	private DeviceService getDeviceService()
	{
		return ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
	}

	private AlarmService getAlarmService()
	{
		return ( AlarmService ) ApplicationContextSupport.getBean( "alarmServiceProxy_internal" );
	}

	private DeferredEventPool getDferredEventPool()
	{
		return ( DeferredEventPool ) ApplicationContextSupport.getBean( "deferredEventPool" );
	}
}
