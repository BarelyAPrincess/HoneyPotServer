package com.marchnetworks.audit.dao;

import com.marchnetworks.audit.data.AuditSearchQuery;
import com.marchnetworks.audit.model.AuditEntity;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.HibernateUtils;
import com.marchnetworks.command.common.dao.GenericHibernateDAO;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AuditDAOImpl<T extends AuditEntity> extends GenericHibernateDAO<T, Long> implements AuditDAO<T>
{
	private static final Logger LOG = LoggerFactory.getLogger( AuditDAOImpl.class );

	public List<T> getAudits( AuditSearchQuery auditViewCrit )
	{
		Session session = ( Session ) entityManager.getDelegate();
		session.setFlushMode( FlushMode.MANUAL );

		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append( " from " );
		hqlQuery.append( entityType.getName() ).append( " audit  " );
		hqlQuery.append( " Where " );

		boolean paramSet = false;
		if ( ( auditViewCrit.getUsernames() != null ) && ( auditViewCrit.getUsernames().length > 0 ) )
		{
			hqlQuery.append( " audit.usernameId in (:usernames)" );
			paramSet = true;
		}

		if ( ( auditViewCrit.getEventNames() != null ) && ( auditViewCrit.getEventNames().length > 0 ) )
		{
			if ( !paramSet )
			{
				hqlQuery.append( " audit.eventNameId in (:eventnames)" );
				paramSet = true;
			}
			else
			{
				hqlQuery.append( " and audit.eventNameId in (:eventnames) " );
			}
		}

		if ( !CommonAppUtils.isNullOrEmptyString( auditViewCrit.getUserRemoteAddress() ) )
		{
			if ( !paramSet )
			{
				hqlQuery.append( " audit.remoteAddressId = :address " );
				paramSet = true;
			}
			else
			{
				hqlQuery.append( " and audit.remoteAddressId = :address " );
			}
		}

		if ( auditViewCrit.getStartTime() > 0L )
		{
			if ( !paramSet )
			{
				hqlQuery.append( " audit.startTime >= :startTime " );
				paramSet = true;
			}
			else
			{
				hqlQuery.append( " and audit.startTime >= :startTime " );
			}
		}

		if ( auditViewCrit.getEndTime() > 0L )
		{
			if ( !paramSet )
			{
				hqlQuery.append( " audit.startTime <= :endTime " );
				paramSet = true;
			}
			else
			{
				hqlQuery.append( " and audit.startTime <= :endTime " );
			}
		}

		if ( ( auditViewCrit.getResourceIds() != null ) && ( auditViewCrit.getResourceIds().length > 0 ) )
		{
			if ( paramSet )
			{
				hqlQuery.append( " and " );
			}
			hqlQuery.append( " (" );
			for ( int i = 0; i < auditViewCrit.getResourceIds().length; i++ )
			{
				if ( i > 0 )
				{
					hqlQuery.append( " or " );
				}
				hqlQuery.append( " audit.resourceIds like :resParam" + i );
			}
			hqlQuery.append( ") " );
		}
		hqlQuery.append( " order by audit.startTime desc" );
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( HibernateUtils.getSql( hqlQuery.toString(), session ) );
		}
		Query query = session.createQuery( hqlQuery.toString() );

		if ( ( auditViewCrit.getUsernames() != null ) && ( auditViewCrit.getUsernames().length > 0 ) )
		{
			Integer[] params = new Integer[auditViewCrit.getUsernames().length];

			int i = 0;
			for ( String username : auditViewCrit.getUsernames() )
			{
				params[i] = Integer.valueOf( username.hashCode() );
				i++;
			}
			query.setParameterList( "usernames", params );
		}

		if ( ( auditViewCrit.getEventNames() != null ) && ( auditViewCrit.getEventNames().length > 0 ) )
		{
			Integer[] params = new Integer[auditViewCrit.getEventNames().length];

			int i = 0;
			for ( String eventName : auditViewCrit.getEventNames() )
			{
				params[i] = Integer.valueOf( eventName.hashCode() );
				i++;
			}
			query.setParameterList( "eventnames", params );
		}

		if ( !CommonAppUtils.isNullOrEmptyString( auditViewCrit.getUserRemoteAddress() ) )
		{
			query.setInteger( "address", auditViewCrit.getUserRemoteAddress().hashCode() );
		}
		if ( auditViewCrit.getStartTime() > 0L )
		{
			query.setLong( "startTime", auditViewCrit.getStartTime() );
		}

		if ( auditViewCrit.getEndTime() > 0L )
		{
			query.setLong( "endTime", auditViewCrit.getEndTime() );
		}

		if ( ( auditViewCrit.getResourceIds() != null ) && ( auditViewCrit.getResourceIds().length > 0 ) )
		{
			for ( int i = 0; i < auditViewCrit.getResourceIds().length; i++ )
			{
				StringBuilder sb = new StringBuilder( "%\"" );
				Long resourceId = auditViewCrit.getResourceIds()[i];
				sb.append( resourceId.toString() );
				sb.append( "\"%" );
				query.setString( "resParam" + i, sb.toString() );
			}
		}

		query.setMaxResults( 1000 );
		List<T> audits = query.list();

		return audits;
	}

	public int deleteOldAudits( long maxOldAge )
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append( " delete " );
		hqlQuery.append( entityType.getName() );
		hqlQuery.append( " where startTime <= ? " );

		Query deleteQuery = session.createQuery( hqlQuery.toString() ).setLong( 0, maxOldAge );

		int purgeCount = deleteQuery.executeUpdate();

		return purgeCount;
	}

	public T findAuditByTag( String tag )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.eq( "eventTag", tag ) );
		crit.setMaxResults( 1 );

		T entity = ( T ) crit.uniqueResult();
		return entity;
	}

	public List<T> findByResourceId( Long resourceId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.ilike( "resourceIds", '"' + resourceId.toString() + '"', MatchMode.ANYWHERE ) );

		List<T> results = crit.list();

		return results;
	}

	public void deleteByAppId( String appId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Query deleteQuery = session.createQuery( "delete " + entityType.getName() + " where `appId` = " + appId.hashCode() );

		deleteQuery.executeUpdate();
	}
}
