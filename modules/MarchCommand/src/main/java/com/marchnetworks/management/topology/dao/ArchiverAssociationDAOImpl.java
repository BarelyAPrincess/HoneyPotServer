package com.marchnetworks.management.topology.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.management.topology.model.ArchiverAssociationEntity;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class ArchiverAssociationDAOImpl extends GenericHibernateDAO<ArchiverAssociationEntity, Long> implements ArchiverAssociationDAO
{
	public ArchiverAssociationEntity findByArchiverId( Long archiverResourceId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( ArchiverAssociationEntity.class );
		criteria.add( Restrictions.eq( "archiverResourceId", archiverResourceId ) );

		return ( ArchiverAssociationEntity ) criteria.uniqueResult();
	}

	public List<Long> findAllArchiverIds()
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( ArchiverAssociationEntity.class );
		criteria.setProjection( Projections.distinct( Projections.property( "archiverResourceId" ) ) );
		return criteria.list();
	}
}

