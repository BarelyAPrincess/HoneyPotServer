package com.marchnetworks.alarm.dao;

import com.marchnetworks.alarm.model.AlarmEntryEntity;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.dao.GenericHibernateDAO;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import java.util.List;

public class TestAlarmEntryDAOImpl extends GenericHibernateDAO<AlarmEntryEntity, Long> implements TestAlarmEntryDAO
{
	private static final String TABLE_NAME = "ALARM_ENTRY";
	private static final String ALARM_SOURCE_FK = "FKCEACCCC4D7DE2143";
	private static final String ALARM_SOURCE_COLUMN_NAME = "ALARM_SOURCE";
	private static final String ALARM_SOURCE_TABLE_NAME = "ALARM_SOURCE";
	private static final String ID_KEY = "id";

	public void batchInsert( List<AlarmEntryEntity> entries, int batchSize )
	{
		List<List<AlarmEntryEntity>> batches = CollectionUtils.split( entries, batchSize );
		Session session = ( Session ) entityManager.getDelegate();

		dropFK();

		String setInsertOn = "SET IDENTITY_INSERT ALARM_ENTRY ON";
		String setInsertOff = "SET IDENTITY_INSERT ALARM_ENTRY OFF";

		long id = getLastId().longValue() + 1L;

		for ( List<AlarmEntryEntity> batch : batches )
		{
			StringBuilder values = new StringBuilder();

			for ( int i = 0; i < batch.size(); i++ )
			{
				AlarmEntryEntity entity = ( AlarmEntryEntity ) batch.get( i );
				values.append( "(" );
				values.append( id ).append( "," );
				values.append( "'" ).append( entity.getDeviceAlarmEntryID() ).append( "'," );
				values.append( entity.getFirstInstanceTime() ).append( "," );
				values.append( entity.getAlarmSourceID() ).append( "," );
				values.append( entity.getClosedTime() ).append( "," );
				values.append( "'" ).append( entity.getClosedByUser() ).append( "'," );
				values.append( entity.getCount() );
				values.append( ")" );

				if ( i < batch.size() - 1 )
				{
					values.append( "," );
				}

				id += 1L;
			}

			StringBuilder sql = new StringBuilder();
			sql.append( setInsertOn );
			sql.append( " INSERT into ALARM_ENTRY (ID, DEVICE_ALARM_ENTRY_ID, FIRST_INSTANCE_TIME, ALARM_SOURCE, CLOSED_TIME, CLOSED_BY_USER, COUNT) VALUES " );
			sql.append( values.toString() );
			sql.append( " " + setInsertOff );

			SQLQuery query = session.createSQLQuery( sql.toString() );
			query.executeUpdate();
		}

		addFK();
	}

	private void dropFK()
	{
		dropConstraint( "ALARM_ENTRY", "FKCEACCCC4D7DE2143" );
	}

	private void addFK()
	{
		addForeignKeyConstraint( "ALARM_ENTRY", "FKCEACCCC4D7DE2143", "ALARM_SOURCE", "ALARM_SOURCE", "id" );
	}

	private void dropConstraint( String tableName, String constraintName )
	{
		Session session = ( Session ) entityManager.getDelegate();
		SQLQuery q = session.createSQLQuery( "ALTER TABLE " + tableName + " DROP CONSTRAINT " + constraintName );
		q.executeUpdate();
	}

	private void addForeignKeyConstraint( String tableName, String foreignKeyName, String foreignKeyColumn, String referenceTable, String referenceColumn )
	{
		Session session = ( Session ) entityManager.getDelegate();
		SQLQuery q = session.createSQLQuery( "ALTER TABLE " + tableName + " ADD CONSTRAINT " + foreignKeyName + " FOREIGN KEY (" + foreignKeyColumn + ") REFERENCES " + referenceTable + "(" + referenceColumn + ")" );
		q.executeUpdate();
	}
}
