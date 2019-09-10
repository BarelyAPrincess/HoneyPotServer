package com.marchnetworks.health.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.types.AlertUserStateEnum;
import com.marchnetworks.health.alerts.AlertEntity;
import com.marchnetworks.health.alerts.DeviceAlertEntity;
import com.marchnetworks.health.alerts.ServerAlertEntity;
import com.marchnetworks.health.search.AlertSearchQuery;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class AlertDAOImpl extends GenericHibernateDAO<AlertEntity, Long> implements AlertDAO
{
	public List<AlertEntity> findClosedAlertsByQuery( List<String> territoryIds, List<String> deviceIds, AlertSearchQuery query, int maxResults )
	{
		Session session = ( Session ) getEntityManager().getDelegate();

		Criteria criteria = session.createCriteria( entityType, "alert" );

		criteria.createAlias( "deletedDevice", "delDevice", 1 );

		Disjunction alertDisjunction = Restrictions.disjunction();
		Conjunction deviceAlertConjunction = Restrictions.conjunction();

		deviceAlertConjunction.add( Restrictions.eq( "alert.class", DeviceAlertEntity.class ) );
		Disjunction deviceAlertDisjunction = Restrictions.disjunction();

		if ( !deviceIds.isEmpty() )
		{
			deviceAlertDisjunction.add( Restrictions.in( "deviceId", deviceIds ) );
		}

		if ( !territoryIds.isEmpty() )
		{
			Disjunction territoryDisjunction = Restrictions.disjunction();
			for ( int i = 0; i < territoryIds.size(); i++ )
			{
				String jsonId = CoreJsonSerializer.toJson( territoryIds.get( i ) );
				territoryDisjunction.add( Restrictions.like( "delDevice.path", jsonId, MatchMode.ANYWHERE ) );
			}
			deviceAlertDisjunction.add( territoryDisjunction );
		}
		if ( ( !deviceIds.isEmpty() ) || ( !territoryIds.isEmpty() ) )
		{
			deviceAlertConjunction.add( deviceAlertDisjunction );
			alertDisjunction.add( deviceAlertConjunction );
		}

		alertDisjunction.add( Restrictions.eq( "alert.class", ServerAlertEntity.class ) );
		criteria.add( alertDisjunction );

		criteria.add( Restrictions.eq( "userState", AlertUserStateEnum.CLOSED ) );

		SearchQueryUtils.setCriteriaForQuery( criteria, query );

		criteria.setReadOnly( true );
		criteria.setMaxResults( maxResults );

		List<AlertEntity> list = criteria.list();

		return list;
	}

	public int deleteClosedAlertsByClosedTime( long maxAge )
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append( " delete " );
		hqlQuery.append( entityType.getName() );
		hqlQuery.append( " where userState = :userState " );
		hqlQuery.append( " and lastUserStateChangedTime <= :closeTime " );

		Query deleteQuery = session.createQuery( hqlQuery.toString() ).setString( "userState", AlertUserStateEnum.CLOSED.name() ).setLong( "closeTime", maxAge );
		int purgeCount = deleteQuery.executeUpdate();

		return purgeCount;
	}
}
