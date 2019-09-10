package com.marchnetworks.health.task;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.health.service.HealthServiceIF;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AlertClosureTask implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( AlertClosureTask.class );
	private String deviceId;
	private List<String> closedAlertEntries;

	public AlertClosureTask( String deviceId, List<String> closedAlertIds )
	{
		this.deviceId = deviceId;
		closedAlertEntries = closedAlertIds;
	}

	public void run()
	{
		if ( ( closedAlertEntries == null ) || ( closedAlertEntries.isEmpty() ) )
		{
			LOG.debug( "No alert entry closures to send to device {}. Aborting task." );
			return;
		}

		getHealthService().sendAlertClosuresToDevice( deviceId, closedAlertEntries );
	}

	private HealthServiceIF getHealthService()
	{
		return ( HealthServiceIF ) ApplicationContextSupport.getBean( "healthServiceProxy_internal" );
	}
}
