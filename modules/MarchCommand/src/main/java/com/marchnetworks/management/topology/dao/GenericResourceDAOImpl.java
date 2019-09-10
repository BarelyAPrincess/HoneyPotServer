package com.marchnetworks.management.topology.dao;

import com.marchnetworks.management.topology.model.GenericResourceEntity;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class GenericResourceDAOImpl extends AbstractResourceDAOImpl<GenericResourceEntity> implements GenericResourceDAO
{
	public List<Long> findIdsByOwner( String owner )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.eq( "owner", owner ) );
		criteria.setProjection( Projections.distinct( Projections.id() ) );
		criteria.setReadOnly( true );

		List<Long> result = criteria.list();
		return result;
	}
}

