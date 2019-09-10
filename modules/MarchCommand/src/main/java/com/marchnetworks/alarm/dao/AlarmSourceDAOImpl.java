package com.marchnetworks.alarm.dao;

import com.marchnetworks.alarm.model.AlarmSourceEntity;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AlarmSourceDAOImpl extends GenericHibernateDAO<AlarmSourceEntity, Long> implements AlarmSourceDAO
{
	public AlarmSourceEntity findByDeviceIdAndDeviceAlarmSourceId( Long deviceId, String deviceAlarmSourceID )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.like( "deviceId", deviceId ) );
		crit.add( Restrictions.like( "deviceAlarmSourceId", deviceAlarmSourceID ) );
		crit.add( Restrictions.eq( "isDeleted", Boolean.valueOf( false ) ) );
		crit.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		AlarmSourceEntity alarmSource = ( AlarmSourceEntity ) crit.uniqueResult();

		return alarmSource;
	}

	public List<AlarmSourceEntity> findAll()
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.like( "isDeleted", Boolean.valueOf( false ) ) );
		crit.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		List<AlarmSourceEntity> results = crit.list();
		return results;
	}

	public List<AlarmSourceEntity> findAllWithDeletedByDeviceId( Long deviceId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.like( "deviceId", deviceId ) );
		crit.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		List<AlarmSourceEntity> results = crit.list();
		return results;
	}

	public List<AlarmSourceEntity> findAllByDeviceId( Long deviceId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.like( "deviceId", deviceId ) );
		crit.add( Restrictions.like( "isDeleted", Boolean.valueOf( false ) ) );
		crit.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		List<AlarmSourceEntity> results = crit.list();
		return results;
	}

	public List<Long> findAllDeletedAlarmSourceIds( List<Long> deviceIds )
	{
		Session session = ( Session ) getEntityManager().getDelegate();

		List<Long> usedDevices = findAllDeletedAlarmSourceDeviceIds();

		HashSet<Long> deviceSet = new HashSet( usedDevices );
		for ( int i = 0; i < deviceIds.size(); i++ )
		{
			if ( !deviceSet.contains( deviceIds.get( i ) ) )
			{
				deviceIds.remove( i );
				i--;
			}
		}

		List<Long> results = new ArrayList();
		List<List<Long>> splitParams = CollectionUtils.split( deviceIds, 1000 );
		for ( List<Long> params : splitParams )
		{
			Criteria criteria = session.createCriteria( entityType );

			criteria.setReadOnly( true );
			criteria.add( Restrictions.eq( "isDeleted", Boolean.valueOf( true ) ) );
			criteria.add( Restrictions.in( "deviceId", params ) );
			criteria.setProjection( Projections.distinct( Projections.id() ) );

			results.addAll( criteria.list() );
		}

		return results;
	}

	private List<Long> findAllDeletedAlarmSourceDeviceIds()
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "isDeleted", Boolean.valueOf( true ) ) );
		criteria.setReadOnly( true );

		criteria.setProjection( Projections.distinct( Projections.property( "deviceId" ) ) );

		return criteria.list();
	}

	public List<Long> findAllDeletedDeviceAlarmSourceIds( List<String> territoryIds )
	{
		if ( territoryIds.isEmpty() )
		{
			return new ArrayList();
		}

		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType.getName() );

		criteria.createAlias( "deletedDevice", "delDevice" );

		Disjunction territoryDisjunction = Restrictions.disjunction();
		for ( int i = 0; i < territoryIds.size(); i++ )
		{
			String jsonId = CoreJsonSerializer.toJson( territoryIds.get( i ) );
			territoryDisjunction.add( Restrictions.like( "delDevice.path", jsonId, MatchMode.ANYWHERE ) );
		}
		criteria.add( territoryDisjunction );

		criteria.setProjection( Projections.distinct( Projections.id() ) );
		criteria.setReadOnly( true );

		List<Long> list = criteria.list();
		return list;
	}

	public List<AlarmSourceEntity> findAllUnreferencedDeleted( List<Long> referencedAlarmSources )
	{
		Session session = ( Session ) getEntityManager().getDelegate();

		List<AlarmSourceEntity> results = new ArrayList();
		if ( !referencedAlarmSources.isEmpty() )
		{
			List<List<Long>> splitParams = CollectionUtils.split( referencedAlarmSources, 1000 );
			for ( List<Long> params : splitParams )
			{
				Criteria criteria = session.createCriteria( entityType );

				criteria.add( Restrictions.eq( "isDeleted", Boolean.valueOf( true ) ) );
				criteria.add( Restrictions.not( Restrictions.in( "id", params ) ) );
				criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

				results.addAll( criteria.list() );
			}
		}
		else
		{
			Criteria criteria = session.createCriteria( entityType );

			criteria.add( Restrictions.eq( "isDeleted", Boolean.valueOf( true ) ) );
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			results.addAll( criteria.list() );
		}
		return results;
	}

	public List<Long> findAllDeletedDeviceIds()
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );
		criteria.createCriteria( "deletedDevice" ).setProjection( Projections.distinct( Projections.property( "id" ) ) );

		criteria.setReadOnly( true );

		return criteria.list();
	}

	public List<AlarmSourceEntity> findAllIncludeDeleted()
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		List<AlarmSourceEntity> results = crit.list();
		return results;
	}
}
