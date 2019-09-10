package com.marchnetworks.management.instrumentation.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.management.instrumentation.model.Channel;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

class ChannelDAOImpl extends GenericHibernateDAO<Channel, Long> implements ChannelDAO
{
	public List<Channel> findByChannelId( String aChannelId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "channelId", aChannelId ) );
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		return criteria.list();
	}
}

