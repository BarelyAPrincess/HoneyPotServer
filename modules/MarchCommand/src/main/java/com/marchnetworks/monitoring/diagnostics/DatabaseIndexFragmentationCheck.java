package com.marchnetworks.monitoring.diagnostics;

import com.marchnetworks.app.service.OsgiService;
import com.marchnetworks.command.api.diagnostics.IndexFragmentationService;
import com.marchnetworks.command.common.diagnostics.IndexFragmentation;
import com.marchnetworks.common.diagnostics.database.SystemInfoService;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.diagnostics.metrics.MetricsTypes;
import com.marchnetworks.common.spring.ApplicationContextSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DatabaseIndexFragmentationCheck
{
	private static final Logger LOG = LoggerFactory.getLogger( DatabaseIndexFragmentationCheck.class );

	private SystemInfoService systemInfoService;
	private OsgiService osgiService;

	public void checkIndex()
	{
		List<IndexFragmentation> indexResults = systemInfoService.getCommandIndexFragmentation( CommandIndexTableNames.getAllTableNames() );

		for ( IndexFragmentation index : indexResults )
		{
			MetricsHelper.metrics.addBucketValue( MetricsTypes.FRAGMENTATION.getName(), index.getTableName() + " " + index.getIndexName(), Double.valueOf( index.getAverageFragmentation() ).longValue() );
			LOG.info( "DatabaseIndex : command." + index.getTableName() + "." + index.getIndexName() + " : " + index.getAverageFragmentation() );
		}

		List<IndexFragmentationService> indexChecks = getOsgiService().getServices( IndexFragmentationService.class );
		for ( IndexFragmentationService indexCheck : indexChecks )
		{
			List<IndexFragmentation> appsIndexResults = indexCheck.appsIndexFragmentationCheck();
			for ( IndexFragmentation index : appsIndexResults )
			{
				MetricsHelper.metrics.addBucketValue( MetricsTypes.FRAGMENTATION.getName(), index.getTableName() + " " + index.getIndexName(), Double.valueOf( index.getAverageFragmentation() ).longValue() );
				LOG.info( "DatabaseIndex : apps." + index.getTableName() + "." + index.getIndexName() + " : " + index.getAverageFragmentation() );
			}
		}
	}

	public void setSystemInfoService( SystemInfoService systemInfoService )
	{
		this.systemInfoService = systemInfoService;
	}

	public OsgiService getOsgiService()
	{
		if ( osgiService == null )
		{
			osgiService = ( ( OsgiService ) ApplicationContextSupport.getBean( "osgiManager" ) );
		}
		return osgiService;
	}
}

