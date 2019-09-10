package com.marchnetworks.management.firmware.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.management.firmware.data.FirmwareGroupEnum;
import com.marchnetworks.management.firmware.model.GroupFirmwareEntity;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

public class GroupFirmwareDAOImpl extends GenericHibernateDAO<GroupFirmwareEntity, Long> implements GroupFirmwareDAO
{
	public GroupFirmwareEntity findByGroup( FirmwareGroupEnum groupId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE ).add( Restrictions.eq( "groupId", groupId ) );

		List<GroupFirmwareEntity> result = criteria.list();
		if ( ( result != null ) && ( result.size() > 0 ) )
		{
			return ( GroupFirmwareEntity ) result.get( 0 );
		}
		return null;
	}

	public boolean isFileAssociated( String fwId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE ).add( Restrictions.eq( "targetFirmwareId", Long.valueOf( fwId ) ) );

		List<GroupFirmwareEntity> result = criteria.list();
		if ( ( result != null ) && ( result.size() > 0 ) )
		{
			return true;
		}
		return false;
	}
}

