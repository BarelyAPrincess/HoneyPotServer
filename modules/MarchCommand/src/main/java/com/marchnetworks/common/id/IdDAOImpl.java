package com.marchnetworks.common.id;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class IdDAOImpl extends GenericHibernateDAO<IdEntity, Long> implements IdDAO
{
	public IdEntity findByTableName( String tableName )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "tableName", tableName ) );
		return ( IdEntity ) criteria.uniqueResult();
	}
}
