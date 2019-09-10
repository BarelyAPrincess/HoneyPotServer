package com.marchnetworks.audit.dao;

import com.google.common.collect.Sets;
import com.marchnetworks.audit.model.AuditDictionaryEntity;
import com.marchnetworks.audit.model.DeviceAuditEntity;
import com.marchnetworks.audit.model.ServerAuditEntity;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.dao.GenericHibernateDAO;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AuditDictionaryDAOImpl extends GenericHibernateDAO<AuditDictionaryEntity, Integer> implements AuditDictionaryDAO
{
	public Set<Integer> findMissingKeys( Set<Integer> keys )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Set<Integer> existingKeys = new HashSet();
		List<List<Integer>> splitParams = CollectionUtils.split( new ArrayList( keys ), 1000 );
		for ( List<Integer> paramList : splitParams )
		{
			Criteria crit = session.createCriteria( entityType );

			ProjectionList projections = Projections.projectionList();
			projections.add( Projections.property( "key" ) );
			crit.setProjection( projections );
			crit.add( Restrictions.in( "key", paramList ) );

			List<Integer> entries = crit.list();
			for ( Integer entry : entries )
			{
				existingKeys.add( entry );
			}
		}
		return Sets.difference( keys, existingKeys );
	}

	public Map<Integer, AuditDictionaryEntity> findValues( Set<Integer> keys )
	{
		Map<Integer, AuditDictionaryEntity> result = new HashMap();

		Session session = ( Session ) entityManager.getDelegate();

		List<List<Integer>> splitParams = CollectionUtils.split( new ArrayList( keys ), 1000 );
		for ( List<Integer> paramList : splitParams )
		{
			Criteria crit = session.createCriteria( entityType );
			crit.add( Restrictions.in( "key", paramList ) );

			List<AuditDictionaryEntity> entries = crit.list();
			for ( AuditDictionaryEntity entry : entries )
			{
				session.evict( entry );
				result.put( entry.getKey(), entry );
			}
		}

		return result;
	}

	public void deleteUnreferencedKeys()
	{
		Session session = ( Session ) entityManager.getDelegate();

		String serverAuditEntityName = ServerAuditEntity.class.getName();
		String deviceAuditEntityName = DeviceAuditEntity.class.getName();

		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append( "delete from " );
		hqlQuery.append( entityType.getName() );
		hqlQuery.append( " as ad where not exists" );
		hqlQuery.append( "( from " ).append( serverAuditEntityName ).append( " as a1 where ad.key = a1.eventNameId " );
		hqlQuery.append( "or ad.key = a1.usernameId " );
		hqlQuery.append( "or ad.key = a1.remoteAddressId " );
		hqlQuery.append( "or ad.key = a1.detailsId " );
		hqlQuery.append( "or ad.key = a1.appId) " );
		hqlQuery.append( "and not exists " );
		hqlQuery.append( "( from " ).append( deviceAuditEntityName ).append( " as a2 where ad.key = a2.eventNameId " );
		hqlQuery.append( "or ad.key = a2.usernameId " );
		hqlQuery.append( "or ad.key = a2.remoteAddressId " );
		hqlQuery.append( "or ad.key = a2.detailsId " );
		hqlQuery.append( "or ad.key = a2.sourceId) " );

		Query query = session.createQuery( hqlQuery.toString() );
		query.executeUpdate();
	}
}
