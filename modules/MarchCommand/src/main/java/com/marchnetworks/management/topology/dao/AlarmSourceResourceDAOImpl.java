package com.marchnetworks.management.topology.dao;

import com.marchnetworks.management.topology.model.AlarmSourceResourceEntity;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

public class AlarmSourceResourceDAOImpl extends AbstractResourceDAOImpl<AlarmSourceResourceEntity> implements AlarmSourceResourceDAO
{
	public AlarmSourceResourceEntity findByAlarmSourceId( Long alarmSourceId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.createCriteria( "alarmSource" ).add( Restrictions.eq( "id", alarmSourceId ) ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE );

		return ( AlarmSourceResourceEntity ) criteria.uniqueResult();
	}
}

