package com.marchnetworks.management.instrumentation.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;
import com.marchnetworks.management.instrumentation.model.Device;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceDAOImpl extends GenericHibernateDAO<Device, String> implements DeviceDAO
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceDAOImpl.class );

	public Device findById( String deviceId )
	{
		try
		{
			return ( Device ) entityManager.find( entityType, Long.valueOf( deviceId ) );
		}
		catch ( NumberFormatException ex )
		{
			LOG.warn( "Invalid deviceId {}.", deviceId );
		}
		return null;
	}

	public CompositeDevice findByAddress( String deviceAddress )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE ).add( Restrictions.eq( "address", deviceAddress ) ).add( Restrictions.eq( "class", CompositeDevice.class ) ).setMaxResults( 1 );

		return ( CompositeDevice ) criteria.uniqueResult();
	}

	public CompositeDevice findByStationId( String stationId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE ).add( Restrictions.eq( "stationId", stationId ) ).add( Restrictions.eq( "class", CompositeDevice.class ) ).setMaxResults( 1 );

		return ( CompositeDevice ) criteria.uniqueResult();
	}

	public Set<String> findAllStationIds()
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.eq( "class", CompositeDevice.class ) );

		criteria.setProjection( Projections.property( "stationId" ) );

		Set<String> resultSet = new java.util.HashSet();
		resultSet.addAll( criteria.list() );

		return resultSet;
	}

	public Device findByAddressAndParent( String deviceAddress, CompositeDevice parentDevice )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE ).add( Restrictions.eq( "address", deviceAddress ) ).add( Restrictions.eq( "parentDevice", parentDevice ) ).setMaxResults( 1 );

		return ( Device ) criteria.uniqueResult();
	}

	public Device findByTimeCreated( long timeCreated )
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis( timeCreated );
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "timeCreated", c ) );
		criteria.setMaxResults( 1 );

		return ( Device ) criteria.uniqueResult();
	}

	public Device findByIdEagerDetached( String deviceId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.eq( "deviceId", Long.valueOf( deviceId ) ) );
		criteria.setFetchMode( "childDevices", FetchMode.JOIN );
		criteria.setFetchMode( "channels", FetchMode.JOIN );
		List<Device> result = criteria.list();

		if ( ( result != null ) && ( result.size() > 0 ) )
		{
			Device device = ( Device ) result.get( 0 );
			session.evict( device );
			return device;
		}

		return null;
	}

	public Device findByIdEager( String deviceId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.eq( "deviceId", Long.valueOf( deviceId ) ) );
		criteria.setFetchMode( "childDevices", FetchMode.JOIN );
		criteria.setFetchMode( "channels", FetchMode.JOIN );
		List<Device> result = criteria.list();

		if ( ( result != null ) && ( result.size() > 0 ) )
		{
			Device device = ( Device ) result.get( 0 );
			return device;
		}

		return null;
	}

	public List<CompositeDevice> findDeviceListFromConnectionTime( int checkInMinutes )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Calendar compareTime = DateUtils.getCurrentUTCTime();
		compareTime.add( 12, -1 * checkInMinutes );
		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.conjunction().add( Restrictions.isNull( "parentDevice" ) ).add( Restrictions.in( "registrationStatus", new RegistrationStatus[] {RegistrationStatus.REGISTERED, RegistrationStatus.PENDING_REPLACEMENT, RegistrationStatus.ERROR_REPLACEMENT} ) ).add( Restrictions.isNotNull( "lastCommunicationTime" ) ).add( Restrictions.lt( "lastCommunicationTime", compareTime ) ) );

		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		return criteria.list();
	}

	public List<CompositeDevice> findAllRegisteredAndReplacingDevices()
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.conjunction().add( Restrictions.eq( "class", CompositeDevice.class ) ).add( Restrictions.in( "registrationStatus", new RegistrationStatus[] {RegistrationStatus.REGISTERED, RegistrationStatus.PENDING_REPLACEMENT, RegistrationStatus.ERROR_REPLACEMENT} ) ) );

		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		return criteria.list();
	}

	public List<CompositeDevice> findAllRegisteredDevices()
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.conjunction().add( Restrictions.eq( "class", CompositeDevice.class ) ).add( Restrictions.eq( "registrationStatus", RegistrationStatus.REGISTERED ) ) );

		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		return criteria.list();
	}

	public Device findByNetworkAddressAndParent( String[] networkAddresses, CompositeDevice parentDevice )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE ).add( Restrictions.eq( "parentDevice", parentDevice ) ).createCriteria( "deviceNetworkInfos" );

		for ( int i = 0; i < networkAddresses.length; i++ )
		{
			String jsonString = CoreJsonSerializer.toJson( networkAddresses[i] );
			criteria.add( Restrictions.like( "value", jsonString, org.hibernate.criterion.MatchMode.ANYWHERE ) );
		}

		criteria.setMaxResults( 1 );
		return ( Device ) criteria.uniqueResult();
	}

	public List<CompositeDevice> findAllCompositeDevices()
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE ).add( Restrictions.eq( "class", CompositeDevice.class ) );

		return criteria.list();
	}

	public Integer updateDeviceAddressByDeviceId( String deviceId, String address )
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder hqlUpdate = new StringBuilder();
		hqlUpdate.append( " update " );
		hqlUpdate.append( entityType.getName() );
		hqlUpdate.append( " set address = :address where deviceId = :deviceId" );

		int updatedCount = session.createQuery( hqlUpdate.toString() ).setString( "deviceId", deviceId ).setString( "address", address ).executeUpdate();

		return Integer.valueOf( updatedCount );
	}

	public Integer updateLastConnectionTime( String deviceId, Calendar time )
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder hqlUpdate = new StringBuilder();
		hqlUpdate.append( " update " );
		hqlUpdate.append( entityType.getName() );
		hqlUpdate.append( " set lastCommunicationTime = :lastCommunicationTime where deviceId = :deviceId" );

		int updatedCount = session.createQuery( hqlUpdate.toString() ).setCalendar( "lastCommunicationTime", time ).setString( "deviceId", deviceId ).executeUpdate();

		return Integer.valueOf( updatedCount );
	}

	public Integer updateRegistrationStatus( String deviceId, RegistrationStatus registrationStatus, String errorMessage )
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder hqlUpdate = new StringBuilder();
		hqlUpdate.append( " update " );
		hqlUpdate.append( entityType.getName() );
		hqlUpdate.append( " set timeRegStatusChanged = :timeRegStatusChanged, " );
		hqlUpdate.append( " registrationErrorMessage = :registrationErrorMessage, " );
		hqlUpdate.append( " registrationStatus = :registrationStatus " );
		hqlUpdate.append( " where deviceId = :deviceId" );

		Calendar currentTime = DateUtils.getCurrentUTCTime();

		int updatedCount = session.createQuery( hqlUpdate.toString() ).setCalendar( "timeRegStatusChanged", currentTime ).setString( "registrationErrorMessage", currentTime.getTime().toString() + " : " + errorMessage ).setParameter( "registrationStatus", registrationStatus ).setString( "deviceId", deviceId ).executeUpdate();

		return Integer.valueOf( updatedCount );
	}

	public void updateAllTimeDeltas( long timeOffset, List<Long> exceptionIds )
	{
		StringBuilder hqlUpdate = new StringBuilder( " update " );
		hqlUpdate.append( CompositeDevice.class.getName() );
		hqlUpdate.append( " set timeDelta = (timeDelta+:timeOffset) " );
		if ( !exceptionIds.isEmpty() )
		{
			hqlUpdate.append( " where deviceId not in (:exceptionIds)" );
		}

		Session session = ( Session ) entityManager.getDelegate();
		Query query = session.createQuery( hqlUpdate.toString() ).setLong( "timeOffset", timeOffset );

		if ( !exceptionIds.isEmpty() )
		{
			query.setParameterList( "exceptionIds", exceptionIds );
		}
		query.executeUpdate();
	}

	public void updateTimeDelta( String deviceId, long timeDelta )
	{
		StringBuilder hqlUpdate = new StringBuilder( "update " );
		hqlUpdate.append( CompositeDevice.class.getName() );
		hqlUpdate.append( " set timeDelta = :timeDelta " );
		hqlUpdate.append( " where deviceId = :deviceId" );

		Session session = ( Session ) entityManager.getDelegate();
		Query query = session.createQuery( hqlUpdate.toString() ).setLong( "timeDelta", timeDelta ).setString( "deviceId", deviceId );

		query.executeUpdate();
	}

	public CompositeDevice findLastTestDevice()
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "class", CompositeDevice.class ) );
		criteria.add( Restrictions.eq( "modelName", "Simulated" ) );
		criteria.setProjection( Projections.max( "deviceId" ) );
		Long result = ( Long ) criteria.uniqueResult();
		if ( result == null )
		{
			return null;
		}
		return ( CompositeDevice ) findById( String.valueOf( result ) );
	}

	public Integer updateDeviceCapabilities( String deviceId, List<String> capabilities )
	{
		String sCapabilities = CoreJsonSerializer.toJson( capabilities );

		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder hqlUpdate = new StringBuilder();
		hqlUpdate.append( " update " );
		hqlUpdate.append( entityType.getName() );
		hqlUpdate.append( " set capabilities = :capabilities where deviceId = :deviceId" );

		Query query = session.createQuery( hqlUpdate.toString() );
		query = query.setString( "capabilities", sCapabilities );
		query = query.setString( "deviceId", deviceId );

		int updatedCount = query.executeUpdate();

		return Integer.valueOf( updatedCount );
	}
}

