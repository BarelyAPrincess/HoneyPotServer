package com.marchnetworks.management.instrumentation.registration;

import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.scheduling.task.TaskSerial;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.BaseDeviceScheduler;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.events.DeviceAddedEvent;
import com.marchnetworks.management.instrumentation.model.CompositeDeviceMBean;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;
import com.marchnetworks.management.instrumentation.task.DeviceAddressUpdater;
import com.marchnetworks.management.instrumentation.task.DeviceRetryReplacementTask;
import com.marchnetworks.server.event.EventListener;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceRegistrationScheduler extends BaseDeviceScheduler implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceRegistrationScheduler.class );

	private static final int MAX_CONCURRENT_REGISTRATIONS = 1;

	public String getListenerName()
	{
		return DeviceRegistrationScheduler.class.getSimpleName();
	}

	public void process( Event event )
	{
		LOG.debug( "Received event {}.", event );

		if ( ( event instanceof DeviceAddedEvent ) )
		{
			DeviceAddedEvent deviceAdded = ( DeviceAddedEvent ) event;

			LOG.debug( "Device {} added.", deviceAdded );
			DeviceMBean device = deviceRegistry.getDevice( deviceAdded.getDeviceId() );
			if ( ( device != null ) && ( ( device instanceof CompositeDeviceMBean ) ) )
			{
				scheduleDeviceRegistration( device.getDeviceId(), deviceAdded.getDeviceExtraInfo(), false );
				LOG.debug( "Scheduled registration for device {}.", deviceAdded.getDeviceId() );
			}
		}
	}

	public void scheduleDeviceUnregistration( DeviceResource deviceResource, Map<String, Object> additionalDeviceRegistrationInfo )
	{
		DeviceUnregistrationTaskImpl task = new DeviceUnregistrationTaskImpl();
		task.setDeviceResource( deviceResource );
		task.setAdditionalDeviceRegistrationInfo( additionalDeviceRegistrationInfo );

		TaskSerial taskSerial = new TaskSerial( task, "generic" );
		taskScheduler.executeAfterTransactionCommits( taskSerial );

		LOG.info( "Scheduled unregistration for device {}.", deviceResource.getDeviceId() );
	}

	public void scheduleDeviceRegistration( String deviceId, Map<String, Object> additionalDeviceRegistrationInfo, boolean massRegistration )
	{
		DeviceRegistrationTaskImpl task = new DeviceRegistrationTaskImpl();
		task.setDeviceId( deviceId );
		task.setAdditionalDeviceRegistrationInfo( additionalDeviceRegistrationInfo );

		if ( massRegistration )
		{
			taskScheduler.executeFixedPool( task, DeviceRegistrationTaskImpl.class.getName(), 1 );
		}
		else
		{
			taskScheduler.executeNow( task );
		}
		LOG.info( "Scheduled registration for device {}.", deviceId );
	}

	public void stopMassRegistration()
	{
		int cancelled = taskScheduler.cancelFixedPool( DeviceRegistrationTaskImpl.class.getName() );
		LOG.info( "Cancelled {} queued mass registration tasks", Integer.valueOf( cancelled ) );
	}

	public void scheduleSerialDeviceRegistration( String deviceId, Map<String, Object> additionalDeviceRegistrationInfo )
	{
		DeviceRegistrationTaskImpl task = new DeviceRegistrationTaskImpl();
		task.setDeviceId( deviceId );
		task.setAdditionalDeviceRegistrationInfo( additionalDeviceRegistrationInfo );

		taskScheduler.executeFixedPoolSerial( task, deviceId );
		LOG.info( "Scheduled re-registration for device {}.", deviceId );
	}

	public void scheduleSerialDeviceReplacement( String deviceId, Map<String, Object> additionalDeviceRegistrationInfo )
	{
		DeviceReplacementTaskImpl task = new DeviceReplacementTaskImpl();
		task.setDeviceId( deviceId );
		task.setAdditionalDeviceRegistrationInfo( additionalDeviceRegistrationInfo );

		taskScheduler.executeFixedPoolSerial( task, deviceId );
	}

	public void scheduleRetryReplacement( String deviceId )
	{
		DeviceRetryReplacementTask task = new DeviceRetryReplacementTask( deviceId );
		taskScheduler.executeFixedPoolSerial( task, deviceId );
		LOG.info( "scheduled retry replacement for device {}.", deviceId );
	}

	public void scheduleSerialDeviceAddressUpdate( String deviceId, String deviceAddress )
	{
		DeviceAddressUpdater task = new DeviceAddressUpdater( deviceId, deviceAddress );
		taskScheduler.executeFixedPoolSerial( task, deviceId );
	}

	public void scheduleChildDeviceRegistration( String deviceId, String channelId )
	{
		ChildDeviceRegistrationTaskImpl task = new ChildDeviceRegistrationTaskImpl();

		task.setDeviceId( deviceId );
		task.setChannelId( channelId );
		taskScheduler.executeFixedPoolSerial( task, deviceId );
		LOG.info( "Scheduled registration for new channel/child device {}.", channelId );
	}
}

