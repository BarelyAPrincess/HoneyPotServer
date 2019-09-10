package com.marchnetworks.common.diagnostics.database;

import com.marchnetworks.command.common.diagnostics.DatabaseNameEnum;
import com.marchnetworks.command.common.diagnostics.IndexFragmentation;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

public class SystemInfoDAOImpl implements SystemInfoDAO
{
	private EntityManager entityManager;

	public List<DatabaseSize> getDatabaseSize()
	{
		Session session = ( Session ) entityManager.getDelegate();

		DatabaseSize command = new DatabaseSize( DatabaseNameEnum.COMMAND_DB.getName() );
		DatabaseSize apps = new DatabaseSize( DatabaseNameEnum.APPS_DB.getName() );
		List<DatabaseSize> sizes = Arrays.asList( new DatabaseSize[] {command, apps} );
		List<Object[]> results = new ArrayList();

		String sql = "SELECT name, type_desc, size, FILEPROPERTY(name, 'SpaceUsed') FROM sys.database_files";

		SQLQuery query = session.createSQLQuery( "BEGIN TRANSACTION use [" + DatabaseNameEnum.APPS_DB.getName() + "] " + sql + " COMMIT" );
		results.addAll( query.list() );

		query = session.createSQLQuery( "BEGIN TRANSACTION use [" + DatabaseNameEnum.COMMAND_DB.getName() + "] " + sql + " COMMIT" );
		results.addAll( query.list() );

		for ( Object[] result : results )
		{
			String name = ( ( String ) result[0] ).toLowerCase();
			String type = ( String ) result[1];
			long size = ( ( Integer ) result[2] ).intValue() * 8 / 1024;
			long usedSize = ( ( Integer ) result[3] ).intValue() * 8 / 1024;

			DatabaseSize database = null;
			if ( name.startsWith( DatabaseNameEnum.COMMAND_DB.getName() ) )
			{
				database = command;
			}
			else
			{
				if ( !name.startsWith( DatabaseNameEnum.APPS_DB.getName() ) )
					continue;
				database = apps;
			}

			if ( type.equals( "ROWS" ) )
			{
				database.setDatabaseSize( size );
				database.setDatabaseUsedSize( usedSize );
			}
			else
			{
				database.setLogSize( size );
				database.setLogUsedSize( usedSize );
			}
		}

		return sizes;
	}

	public List<IndexFragmentation> getIndexFragmentation( String databaseName, String tableName )
	{
		Session session = ( Session ) entityManager.getDelegate();
		List<Object[]> queryResults = new ArrayList();
		try
		{
			StringBuilder sb = new StringBuilder( "SELECT name, avg_fragmentation_in_percent FROM sys.dm_db_index_physical_stats " );
			sb.append( "(DB_ID(N'" );
			sb.append( databaseName );
			sb.append( "'), OBJECT_ID(N'" );
			sb.append( tableName );
			sb.append( "'), NULL, NULL, NULL) AS a JOIN sys.indexes AS b ON a.object_id = b.object_id AND a.index_id = b.index_id;" );

			SQLQuery query = session.createSQLQuery( "BEGIN TRANSACTION use [" + databaseName + "] " + sb.toString() + " COMMIT" );
			queryResults.addAll( query.list() );
		}
		finally
		{
			switchBackToCoreDatabase( session );
		}

		List<IndexFragmentation> results = new ArrayList( queryResults.size() );
		for ( Object[] row : queryResults )
		{
			IndexFragmentation indexFragmentation = new IndexFragmentation( databaseName, tableName, ( String ) row[0], ( ( Double ) row[1] ).doubleValue() );
			results.add( indexFragmentation );
		}

		return results;
	}

	public void reorgIndexes( String databaseName, String tableName, String... indexes )
	{
		Session session = ( Session ) entityManager.getDelegate();
		try
		{
			for ( String index : indexes )
			{
				StringBuilder sb = new StringBuilder( "ALTER INDEX " );
				sb.append( index );
				sb.append( " ON " );
				sb.append( tableName );
				sb.append( " REORGANIZE " );

				SQLQuery query = session.createSQLQuery( "BEGIN TRANSACTION use [" + databaseName + "] " + sb.toString() + " COMMIT" );
				query.executeUpdate();
			}
		}
		finally
		{
			switchBackToCoreDatabase( session );
		}
	}

	public void rebuildIndexes( String databaseName, String tableName, String... indexes )
	{
		Session session = ( Session ) entityManager.getDelegate();
		try
		{
			for ( String index : indexes )
			{
				StringBuilder sb = new StringBuilder( "ALTER INDEX " );
				sb.append( index );
				sb.append( " ON " );
				sb.append( tableName );
				sb.append( " REBUILD " );

				SQLQuery query = session.createSQLQuery( "BEGIN TRANSACTION use [" + databaseName + "] " + sb.toString() + " COMMIT" );
				query.executeUpdate();
			}
		}
		finally
		{
			switchBackToCoreDatabase( session );
		}
	}

	private void switchBackToCoreDatabase( Session session )
	{
		SQLQuery query = session.createSQLQuery( "BEGIN TRANSACTION use [" + DatabaseNameEnum.COMMAND_DB.getName() + "] COMMIT" );
		query.executeUpdate();
	}

	public void setEntityManager( EntityManager entityManager )
	{
		this.entityManager = entityManager;
	}
}
