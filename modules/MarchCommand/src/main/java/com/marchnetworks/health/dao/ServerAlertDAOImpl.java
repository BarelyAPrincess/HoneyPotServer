package com.marchnetworks.health.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.common.types.AlertUserStateEnum;
import com.marchnetworks.health.alerts.ServerAlertEntity;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class ServerAlertDAOImpl extends GenericHibernateDAO<ServerAlertEntity, Long> implements ServerAlertDAO
{
	public List<ServerAlertEntity> findAllUserOpenAlertsByServer( String serverId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.setReadOnly( true );
		criteria.add( Restrictions.like( "serverId", serverId ) );
		criteria.add( Restrictions.eq( "userState", AlertUserStateEnum.OPEN ) );

		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		return criteria.list();
	}

	public ServerAlertEntity findUserOpenAlertByIdentifiers( String alertCode, String serverId, String sourceId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.like( "alertCode", alertCode ) );
		criteria.add( Restrictions.like( "serverId", serverId ) );
		criteria.add( Restrictions.like( "sourceId", sourceId ) );
		criteria.add( Restrictions.eq( "userState", AlertUserStateEnum.OPEN ) );

		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		return ( ServerAlertEntity ) criteria.uniqueResult();
	}

	public List<ServerAlertEntity> findUnresolvedAlertsByIdentifiers( String alertCode, String serverId, String sourceId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.like( "alertCode", alertCode ) );
		criteria.add( Restrictions.like( "serverId", serverId ) );
		criteria.add( Restrictions.like( "sourceId", sourceId ) );
		criteria.add( Restrictions.eq( "deviceState", Boolean.TRUE ) );

		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		return criteria.list();
	}
}
