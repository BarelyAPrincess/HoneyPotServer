package com.marchnetworks.health.dao;

import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.health.alerts.DeviceAlertEntity;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import java.util.List;

public class TestDeviceAlertDAOImpl extends GenericHibernateDAO<DeviceAlertEntity, Long> implements TestDeviceAlertDAO
{
	private static final String DEVICE_ALERT_TO_ALERT_FK = "FK9CD46E13541446C2";
	private static final String DEVICE_DELETED_DEVICE_FK = "FK9CD46E13611D9A6B";
	private static final String DELETED_DEVICE_COLUMN = "FK_DELETED_DEVICE_ID";
	private static final String ALERT_TABLE_NAME = "ALERT";
	private static final String TABLE_NAME = "DEVICE_ALERT";
	private static final String DELETED_DEVICE_TABLE_NAME = "DELETED_DEVICE";
	private static final String ID_KEY = "id";

	public void batchInsert( List<DeviceAlertEntity> alerts, int batchSize )
	{
		List<List<DeviceAlertEntity>> batches = CollectionUtils.split( alerts, batchSize );
		Session session = ( Session ) entityManager.getDelegate();

		dropFKs();

		long id = getLastId().longValue() + 1L;

		for ( List<DeviceAlertEntity> batch : batches )
		{
			StringBuilder alertValues = new StringBuilder();
			StringBuilder deviceAlertValues = new StringBuilder();

			for ( int i = 0; i < batch.size(); i++ )
			{
				DeviceAlertEntity entity = ( DeviceAlertEntity ) batch.get( i );

				alertValues.append( "(" ).append( id ).append( "," );
				alertValues.append( "'" ).append( entity.getAlertCode() ).append( "'," );
				alertValues.append( "'" ).append( entity.getCategory() ).append( "'," );
				alertValues.append( "'" ).append( entity.getSeverity() ).append( "'," );
				alertValues.append( "'" ).append( entity.getSourceId() ).append( "'," );
				alertValues.append( "'" ).append( entity.getSourceDesc() ).append( "'," );
				alertValues.append( entity.getAlertTime() ).append( "," );
				alertValues.append( entity.getLastInstanceTime() ).append( "," );
				alertValues.append( entity.getAlertResolvedTime() ).append( "," );
				alertValues.append( entity.getCount() ).append( "," );
				alertValues.append( entity.getDeviceState() == true ? 0 : 1 ).append( "," );
				alertValues.append( "'" ).append( entity.getUserState() ).append( "'," );
				alertValues.append( entity.getLastUserStateChangedTime() );
				alertValues.append( ")" );

				deviceAlertValues.append( "(" ).append( id ).append( "," );
				deviceAlertValues.append( "'" ).append( entity.getDeviceId() ).append( "'," );
				deviceAlertValues.append( "'" ).append( entity.getDeviceAlertId() ).append( "'," );
				deviceAlertValues.append( entity.getThresholdDuration() ).append( "," );
				deviceAlertValues.append( entity.getThresholdFrequency() );
				deviceAlertValues.append( ")" );

				if ( i < batch.size() - 1 )
				{
					alertValues.append( "," );
					deviceAlertValues.append( "," );
				}

				id += 1L;
			}

			StringBuilder sql = new StringBuilder();
			sql.append( "SET IDENTITY_INSERT ALERT ON INSERT into ALERT (ID, ALERTCODE, CATEGORY, SEVERITY, SOURCEID, SOURCE_DESC, ALERT_TIME, LAST_INSTANCE_TIME, ALERT_RESOLVED_TIME, COUNT, DEVICE_STATE, USER_STATE, LAST_USER_STATE_CHANGED_TIME) VALUES " );
			sql.append( alertValues.toString() );
			sql.append( " SET IDENTITY_INSERT ALERT OFF\n" );
			sql.append( "INSERT INTO DEVICE_ALERT (ID, DEVICEID, DEVICE_ALERT_ID, THRESHOLD_DURATION, THRESHOLD_FREQUENCY) VALUES " );
			sql.append( deviceAlertValues.toString() );

			SQLQuery query = session.createSQLQuery( sql.toString() );
			query.executeUpdate();
		}

		addFKs();
	}

	private void dropFKs()
	{
		dropConstraint( "DEVICE_ALERT", "FK9CD46E13541446C2" );
		dropConstraint( "DEVICE_ALERT", "FK9CD46E13611D9A6B" );
	}

	private void addFKs()
	{
		addForeignKeyConstraint( "DEVICE_ALERT", "FK9CD46E13541446C2", "id", "ALERT", "id" );
		addForeignKeyConstraint( "DEVICE_ALERT", "FK9CD46E13611D9A6B", "FK_DELETED_DEVICE_ID", "DELETED_DEVICE", "id" );
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

