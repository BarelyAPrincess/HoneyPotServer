package com.marchnetworks.esm.common.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.esm.common.model.ProfileEntity;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class ProfileDAOImpl extends GenericHibernateDAO<ProfileEntity, Long> implements ProfileDAO
{
	public ProfileEntity findSuperAdminProfile()
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "superAdmin", Boolean.TRUE ) );

		return ( ProfileEntity ) criteria.uniqueResult();
	}

	public ProfileEntity findByName( String name )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "name", name ) );

		return ( ProfileEntity ) criteria.uniqueResult();
	}
}
