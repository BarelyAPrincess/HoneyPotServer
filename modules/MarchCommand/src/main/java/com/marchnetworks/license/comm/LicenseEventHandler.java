package com.marchnetworks.license.comm;

import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.license.DeviceLicenseBO;
import com.marchnetworks.license.LicenseService;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateChangeEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRestartEvent;
import com.marchnetworks.management.instrumentation.events.ServerIdHashEvent;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.EventRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseEventHandler implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( LicenseEventHandler.class );

	private DeviceLicenseBO deviceLicenseBO;

	private EventRegistry eventRegistry;

	private LicenseService licenseService;

	public String getListenerName()
	{
		return LicenseEventHandler.class.getSimpleName();
	}

	public void process( Event event )
	{
		LOG.debug( "LicenseEventHandler: process: {}", event );

		if ( ( event instanceof AbstractDeviceEvent ) )
		{
			AbstractDeviceEvent de = ( AbstractDeviceEvent ) event;
			Long deviceId = Long.valueOf( de.getDeviceId() );

			if ( ( de instanceof DeviceRegistrationEvent ) )
			{
				processDeviceRegistrationEvent( ( DeviceRegistrationEvent ) de, deviceId );
			}
			else if ( ( de instanceof DeviceRestartEvent ) )
			{
				processDeviceRestartEvent( ( DeviceRestartEvent ) de, deviceId );
			}
			else if ( ( de instanceof DeviceConnectionStateChangeEvent ) )
			{
				processDeviceConnectionStateChangedEvent( deviceId );
			}

			if ( ( event instanceof ServerIdHashEvent ) )
			{
				processServerIDHashEvent( ( ServerIdHashEvent ) de, deviceId );
			}
		}
	}

	private void processDeviceRegistrationEvent( DeviceRegistrationEvent ev, Long deviceId )
	{
		RegistrationStatus rs = ev.getRegistrationStatus();
		if ( rs.equals( RegistrationStatus.UNREGISTERED ) )
		{
			LOG.info( "UNREGISTERED event for device :" + deviceId );
			deviceLicenseBO.removeDeviceLicense( deviceId );
		}
		else if ( rs.equals( RegistrationStatus.REGISTERED ) )
		{
			long start = System.currentTimeMillis();
			getLicenseService().sendServerId( deviceId, new ServerIdHashEvent( deviceId.toString() ) );
			long end = System.currentTimeMillis();
			LOG.debug( "Post-Registration time for LicenseEventHandler: " + ( end - start ) + " ms." );
		}
	}

	private void processDeviceRestartEvent( DeviceRestartEvent ev, Long deviceId )
	{
		deviceLicenseBO.doCheckDeviceOnline( deviceId, true );
	}

	private void processDeviceConnectionStateChangedEvent( Long deviceId )
	{
		deviceLicenseBO.doCheckDeviceOnline( deviceId, false );
	}

	private void processServerIDHashEvent( ServerIdHashEvent ev, Long deviceId )
	{
		getLicenseService().sendServerId( deviceId, ev );
	}

	public void setDeviceLicenseBO( DeviceLicenseBO deviceLicenseBO )
	{
		this.deviceLicenseBO = deviceLicenseBO;
	}

	public EventRegistry getEventRegistry()
	{
		if ( eventRegistry == null )
		{
			eventRegistry = ( ( EventRegistry ) ApplicationContextSupport.getBean( "eventRegistry" ) );
		}
		return eventRegistry;
	}

	public LicenseService getLicenseService()
	{
		return licenseService;
	}

	public void setLicenseService( LicenseService licenseService )
	{
		this.licenseService = licenseService;
	}
}
