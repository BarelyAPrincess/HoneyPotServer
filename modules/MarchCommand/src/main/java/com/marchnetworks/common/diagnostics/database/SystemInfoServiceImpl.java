package com.marchnetworks.common.diagnostics.database;

import com.marchnetworks.command.api.diagnostics.SystemInfoCoreService;
import com.marchnetworks.command.common.diagnostics.DatabaseNameEnum;
import com.marchnetworks.command.common.diagnostics.IndexFragmentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SystemInfoServiceImpl implements SystemInfoService, SystemInfoCoreService
{
	private static final Logger LOG = LoggerFactory.getLogger( SystemInfoServiceImpl.class );

	private static final double MIN_INDEX_REORG_THRESHOLD = 5.0D;

	private static final double MIN_INDEX_REBUILD_THRESHOLD = 30.0D;

	private SystemInfoDAO systemInfoDAO;

	public List<DatabaseSize> getDatabaseSize()
	{
		return systemInfoDAO.getDatabaseSize();
	}

	public List<IndexFragmentation> getAppsIndexFragmentation( List<String> tables )
	{
		return getIndexFragmentation( DatabaseNameEnum.APPS_DB.getName(), tables );
	}

	public List<IndexFragmentation> getCommandIndexFragmentation( List<String> tables )
	{
		return getIndexFragmentation( DatabaseNameEnum.COMMAND_DB.getName(), tables );
	}

	private List<IndexFragmentation> getIndexFragmentation( String databaseName, List<String> tables )
	{
		List<IndexFragmentation> results = new ArrayList();
		for ( String table : tables )
		{
			results.addAll( systemInfoDAO.getIndexFragmentation( databaseName, table ) );
		}
		return results;
	}

	public void defragmentIndex( IndexFragmentation indexFragmentation )
	{
		LOG.info( "Index {} from table {} has an average of {}% fragmentation", new Object[] {indexFragmentation.getIndexName(), indexFragmentation.getTableName(), Double.valueOf( indexFragmentation.getAverageFragmentation() )} );
		if ( indexFragmentation.getAverageFragmentation() > 5.0D )
		{
			long start = System.currentTimeMillis();
			systemInfoDAO.reorgIndexes( DatabaseNameEnum.APPS_DB.getName(), indexFragmentation.getTableName(), new String[] {indexFragmentation.getIndexName()} );
			LOG.info( "Index Reorg operation for index {} on table {} took {} ms", new Object[] {indexFragmentation.getIndexName(), indexFragmentation.getTableName(), Long.valueOf( System.currentTimeMillis() - start )} );

			if ( indexFragmentation.getAverageFragmentation() > 30.0D )
			{
				start = System.currentTimeMillis();
				systemInfoDAO.rebuildIndexes( DatabaseNameEnum.APPS_DB.getName(), indexFragmentation.getTableName(), new String[] {indexFragmentation.getIndexName()} );
				LOG.info( "Index Rebuild operation for index {} on table {} took {} ms", new Object[] {indexFragmentation.getIndexName(), indexFragmentation.getTableName(), Long.valueOf( System.currentTimeMillis() - start )} );
			}
		}
	}

	public void setSystemInfoDAO( SystemInfoDAO systemInfoDAO )
	{
		this.systemInfoDAO = systemInfoDAO;
	}
}
