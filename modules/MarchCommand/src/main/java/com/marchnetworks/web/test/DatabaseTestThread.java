package com.marchnetworks.web.test;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceTestService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseTestThread implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( DatabaseTestThread.class );

	private static final DeviceTestService deviceTestService = ( DeviceTestService ) ApplicationContextSupport.getBean( "deviceTestServiceProxy" );
	private int waitTime;

	public DatabaseTestThread( int waitTime )
	{
		this.waitTime = waitTime;
	}

	public void run()
	{
		try
		{
			deviceTestService.testBlockDatabaseConnection( waitTime );
		}
		catch ( Exception e )
		{
			LOG.error( "Database blocker thread failed, Exception: " + e.getMessage() );
		}
	}
}
