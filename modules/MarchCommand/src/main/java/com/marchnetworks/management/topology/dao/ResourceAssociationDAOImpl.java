package com.marchnetworks.management.topology.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.management.topology.model.ResourceAssociationEntity;
import com.marchnetworks.management.topology.model.ResourceEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class ResourceAssociationDAOImpl extends GenericHibernateDAO<ResourceAssociationEntity, Long> implements ResourceAssociationDAO
{
	public ResourceAssociationEntity findAssociation( Long firstResourceId, Long secondResourceId, String type )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.eq( "firstResource.id", firstResourceId ) ).add( Restrictions.eq( "secondResource.id", secondResourceId ) ).add( Restrictions.eq( "type", type ) );

		List<ResourceAssociationEntity> results = criteria.list();

		if ( results.size() > 0 )
		{
			return ( ResourceAssociationEntity ) results.get( 0 );
		}
		return null;
	}

	public Map<String, ResourceAssociationEntity> findAssociations( ResourceEntity resource, String type )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.and( Restrictions.eq( "firstResource", resource ), Restrictions.eq( "type", type ) ) );

		Map<String, ResourceAssociationEntity> ret = new HashMap<String, ResourceAssociationEntity>();

		for ( ResourceAssociationEntity association : ( List<ResourceAssociationEntity> ) criteria.list() )
		{
			ret.put( association.getSecondResource().getIdAsString(), association );
		}

		return ret;
	}

	public List<ResourceAssociationEntity> findParentAssociations( List<ResourceEntity> resources )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.in( "secondResource", resources ) );
		List<ResourceAssociationEntity> associations = criteria.list();
		return associations;
	}

	public Map<String, ResourceEntity> findAssociatedResources( ResourceEntity resource, String type )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.and( Restrictions.eq( "firstResource", resource ), Restrictions.eq( "type", type ) ) );

		Map<String, ResourceEntity> ret = new HashMap();
		for ( ResourceAssociationEntity association : ( List<ResourceAssociationEntity> ) criteria.list() )
		{
			ret.put( association.getSecondResource().getIdAsString(), association.getSecondResource() );
		}

		return ret;
	}

	public List<ResourceAssociationEntity> findAllDetached()
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).setFetchMode( "firstResource", FetchMode.SELECT ).setFetchMode( "secondResource", FetchMode.JOIN );
		criteria.setReadOnly( true );
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		List<ResourceAssociationEntity> result = criteria.list();

		for ( ResourceAssociationEntity criteriaResult : result )
		{
			session.evict( criteriaResult );
		}

		return result;
	}
}

