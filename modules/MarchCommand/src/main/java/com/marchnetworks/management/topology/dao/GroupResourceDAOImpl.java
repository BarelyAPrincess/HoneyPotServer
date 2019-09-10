package com.marchnetworks.management.topology.dao;

import com.marchnetworks.management.topology.model.GroupEntity;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class GroupResourceDAOImpl extends AbstractResourceDAOImpl<GroupEntity> implements GroupResourceDAO
{
	public List<GroupEntity> findAllEmptyResourceNodes()
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.isEmpty( "associationsMap" ) );

		return criteria.list();
	}
}

