package com.marchnetworks.casemanagement.dao;

import com.marchnetworks.casemanagement.model.LocalGroupEntity;
import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class LocalGroupDAOImpl extends GenericHibernateDAO<LocalGroupEntity, Long> implements LocalGroupDAO
{
	public List<LocalGroupEntity> findByUser( String username )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.like( "membersString", CoreJsonSerializer.toJson( username ), MatchMode.ANYWHERE ) );

		return criteria.list();
	}

	public List<Long> findIdsByUser( String username )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType.getName() );

		criteria.add( Restrictions.like( "membersString", CoreJsonSerializer.toJson( username ), MatchMode.ANYWHERE ) );
		criteria.setProjection( Projections.distinct( Projections.id() ) );

		List<Long> results = criteria.list();
		return results;
	}

	public LocalGroupEntity findLocalGroupByName( String localGroupName )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType.getName() );

		criteria.add( Restrictions.ilike( "name", localGroupName, MatchMode.EXACT ) );

		return ( LocalGroupEntity ) criteria.uniqueResult();
	}
}

