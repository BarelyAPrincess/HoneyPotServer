package com.marchnetworks.management.topology.dao;

import com.marchnetworks.management.topology.model.ChannelResourceEntity;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

class ChannelResourceDAOImpl extends AbstractResourceDAOImpl<ChannelResourceEntity> implements ChannelResourceDAO
{
	public ChannelResourceEntity getChannel( String deviceId, String channelId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria crit = session.createCriteria( entityType ).createCriteria( "channel" ).add( Restrictions.eq( "channelId", channelId ) ).createCriteria( "device" ).add( Restrictions.eq( "deviceId", Long.valueOf( deviceId ) ) );

		crit.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		List<ChannelResourceEntity> channelList = crit.list();

		if ( ( channelList != null ) && ( !channelList.isEmpty() ) )
		{
			return ( ChannelResourceEntity ) channelList.get( 0 );
		}
		return null;
	}

	public Long getChannelId( String deviceId, String channelId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType );
		criteria.setProjection( Projections.id() ).createCriteria( "channel" ).add( Restrictions.eq( "channelId", channelId ) ).createCriteria( "device" ).add( Restrictions.eq( "deviceId", Long.valueOf( deviceId ) ) );

		return ( Long ) criteria.uniqueResult();
	}
}

