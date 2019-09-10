package com.marchnetworks.alarm.dao;

import com.marchnetworks.alarm.model.AlarmEntryEntity;
import com.marchnetworks.alarm.model.AlarmSourceEntity;
import com.marchnetworks.command.common.dao.GenericHibernateDAO;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;

public class AlarmEntryDAOImpl extends GenericHibernateDAO<AlarmEntryEntity, Long> implements AlarmEntryDAO
{
	public AlarmEntryEntity findByDeviceEntryIdAndSource( String deviceAlarmEntryId, AlarmSourceEntity alarmSource )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.like( "deviceAlarmEntryId", deviceAlarmEntryId ) );
		crit.add( Restrictions.eq( "alarmSource", alarmSource ) );
		crit.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		AlarmEntryEntity alarm = ( AlarmEntryEntity ) crit.uniqueResult();
		return alarm;
	}

	public AlarmEntryEntity findOpenEntryByDeviceEntryIdAndSource( String deviceAlarmEntryId, AlarmSourceEntity alarmSource )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.like( "deviceAlarmEntryId", deviceAlarmEntryId ) );
		crit.add( Restrictions.eq( "alarmSource", alarmSource ) );
		crit.add( Restrictions.le( "closedTime", Long.valueOf( 0L ) ) );
		crit.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		AlarmEntryEntity alarm = ( AlarmEntryEntity ) crit.uniqueResult();
		return alarm;
	}

	public List<AlarmEntryEntity> findByAlarmSource( AlarmSourceEntity alarmSource )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.eq( "alarmSource", alarmSource ) );
		crit.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		List<AlarmEntryEntity> result = crit.list();
		return result;
	}

	public List<AlarmEntryEntity> findAllByQuery( List<Long> alarmSourceIDs, boolean includeOpenEntries, boolean includeClosedEntries, long startTime, long endTime, int maxEntries )
	{
		if ( ( ( !includeOpenEntries ) && ( !includeClosedEntries ) ) || ( alarmSourceIDs.isEmpty() ) )
		{
			return new ArrayList();
		}

		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.in( "alarmSource.id", alarmSourceIDs ) );

		if ( ( includeOpenEntries ) && ( !includeClosedEntries ) )
		{
			crit.add( Restrictions.eq( "closedTime", Long.valueOf( 0L ) ) );
		}

		if ( ( !includeOpenEntries ) && ( includeClosedEntries ) )
		{
			crit.add( Restrictions.ne( "closedTime", Long.valueOf( 0L ) ) );
		}

		if ( ( startTime > 0L ) && ( endTime == 0L ) )
		{
			crit.add( Restrictions.disjunction().add( Restrictions.ge( "firstInstanceTime", Long.valueOf( startTime ) ) ).add( Restrictions.ge( "lastInstanceTime", Long.valueOf( startTime ) ) ).add( Restrictions.ge( "closedTime", Long.valueOf( startTime ) ) ) );

		}
		else if ( ( endTime > 0L ) && ( startTime == 0L ) )
		{
			crit.add( Restrictions.disjunction().add( Restrictions.le( "firstInstanceTime", Long.valueOf( endTime ) ) ).add( Restrictions.le( "lastInstanceTime", Long.valueOf( endTime ) ) ).add( Restrictions.between( "closedTime", Long.valueOf( 1L ), Long.valueOf( endTime ) ) ) );

		}
		else if ( ( startTime > 0L ) && ( endTime > 0L ) )
		{
			crit.add( Restrictions.disjunction().add( Restrictions.between( "firstInstanceTime", Long.valueOf( startTime ), Long.valueOf( endTime ) ) ).add( Restrictions.between( "lastInstanceTime", Long.valueOf( startTime ), Long.valueOf( endTime ) ) ).add( Restrictions.between( "closedTime", Long.valueOf( startTime ), Long.valueOf( endTime ) ) ) );
		}

		if ( ( includeOpenEntries ) && ( !includeClosedEntries ) )
		{
			crit.addOrder( Order.desc( "lastInstanceTime" ) );
		}
		else
		{
			crit.addOrder( Order.asc( "lastInstanceTime" ) );
		}

		crit.setFetchMode( "alarmSource", FetchMode.SELECT );
		crit.setReadOnly( true );

		crit.setMaxResults( maxEntries );

		List<AlarmEntryEntity> list = crit.list();
		if ( list == null )
		{
			return new ArrayList();
		}
		return list;
	}

	public List<Long> findReferencedAlarmSources( boolean includeOpenEntries, boolean includeClosedEntries, long startTime, long endTime )
	{
		Session session = ( Session ) entityManager.getDelegate();
		StringBuilder hqlQuery = new StringBuilder();

		hqlQuery.append( "select alSource.id from " );
		hqlQuery.append( entityType.getName() );
		hqlQuery.append( " alarmEntry join alarmEntry.alarmSource as alSource" );

		boolean includeOpen = ( includeOpenEntries ) && ( !includeClosedEntries );
		boolean includeClosed = ( !includeOpenEntries ) && ( includeClosedEntries );

		if ( ( includeOpen ) || ( includeClosed ) || ( startTime > 0L ) || ( endTime > 0L ) )
		{
			hqlQuery.append( " where" );
		}

		if ( includeOpen )
		{
			hqlQuery.append( " alarmEntry.closedTime = 0" );
			if ( ( startTime > 0L ) || ( endTime > 0L ) )
			{
				hqlQuery.append( " and" );
			}
		}

		if ( includeClosed )
		{
			hqlQuery.append( " alarmEntry.closedTime != 0" );
			if ( ( startTime > 0L ) || ( endTime > 0L ) )
			{
				hqlQuery.append( " and" );
			}
		}

		if ( ( startTime > 0L ) && ( endTime == 0L ) )
		{
			hqlQuery.append( " (alarmEntry.firstInstanceTime >= :startTime" );
			hqlQuery.append( " or alarmEntry.lastInstanceTime >= :startTime" );
			hqlQuery.append( " or alarmEntry.closedTime >= :startTime)" );
		}
		else if ( ( endTime > 0L ) && ( startTime == 0L ) )
		{
			hqlQuery.append( " (alarmEntry.firstInstanceTime <= :endTime" );
			hqlQuery.append( " or alarmEntry.lastInstanceTime <= :endTime" );
			hqlQuery.append( " or alarmEntry.closedTime between 1 and :endTime)" );
		}
		else if ( ( endTime > 0L ) && ( startTime > 0L ) )
		{
			hqlQuery.append( " (alarmEntry.firstInstanceTime between :startTime and :endTime" );
			hqlQuery.append( " or alarmEntry.lastInstanceTime between :startTime and :endTime" );
			hqlQuery.append( " or alarmEntry.closedTime between :startTime and :endTime)" );
		}

		hqlQuery.append( " group by alSource.id" );

		if ( includeOpen )
		{
			hqlQuery.append( " order by max (alarmEntry.lastInstanceTime) desc" );
		}
		else
		{
			hqlQuery.append( " order by min (alarmEntry.lastInstanceTime) asc" );
		}

		Query queryObj = session.createQuery( hqlQuery.toString() );
		queryObj.setReadOnly( true );

		if ( startTime > 0L )
		{
			queryObj.setLong( "startTime", startTime );
		}
		if ( endTime > 0L )
		{
			queryObj.setLong( "endTime", endTime );
		}

		List<Long> list = queryObj.list();
		if ( list == null )
		{
			return new ArrayList();
		}
		return list;
	}

	public List<AlarmEntryEntity> findClosedNotReconciledByDevice( Long deviceId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType ).add( Restrictions.gt( "closedTime", Long.valueOf( 0L ) ) ).add( Restrictions.eq( "reconciledWithDevice", Boolean.FALSE ) ).createCriteria( "alarmSource" ).add( Restrictions.eq( "deviceId", deviceId ) ).setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		return crit.list();
	}

	public List<Long> findReferencedDeletedAlarmSourceIds()
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.createCriteria( "alarmSource" ).add( Restrictions.eq( "isDeleted", Boolean.valueOf( true ) ) );
		crit.add( Restrictions.gt( "closedTime", Long.valueOf( 0L ) ) );
		crit.setProjection( Projections.distinct( Projections.property( "alarmSource.id" ) ) );

		return crit.list();
	}

	public int deleteClosedAlarmsByLastInstanceTime( long maxLastInstanceTime )
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append( " delete " );
		hqlQuery.append( entityType.getName() );
		hqlQuery.append( " where lastInstanceTime <= :lastInstanceTime " );
		hqlQuery.append( " and closedTime > :closedTime " );

		Query deleteQuery = session.createQuery( hqlQuery.toString() ).setLong( "lastInstanceTime", maxLastInstanceTime ).setLong( "closedTime", 0L );

		int purgeCount = deleteQuery.executeUpdate();

		return purgeCount;
	}

	public AlarmEntryEntity findUnclosedByAlarmSource( AlarmSourceEntity alarmSource )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.eq( "alarmSource", alarmSource ) );
		crit.add( Restrictions.eq( "closedTime", Long.valueOf( 0L ) ) );
		crit.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		AlarmEntryEntity alarm = ( AlarmEntryEntity ) crit.uniqueResult();
		return alarm;
	}

	public List<AlarmEntryEntity> findAllOpen()
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.add( Restrictions.eq( "closedTime", Long.valueOf( 0L ) ) );
		return crit.list();
	}
}
