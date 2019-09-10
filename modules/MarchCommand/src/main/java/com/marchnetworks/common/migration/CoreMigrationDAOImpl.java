package com.marchnetworks.common.migration;

import com.marchnetworks.command.api.migration.MigrationDAOImpl;
import com.marchnetworks.command.api.migration.MigrationDAOImpl.ConstraintType;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CoreMigrationDAOImpl extends MigrationDAOImpl implements CoreMigrationDAO
{
	private static final Logger LOG = LoggerFactory.getLogger( CoreMigrationDAOImpl.class );

	public void updateGenericStorage( Object[] clientObject )
	{
		Session session = ( Session ) entityManager.getDelegate();
		SQLQuery q = session.createSQLQuery( "INSERT into generic_storage (data, size, object_id, user_id, store, version) values (?,?,?,?,'USER',1)" );
		q.setParameter( 0, clientObject[1] );
		q.setParameter( 1, clientObject[2] );
		q.setParameter( 2, clientObject[3] );
		q.setParameter( 3, clientObject[4] );
		q.executeUpdate();
	}

	public void explicitColumnConversion( String tableName, String columnName, String varType )
	{
		Session session = ( Session ) entityManager.getDelegate();
		String columnTemp = columnName + "TEMP ";
		addColumn( tableName, columnTemp, varType );

		StringBuffer theQuery = new StringBuffer();
		theQuery.append( "UPDATE " + tableName + " SET " + columnName + "TEMP=CONVERT (" + varType + ", " + columnName + ");" );
		SQLQuery q = session.createSQLQuery( theQuery.toString() );
		q.executeUpdate();

		dropColumns( tableName, new String[] {columnName} );

		addColumn( tableName, columnName, varType );

		theQuery = new StringBuffer();
		theQuery.append( "UPDATE " + tableName + " SET " + columnName + "=" + columnName + "TEMP;" );
		theQuery.append( "ALTER TABLE " + tableName + " DROP COLUMN " + columnName + "TEMP;" );
		q = session.createSQLQuery( theQuery.toString() );
		q.executeUpdate();
	}

	public void updateLoginCacheUsernames()
	{
		if ( !columnExists( "LOGINCACHE", "USERNAME" ) )
		{
			addColumn( "LOGINCACHE", "USERNAME", "nvarchar(100)" );
		}
		List<Object[]> members = findItemsToMigrate( "member", null, new String[] {"FK_LOGINCACHE_ID", "NAME"} );
		for ( Object[] member : members )
		{
			Object value = member[1];
			String whereClause = "id = " + member[0];
			updateTable( "logincache", "username", value, whereClause );
		}

		String constraintName = findFirstConstraintName( "LOGINCACHE", ConstraintType.UNIQUE_CONSTRAINT );
		if ( constraintName == null )
		{
			addUniqueConstraint( "LOGINCACHE", new String[] {"USERNAME"} );
		}
	}

	public void migrateOldAuditLogs()
	{
		Session session = ( Session ) entityManager.getDelegate();
		StringBuilder sqlStatement = new StringBuilder( "INSERT INTO AUDIT_LOGS_OLD " );
		sqlStatement.append( " SELECT REMOTE_ADDRESS, USER_NAME, DATE_AND_TIME, CALLER_CLASS, CALLER_METHOD, TOP_OBJECT_NAME, AUDIT_LOG_BODY, ROOT_DEVICE_NAME, LEAF_DEVICE_NAME, REQUEST_TYPE" );
		sqlStatement.append( " FROM AUDIT_LOGS " );
		SQLQuery query = session.createSQLQuery( sqlStatement.toString() );
		try
		{
			query.executeUpdate();
		}
		catch ( SQLGrammarException sge )
		{
			LOG.info( "Failed to migrate audit logs data " );
		}
	}

	public void updateChannelResourceNames()
	{
		Session session = ( Session ) entityManager.getDelegate();
		StringBuilder sqlStatement = new StringBuilder( " UPDATE Resource SET Resource.NAME = Channel.name " );
		sqlStatement.append( " FROM Resource " );
		sqlStatement.append( " INNER JOIN Channel_Resource ON Resource.id = Channel_Resource.id " );
		sqlStatement.append( " INNER JOIN Channel ON Channel_Resource.channel = Channel.id " );

		SQLQuery query = session.createSQLQuery( sqlStatement.toString() );
		query.executeUpdate();
	}

	public void migrateLoginCache()
	{
		String memberTableName = "MEMBER";
		String loginCacheTableName = "LOGINCACHE";

		Session session = ( Session ) entityManager.getDelegate();
		StringBuilder sql = new StringBuilder( "UPDATE MEMBER " );

		sql.append( "SET MEMBER.HASH = LOGINCACHE.HASH, " );
		sql.append( "MEMBER.LAST_LOGIN = LOGINCACHE.LAST_LOGIN, " );

		if ( columnExists( loginCacheTableName, "APP_RIGHTS" ) )
		{
			sql.append( "MEMBER.ASSEMBLED_APP_RIGHTS = LOGINCACHE.APP_RIGHTS, " );
		}

		if ( columnExists( loginCacheTableName, "APP_PROFILE_DATA" ) )
		{
			sql.append( "MEMBER.ASSEMBLED_APP_DATA = LOGINCACHE.APP_PROFILE_DATA, " );
		}

		if ( columnExists( loginCacheTableName, "RIGHTS" ) )
		{
			sql.append( "MEMBER.ASSEMBLED_RIGHTS = LOGINCACHE.RIGHTS, " );
		}

		sql.append( "MEMBER.ASSEMBLED_SYSTEM_IDS = LOGINCACHE.SYSTEM_IDS, " );
		sql.append( "MEMBER.ASSEMBLED_LOGICAL_IDS = LOGINCACHE.LOGICAL_IDS, " );

		sql.append( "MEMBER.SALT = LOGINCACHE.SALT " );
		sql.append( "FROM LOGINCACHE " );
		sql.append( "JOIN MEMBER ON MEMBER.FK_LOGINCACHE_ID = LOGINCACHE.ID" );

		SQLQuery query = session.createSQLQuery( sql.toString() );
		try
		{
			query.executeUpdate();
		}
		catch ( Exception e )
		{
			LOG.error( "Failed to migrate login cache. Error: " + e.getMessage() );
			return;
		}

		dropConstraint( memberTableName, "FK87557E9AEBF18D66", ConstraintType.FOREIGN_KEY );

		dropColumns( memberTableName, new String[] {"FK_LOGINCACHE_ID"} );

		dropTables( new String[] {"LOGINCACHE"} );
	}

	public void migrateMemberViews()
	{
		List<Object[]> memberResourceViews = findAllRowsFromTable( "MEMBER_RESOURCE" );
		Map<BigDecimal, Set<String>> systemIds = new HashMap();
		Map<BigDecimal, Set<String>> logicalIds = new HashMap();
		Map<BigDecimal, BigDecimal> personalIds = new HashMap();

		for ( Object[] memberResourceView : memberResourceViews )
		{
			String resourceId = memberResourceView[1].toString();
			String type = memberResourceView[2].toString();
			BigDecimal memberId = ( BigDecimal ) memberResourceView[3];

			if ( type.equals( "SYSTEM" ) )
			{
				if ( systemIds.containsKey( memberId ) )
				{
					( ( Set ) systemIds.get( memberId ) ).add( resourceId );
				}
				else
				{
					systemIds.put( memberId, new HashSet() );
					( ( Set ) systemIds.get( memberId ) ).add( resourceId );
				}
			}
			else if ( type.equals( "LOGICAL" ) )
			{
				if ( logicalIds.containsKey( memberId ) )
				{
					( ( Set ) logicalIds.get( memberId ) ).add( resourceId );
				}
				else
				{
					logicalIds.put( memberId, new HashSet() );
					( ( Set ) logicalIds.get( memberId ) ).add( resourceId );
				}
			}
			else
			{
				personalIds.put( memberId, new BigDecimal( resourceId ) );
			}
		}

		Session session = ( Session ) entityManager.getDelegate();

		for ( Entry<BigDecimal, Set<String>> entry : systemIds.entrySet() )
		{
			StringBuilder sql = new StringBuilder( "UPDATE MEMBER " );
			String jsonIds = "'" + CoreJsonSerializer.toJson( entry.getValue() ) + "'";
			sql.append( "SET SYSTEM_IDS = " + jsonIds );
			sql.append( " WHERE ID = " + entry.getKey() );

			SQLQuery query = session.createSQLQuery( sql.toString() );
			try
			{
				query.executeUpdate();
			}
			catch ( Exception e )
			{
				LOG.error( "Failed to migrate system IDs to MEMBER table. Error: " + e.getMessage() );
				return;
			}
		}

		for ( Entry<BigDecimal, Set<String>> entry : logicalIds.entrySet() )
		{
			StringBuilder sql = new StringBuilder( "UPDATE MEMBER " );
			String jsonIds = "'" + CoreJsonSerializer.toJson( entry.getValue() ) + "'";
			sql.append( "SET LOGICAL_IDS = " + jsonIds );
			sql.append( " WHERE ID = " + entry.getKey() );

			SQLQuery query = session.createSQLQuery( sql.toString() );
			try
			{
				query.executeUpdate();
			}
			catch ( Exception e )
			{
				LOG.error( "Failed to migrate logical IDs to MEMBER table. Error: " + e.getMessage() );
				return;
			}
		}

		for ( Entry<BigDecimal, BigDecimal> entry : personalIds.entrySet() )
		{
			StringBuilder sql = new StringBuilder( "UPDATE MEMBER " );
			sql.append( "SET PERSONAL_ID = " + entry.getValue() );
			sql.append( " WHERE ID = " + entry.getKey() );

			SQLQuery query = session.createSQLQuery( sql.toString() );
			try
			{
				query.executeUpdate();
			}
			catch ( Exception e )
			{
				LOG.error( "Failed to migrate personal ID to MEMBER table. Error: " + e.getMessage() );
				return;
			}
		}

		dropTables( new String[] {"MEMBER_RESOURCE"} );
	}

	public void migrateProfileRights()
	{
		List<Object[]> profileRights = findAllRowsFromTable( "PROFILE_RIGHTS" );
		Map<BigDecimal, Set<String>> rights = new HashMap();
		Session session = ( Session ) entityManager.getDelegate();

		for ( Object[] right : profileRights )
		{
			BigDecimal profileId = ( BigDecimal ) right[0];
			String profileRight = right[1].toString();

			if ( rights.containsKey( profileId ) )
			{
				( ( Set ) rights.get( profileId ) ).add( profileRight );
			}
			else
			{
				rights.put( profileId, new HashSet() );
				( ( Set ) rights.get( profileId ) ).add( profileRight );
			}
		}

		for ( Entry<BigDecimal, Set<String>> entry : rights.entrySet() )
		{
			StringBuilder sql = new StringBuilder( "UPDATE PROFILE " );
			String jsonRights = "'" + CoreJsonSerializer.toJson( entry.getValue() ) + "'";
			sql.append( "SET RIGHTS = " + jsonRights );
			sql.append( " WHERE ID = " + entry.getKey() );

			SQLQuery query = session.createSQLQuery( sql.toString() );
			try
			{
				query.executeUpdate();
			}
			catch ( Exception e )
			{
				LOG.error( "Failed to migrate Profile Rights. Error: " + e.getMessage() );
				return;
			}
		}

		dropTables( new String[] {"PROFILE_RIGHTS"} );
	}

	public void migrateTerritoryToSchedules()
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder sql = new StringBuilder( "UPDATE SCHEDULES " );
		sql.append( "SET SCHEDULES.SYSTEM_ROOTS = MEMBER.ASSEMBLED_SYSTEM_IDS, " );
		sql.append( "SCHEDULES.LOGICAL_ROOTS = MEMBER.ASSEMBLED_LOGICAL_IDS " );
		sql.append( "FROM MEMBER JOIN SCHEDULES ON SCHEDULES.USERNAME = MEMBER.NAME" );

		SQLQuery query = session.createSQLQuery( sql.toString() );
		try
		{
			query.executeUpdate();
		}
		catch ( Exception e )
		{
			LOG.error( "Failed to migrate territories to schedule. Error: " + e.getMessage() );
			return;
		}

		String deleteSql = "DELETE FROM SCHEDULES WHERE SYSTEM_ROOTS IS NULL AND LOGICAL_ROOTS IS NULL";
		query = session.createSQLQuery( deleteSql );
		try
		{
			query.executeUpdate();
		}
		catch ( Exception e )
		{
			LOG.error( "Failed to delete schedules with no territory on migration. Error: " + e.getMessage() );
			return;
		}

		dropColumns( "SCHEDULES", new String[] {"USERNAME"} );
	}
}
