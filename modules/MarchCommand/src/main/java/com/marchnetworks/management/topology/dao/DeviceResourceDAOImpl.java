package com.marchnetworks.management.topology.dao;

import com.marchnetworks.management.topology.model.DeviceResourceEntity;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

public class DeviceResourceDAOImpl extends AbstractResourceDAOImpl<DeviceResourceEntity> implements DeviceResourceDAO
{
	public DeviceResourceEntity findByDeviceId( String deviceId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType ).createCriteria( "device" ).add( Restrictions.eq( "deviceId", Long.valueOf( deviceId ) ) ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE );

		return ( DeviceResourceEntity ) criteria.uniqueResult();
	}

	public Long findResourceIdByDeviceId( String deviceId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		ProjectionList projections = Projections.projectionList();
		projections.add( Projections.property( "id" ), "deviceResourceId" );

		Criteria criteria = session.createCriteria( entityType ).setProjection( projections ).add( Restrictions.eq( "device.deviceId", Long.valueOf( deviceId ) ) );

		return ( Long ) criteria.uniqueResult();
	}
}

