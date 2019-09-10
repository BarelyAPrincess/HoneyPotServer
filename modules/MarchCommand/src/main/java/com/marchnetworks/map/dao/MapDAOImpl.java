package com.marchnetworks.map.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.map.model.MapEntity;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class MapDAOImpl extends GenericHibernateDAO<MapEntity, Long> implements MapDAO
{
	public MapEntity findByHash( byte[] hash )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.add( Restrictions.eq( "hash", hash ) );

		MapEntity alarmSource = ( MapEntity ) crit.uniqueResult();

		return alarmSource;
	}

	public boolean checkExists( Long id )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.idEq( id ) );

		return checkExists( crit );
	}
}

