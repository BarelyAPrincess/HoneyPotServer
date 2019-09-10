package com.marchnetworks.management.config.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.management.config.DeviceImageState;
import com.marchnetworks.management.config.DeviceSnapshotState;
import com.marchnetworks.management.config.model.DeviceConfig;
import com.marchnetworks.management.config.model.DeviceImage;
import com.marchnetworks.management.instrumentation.data.DeviceNetworkInfoType;
import com.marchnetworks.management.instrumentation.model.Device;
import com.marchnetworks.management.instrumentation.model.DeviceNetworkInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

public class DeviceConfigDAOImpl extends GenericHibernateDAO<DeviceConfig, Long> implements DeviceConfigDAO
{
	public DeviceConfig findByDeviceId( String deviceId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append( " from " );
		hqlQuery.append( entityType.getName() );
		hqlQuery.append( " where device.deviceId = ? " );
		org.hibernate.Query query = session.createQuery( hqlQuery.toString() );
		query.setParameter( 0, Long.valueOf( Long.parseLong( deviceId ) ) );

		List<DeviceConfig> result = query.list();

		if ( ( result != null ) && ( result.size() > 0 ) )
		{
			return ( DeviceConfig ) result.get( 0 );
		}
		return null;
	}

	public List<DeviceConfig> findAllByDeviceId( String deviceId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Set<Long> duplicateDevices = new HashSet();
		Set<Long> duplicateMACs = new HashSet();
		Set<Long> duplicateNames = new HashSet();

		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append( " from " );
		hqlQuery.append( DeviceNetworkInfo.class.getName() );
		hqlQuery.append( " devicenetworkinfo where devicenetworkinfo.device.deviceId = ? and devicenetworkinfo.networkInfoType in ('MAC_ADDRESS','NETWORK_NAME')" );
		org.hibernate.Query query = session.createQuery( hqlQuery.toString() );
		query.setParameter( 0, Long.valueOf( Long.parseLong( deviceId ) ) );
		List<DeviceNetworkInfo> result = query.list();

		for ( DeviceNetworkInfo aConfig : result )
		{
			StringBuilder hqlQuery2 = new StringBuilder();
			hqlQuery2.append( " from " );
			hqlQuery2.append( DeviceNetworkInfo.class.getName() );
			hqlQuery2.append( " devicenetworkinfo where devicenetworkinfo.device.deviceId != ? and devicenetworkinfo.networkInfoType= ? and devicenetworkinfo.value= ?" );
			org.hibernate.Query query2 = session.createQuery( hqlQuery2.toString() );
			query2.setParameter( 0, Long.valueOf( Long.parseLong( deviceId ) ) );
			query2.setParameter( 1, aConfig.getNetworkInfoType() );
			query2.setParameter( 2, aConfig.getValue() );
			List<DeviceNetworkInfo> result2 = query2.list();

			for ( DeviceNetworkInfo networkInfoDuplicate : result2 )
			{
				Long dupDeviceId = Long.valueOf( Long.parseLong( networkInfoDuplicate.getDevice().getDeviceId() ) );
				if ( networkInfoDuplicate.getNetworkInfoType().equals( DeviceNetworkInfoType.MAC_ADDRESS ) )
				{
					duplicateMACs.add( dupDeviceId );
				}
				else
				{
					duplicateNames.add( dupDeviceId );
				}
			}
		}

		for ( Long lDeviceId : duplicateMACs )
		{
			if ( duplicateNames.contains( lDeviceId ) )
			{
				duplicateDevices.add( lDeviceId );
			}
		}
		duplicateDevices.add( Long.valueOf( Long.parseLong( deviceId ) ) );

		StringBuilder hqlQuery3 = new StringBuilder();
		hqlQuery3.append( " from " );
		hqlQuery3.append( entityType.getName() );
		hqlQuery3.append( " where device.deviceId in (:deviceIDs)" );
		org.hibernate.Query query3 = session.createQuery( hqlQuery3.toString() );
		query3.setParameterList( "deviceIDs", duplicateDevices );

		return query3.list();
	}

	public List<DeviceConfig> findByImage( DeviceImage image )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE );
		crit.add( Restrictions.eq( "image", image ) );

		List<DeviceConfig> list = crit.list();
		return list;
	}

	public List<DeviceConfig> findAllByImage( DeviceImage image )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE );
		crit.add( Restrictions.eq( "image", image ) );

		List<DeviceConfig> list = crit.list();
		return list;
	}

	public List<DeviceConfig> findAll()
	{
		javax.persistence.Query query = entityManager.createQuery( buildQueryCriteria() );
		return buildResultSetForFindAll( query );
	}

	public List<DeviceConfig> findAllDetached()
	{
		Session session = ( Session ) entityManager.getDelegate();
		javax.persistence.Query query = entityManager.createQuery( buildQueryCriteria() );
		List<DeviceConfig> result = buildResultSetForFindAll( query );
		for ( DeviceConfig deviceConfig : result )
		{
			session.evict( deviceConfig );
		}
		return result;
	}

	private List<DeviceConfig> buildResultSetForFindAll( javax.persistence.Query query )
	{
		List<DeviceConfig> resultList = new ArrayList();

		Iterator<?> deviceConfigs = query.getResultList().iterator();
		while ( deviceConfigs.hasNext() )
		{
			Object[] tuple = ( Object[] ) deviceConfigs.next();
			DeviceConfig configuration = new DeviceConfig();
			configuration.setId( ( Long ) tuple[0] );
			if ( tuple[1] != null )
			{
				configuration.setImage( ( DeviceImage ) tuple[1] );
			}
			Device device = new Device();
			device.setDeviceId( ( Long ) tuple[2] );
			if ( tuple[3] != null )
			{
				device.setSoftwareVersion( ( String ) tuple[3] );
			}
			configuration.setDevice( device );
			configuration.setAssignState( ( DeviceImageState ) tuple[4] );
			configuration.setSnapshotState( ( DeviceSnapshotState ) tuple[5] );

			resultList.add( configuration );
		}

		return resultList;
	}

	private String buildQueryCriteria()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( " select deviceConfig.id, image, device.deviceId, device.softwareVersion, deviceConfig.assignState, deviceConfig.snapshotState " );
		sb.append( " from " );
		sb.append( entityType.getName() );
		sb.append( " deviceConfig left join deviceConfig.image image " );
		sb.append( " join deviceConfig.device device " );

		return sb.toString();
	}

	public List<DeviceConfig> findAllByAssignState( DeviceImageState... imageStates )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria crit = session.createCriteria( entityType ).add( Restrictions.in( "assignState", imageStates ) );
		crit.setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE );
		return crit.list();
	}

	public List<DeviceConfig> findAllByUpdateState( DeviceImageState... updateStates )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria crit = session.createCriteria( entityType ).add( Restrictions.in( "updateState", updateStates ) );
		crit.setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE );
		return crit.list();
	}
}
