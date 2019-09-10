package com.marchnetworks.management.instrumentation.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.management.instrumentation.model.DeviceOutputEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public abstract class AbstractDeviceOutputDAOImpl<T extends DeviceOutputEntity> extends GenericHibernateDAO<T, Long> implements DeviceOutputDAO<T>
{
	public List<T> findAllByDeviceId( Long deviceId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.add( Restrictions.eq( "deviceId", deviceId ) );

		return crit.list();
	}

	public Set<String> findAllOutputIdsByDeviceId( Long deviceId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.add( Restrictions.eq( "deviceId", deviceId ) );

		ProjectionList projections = Projections.projectionList();
		projections.add( Projections.property( "outputId" ), "outputId" );
		crit.setProjection( projections );

		Set<String> resultSet = new HashSet();
		resultSet.addAll( crit.list() );

		return resultSet;
	}

	public T findByDeviceAndOutputId( Long deviceId, String outputId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType ).add( Restrictions.eq( "deviceId", deviceId ) ).add( Restrictions.eq( "outputId", outputId ) );

		return ( T ) crit.uniqueResult();
	}

	public void deleteByDeviceId( Long deviceId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder query = new StringBuilder();
		query.append( "delete " );
		query.append( entityType.getName() );
		query.append( " where deviceId = :deviceId " );

		Query deleteQuery = session.createQuery( query.toString() );
		deleteQuery.setParameter( "deviceId", deviceId );
		deleteQuery.executeUpdate();
	}
}

