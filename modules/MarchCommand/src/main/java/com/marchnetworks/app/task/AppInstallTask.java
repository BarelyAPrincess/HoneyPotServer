package com.marchnetworks.app.task;

import com.marchnetworks.app.service.AppManager;
import com.marchnetworks.command.common.app.AppException;
import com.marchnetworks.common.spring.ApplicationContextSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppInstallTask implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( AppInstallTask.class );
	private String file;

	public AppInstallTask( String file )
	{
		this.file = file;
	}

	public void run()
	{
		AppManager appManager = ( AppManager ) ApplicationContextSupport.getBean( "appManagerProxy_internal" );
		try
		{
			String id = appManager.install( file );
			appManager.start( id );
		}
		catch ( AppException e )
		{
			LOG.error( "Could not install and start Built-In App, Error: " + e.getMessage() );
		}
	}
}
