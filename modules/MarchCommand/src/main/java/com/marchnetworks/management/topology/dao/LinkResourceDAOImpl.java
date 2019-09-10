package com.marchnetworks.management.topology.dao;

import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.management.topology.model.LinkResourceEntity;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class LinkResourceDAOImpl extends AbstractResourceDAOImpl<LinkResourceEntity> implements LinkResourceDAO
{
	public List<LinkResourceEntity> findAllByLinkedResourceId( Long linkedResourceId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		String jsonId = CoreJsonSerializer.toJson( String.valueOf( linkedResourceId ) );
		criteria.add( Restrictions.like( "linkedResourceIds", jsonId, MatchMode.ANYWHERE ) );

		List<LinkResourceEntity> results = criteria.list();
		return results;
	}
}

