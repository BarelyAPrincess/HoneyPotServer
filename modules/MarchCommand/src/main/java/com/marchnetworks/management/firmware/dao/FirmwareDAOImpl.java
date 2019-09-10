package com.marchnetworks.management.firmware.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.management.firmware.data.UpdateStateEnum;
import com.marchnetworks.management.firmware.model.FirmwareEntity;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class FirmwareDAOImpl extends GenericHibernateDAO<FirmwareEntity, Long> implements FirmwareDAO
{
	public FirmwareEntity findByDeviceId( String deviceId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria crit = session.createCriteria( entityType );
		crit.add( Restrictions.eq( "deviceId", Long.valueOf( Long.parseLong( deviceId ) ) ) );

		crit.setMaxResults( 1 );
		FirmwareEntity result = ( FirmwareEntity ) crit.uniqueResult();
		return result;
	}

	public List<FirmwareEntity> findByScheduleId( Long scheduleId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append( " from " );
		hqlQuery.append( entityType.getName() );
		hqlQuery.append( " where schedulerId = ? " );
		Query query = session.createQuery( hqlQuery.toString() );
		query.setParameter( 0, scheduleId );

		List<FirmwareEntity> result = query.list();

		return result;
	}

	public List<FirmwareEntity> findAllByState( UpdateStateEnum state )
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append( " from " );
		hqlQuery.append( entityType.getName() );
		hqlQuery.append( " where updateState = ? " );
		Query query = session.createQuery( hqlQuery.toString() );
		query.setParameter( 0, state );

		List<FirmwareEntity> result = query.list();

		return result;
	}

	public List<FirmwareEntity> findAllReadyUpgrades()
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria crit = session.createCriteria( entityType );

		UpdateStateEnum[] states = {UpdateStateEnum.FIRMWARE_UPGRADE_IDLE, UpdateStateEnum.FIRMWARE_UPGRADE_WAITING};
		crit.add( Restrictions.in( "updateState", states ) );
		crit.add( Restrictions.isNotNull( "targetFirmwareId" ) );

		List<FirmwareEntity> result = crit.list();

		return result;
	}

	public List<FirmwareEntity> findAllUnfinishedUpgrade()
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );
		UpdateStateEnum[] states = {UpdateStateEnum.FIRMWARE_UPGRADE_PENDING, UpdateStateEnum.FIRMWARE_UPGRADE_WAITING_ACCEPT, UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED};
		crit.add( Restrictions.in( "updateState", states ) );

		List<FirmwareEntity> result = crit.list();
		return result;
	}

	public boolean isFileAssociated( String fwId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria crit = session.createCriteria( entityType );
		crit.add( Restrictions.eq( "targetFirmwareId", Long.valueOf( Long.parseLong( fwId ) ) ) );
		crit.add( Restrictions.ne( "updateState", UpdateStateEnum.FIRMWARE_UPGRADE_COMPLETED ) );

		List<FirmwareEntity> result = crit.list();
		if ( ( result != null ) && ( result.size() > 0 ) )
		{
			return true;
		}
		return false;
	}

	public boolean isScheduleInUse( Long id )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "schedulerId", id ) );
		return checkExists( criteria );
	}
}

