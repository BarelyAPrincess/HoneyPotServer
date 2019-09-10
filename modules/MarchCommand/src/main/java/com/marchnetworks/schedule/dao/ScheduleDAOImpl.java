package com.marchnetworks.schedule.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.schedule.model.ScheduleEntity;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class ScheduleDAOImpl extends GenericHibernateDAO<ScheduleEntity, Long> implements ScheduleDAO
{
	public List<ScheduleEntity> findAllByGroupAndAppId( String group, String appId )
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

	public List<ScheduleEntity> findAllByUsername( String username )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "username", username ) );
		return criteria.list();
	}
}

