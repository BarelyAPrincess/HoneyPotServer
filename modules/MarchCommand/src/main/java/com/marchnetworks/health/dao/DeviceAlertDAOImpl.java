package com.marchnetworks.health.dao;

import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.common.types.AlertUserStateEnum;
import com.marchnetworks.health.alerts.DeviceAlertEntity;
import com.marchnetworks.health.search.AlertSearchQuery;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DeviceAlertDAOImpl extends GenericHibernateDAO<DeviceAlertEntity, Long> implements DeviceAlertDAO
{
	private static final int MAX_QUERY_PARAMETERS = 1000;

	public DeviceAlertEntity findAlert( String deviceId, String deviceAlertId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.add( Restrictions.like( "deviceId", deviceId ) );
		crit.add( Restrictions.like( "deviceAlertId", deviceAlertId ) );

		crit.addOrder( Order.desc( "id" ) );
		crit.setMaxResults( 1 );
		DeviceAlertEntity alert = ( DeviceAlertEntity ) crit.uniqueResult();
		return alert;
	}

	public DeviceAlertEntity findByIdentifiers( String alertCode, String deviceId, String sourceId, long alertTime )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.like( "alertCode", alertCode ) );
		crit.add( Restrictions.like( "deviceId", deviceId ) );
		crit.add( Restrictions.like( "sourceId", sourceId ) );
		crit.add( Restrictions.eq( "alertTime", Long.valueOf( alertTime ) ) );
		DeviceAlertEntity alert = ( DeviceAlertEntity ) crit.uniqueResult();

		return alert;
	}

	public List<DeviceAlertEntity> findUnresolvedAlertsByIdentifiers( String alertCode, String deviceId, String sourceId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.like( "alertCode", alertCode ) );
		criteria.add( Restrictions.like( "deviceId", deviceId ) );
		criteria.add( Restrictions.like( "sourceId", sourceId ) );
		criteria.add( Restrictions.eq( "deviceState", Boolean.TRUE ) );

		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		return criteria.list();
	}

	public DeviceAlertEntity findUserOpenAlertByIdentifiers( String alertCode, String deviceId, String sourceId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.like( "alertCode", alertCode ) );
		criteria.add( Restrictions.like( "deviceId", deviceId ) );
		criteria.add( Restrictions.like( "sourceId", sourceId ) );
		criteria.add( Restrictions.eq( "userState", AlertUserStateEnum.OPEN ) );

		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		return ( DeviceAlertEntity ) criteria.uniqueResult();
	}

	public List<DeviceAlertEntity> findAllUserOpenAlertsByDevices( List<String> deviceIds )
	{
		Criterion restriction = Restrictions.eq( "userState", AlertUserStateEnum.OPEN );
		return findAlertsByDeviceIds( deviceIds, new Criterion[] {restriction} );
	}

	public List<DeviceAlertEntity> findAllAlertsByDevice( String deviceId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.add( Restrictions.eq( "deviceId", deviceId ) );
		return criteria.list();
	}

	public List<DeviceAlertEntity> findTimeRestrictedAlertsByDevices( List<String> deviceIds, long startTime, long endTime )
	{
		return findAlertsByDeviceIds( deviceIds, new Criterion[] {Restrictions.gt( "lastInstanceTime", Long.valueOf( startTime ) ), Restrictions.le( "lastInstanceTime", Long.valueOf( endTime ) )} );
	}

	public List<DeviceAlertEntity> findAllAlertsByRootDeviceAndSourceIdList( String rootDeviceId, List<String> sourceIds )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.add( Restrictions.eq( "deviceId", rootDeviceId ) );
		criteria.add( Restrictions.isNull( "channelName" ) );
		if ( !sourceIds.isEmpty() )
		{
			criteria.add( Restrictions.in( "sourceId", sourceIds ) );
		}
		return criteria.list();
	}

	public List<DeviceAlertEntity> findAllAlertsByRootDeviceAndSourceId( String rootDeviceId, String source )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.eq( "deviceId", rootDeviceId ) );
		criteria.add( Restrictions.eq( "sourceId", source ) );
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		return criteria.list();
	}

	public List<Long> findAllDeletedDeviceIds()
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.createCriteria( "deletedDevice" ).setProjection( Projections.distinct( Projections.property( "id" ) ) );

		return criteria.list();
	}

	public List<String> findAllClosedDeviceIds( AlertSearchQuery query )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "userState", AlertUserStateEnum.CLOSED ) );
		criteria.add( Restrictions.isNull( "deletedDevice" ) );

		SearchQueryUtils.setCriteriaForQuery( criteria, query );
		criteria.setProjection( Projections.distinct( Projections.property( "deviceId" ) ) );
		criteria.setReadOnly( true );

		return criteria.list();
	}

	private List<DeviceAlertEntity> findAlertsByDeviceIds( List<String> deviceIds, Criterion... restrictions )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		List<String> usedDevices = findAllDeviceIds( restrictions );

		HashSet<String> deviceSet = new HashSet( usedDevices );
		for ( int i = 0; i < deviceIds.size(); i++ )
		{
			if ( !deviceSet.contains( deviceIds.get( i ) ) )
			{
				deviceIds.remove( i );
				i--;
			}
		}

		List<DeviceAlertEntity> result = new ArrayList();

		List<List<String>> splitParams = CollectionUtils.split( deviceIds, 1000 );
		for ( List<String> params : splitParams )
		{
			Criteria criteria = session.createCriteria( entityType );
			criteria.setReadOnly( true );
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			for ( Criterion criterion : restrictions )
			{
				criteria.add( criterion );
			}
			criteria.add( Restrictions.in( "deviceId", params ) );

			result.addAll( criteria.list() );
		}

		return result;
	}

	private List<String> findAllDeviceIds( Criterion... restrictions )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );
		for ( Criterion criterion : restrictions )
		{
			criteria.add( criterion );
		}

		criteria.setProjection( Projections.distinct( Projections.property( "deviceId" ) ) );

		return criteria.list();
	}

	public List<String> findClosedNotReconciledAlertIdsByDeviceId( String deviceId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "deviceId", deviceId ) );
		criteria.add( Restrictions.eq( "userState", AlertUserStateEnum.CLOSED ) );
		criteria.add( Restrictions.eq( "reconciledWithDevice", Boolean.valueOf( false ) ) );
		criteria.setProjection( Projections.distinct( Projections.property( "deviceAlertId" ) ) );
		criteria.setReadOnly( true );

		return criteria.list();
	}

	public List<DeviceAlertEntity> findClosedAlertsByDeviceIds( String deviceId, List<String> deviceAlertIds )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "deviceId", deviceId ) );
		criteria.add( Restrictions.eq( "userState", AlertUserStateEnum.CLOSED ) );
		criteria.add( Restrictions.eq( "reconciledWithDevice", Boolean.valueOf( false ) ) );
		criteria.add( Restrictions.in( "deviceAlertId", deviceAlertIds ) );

		return criteria.list();
	}
}
