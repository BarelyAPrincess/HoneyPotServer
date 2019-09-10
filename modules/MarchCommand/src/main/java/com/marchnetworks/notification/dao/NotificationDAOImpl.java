package com.marchnetworks.notification.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.notification.model.NotificationEntity;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class NotificationDAOImpl extends GenericHibernateDAO<NotificationEntity, Long> implements NotificationDAO
{
	public List<NotificationEntity> findAllByGroupAndAppId( String group, String appId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "group", group ) );
		if ( appId != null )
		{
			criteria.add( Restrictions.eq( "appId", appId ) );
		}
		else
		{
			criteria.add( Restrictions.isNull( "appId" ) );
		}
		return criteria.list();
	}

	public boolean existsByGroupAppIdAndName( String group, String appId, String name )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "group", group ) );
		if ( appId != null )
		{
			criteria.add( Restrictions.eq( "appId", appId ) );
		}
		else
		{
			criteria.add( Restrictions.isNull( "appId" ) );
		}
		criteria.add( Restrictions.eq( "name", name ) );
		return checkExists( criteria );
	}

	public void deleteByAppId( String appId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder query = new StringBuilder();
		query.append( "delete " );
		query.append( entityType.getName() );
		query.append( " where appId = :appId " );

		Query deleteQuery = session.createQuery( query.toString() );
		deleteQuery.setParameter( "appId", appId );
		deleteQuery.executeUpdate();
	}

	public List<NotificationEntity> findAllByUsername( String username )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType );
		Disjunction disjunction = Restrictions.disjunction();
		disjunction.add( Restrictions.eq( "username", username ) );
		String jsonUsername = CoreJsonSerializer.toJson( username );
		disjunction.add( Restrictions.like( "recipientsString", jsonUsername, MatchMode.ANYWHERE ) );
		criteria.add( disjunction );

		return criteria.list();
	}
}

