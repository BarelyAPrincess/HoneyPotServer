package com.marchnetworks.app.dao;

import com.marchnetworks.app.model.AppEntity;
import com.marchnetworks.command.common.dao.GenericHibernateDAO;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class AppDAOImpl extends GenericHibernateDAO<AppEntity, Long> implements AppDAO
{
	private static final Logger LOG = LoggerFactory.getLogger( AppDAOImpl.class );

	public AppEntity findByGuid( String guid )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.eq( "guid", guid ) );
		crit.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		AppEntity app = ( AppEntity ) crit.uniqueResult();
		if ( ( app != null ) && ( !app.readIdentity() ) )
		{
			LOG.warn( "App is corrputed in database, App id:" + app.getGuid() + ", identity dump:" + app.getIdentityAsString() );
			return null;
		}

		return app;
	}

	public List<AppEntity> findAll()
	{
		Criteria criteria = ( ( Session ) entityManager.getDelegate() ).createCriteria( entityType );
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		List<AppEntity> list = criteria.list();
		for ( Iterator<AppEntity> iterator = list.iterator(); iterator.hasNext(); )
		{
			AppEntity app = ( AppEntity ) iterator.next();
			if ( !app.readIdentity() )
			{
				LOG.warn( "App is corrputed in database, App id:" + app.getGuid() + ", identity dump:" + app.getIdentityAsString() );
				iterator.remove();
			}
		}
		return list;
	}
}
