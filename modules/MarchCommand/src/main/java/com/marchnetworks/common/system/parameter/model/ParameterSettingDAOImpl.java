package com.marchnetworks.common.system.parameter.model;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.List;

class ParameterSettingDAOImpl extends GenericHibernateDAO<ParameterSettingEntity, String> implements ParameterSettingDAO
{
	public boolean isParameterSettingEmpty()
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.setReadOnly( true );
		return !checkExists( crit );
	}

	public List<ParameterSettingEntity> findAllByName( String... paramNames )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.add( Restrictions.in( "parameterName", paramNames ) );

		return crit.list();
	}
}
