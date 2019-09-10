package com.marchnetworks.management.topology.dao;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.command.common.topology.data.Store;
import com.marchnetworks.management.topology.model.GenericStorageEntity;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

public class GenericStorageDAOImpl extends GenericHibernateDAO<GenericStorageEntity, Long> implements GenericStorageDAO
{
	public GenericStorageEntity findByIdentifiers( Store store, String objectId, String appId, String userId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		setCriteriaFromIdentifiers( crit, store, objectId, appId, userId );

		GenericStorageEntity storageEntity = ( GenericStorageEntity ) crit.uniqueResult();
		return storageEntity;
	}

	public Long findIdByIdentifiers( Store store, String objectId, String appId, String userId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		setCriteriaFromIdentifiers( crit, store, objectId, appId, userId );

		crit.setProjection( Projections.id() );
		Long id = ( Long ) crit.uniqueResult();
		return id;
	}

	public boolean checkExists( Store store, String objectId, String appId, String userId )
	{
		Long id = findIdByIdentifiers( store, objectId, appId, userId );
		if ( id != null )
		{
			return true;
		}
		return false;
	}

	private void setCriteriaFromIdentifiers( Criteria crit, Store store, String objectId, String appId, String userId )
	{
		crit.add( Restrictions.eq( "store", store ) );
		crit.add( Restrictions.eq( "objectId", objectId ) );
		if ( !CommonAppUtils.isNullOrEmptyString( appId ) )
		{
			crit.add( Restrictions.eq( "appId", appId ) );
		}
		else
		{
			crit.add( Restrictions.isNull( "appId" ) );
		}

		if ( store == Store.USER )
		{
			crit.add( Restrictions.eq( "userId", userId ) );
		}
	}

	public List<GenericStorageEntity> findAllByIdentifiers( Store store, String appId, String username )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.eq( "this.store", store ) );

		if ( CommonAppUtils.isNullOrEmptyString( appId ) )
		{
			crit.add( Restrictions.isNull( "this.appId" ) );
		}
		else if ( !appId.equals( "*" ) )
		{
			crit.add( Restrictions.eq( "this.appId", appId ) );
		}

		if ( ( store == Store.USER ) && ( !username.equals( "*" ) ) )
		{
			crit.add( Restrictions.eq( "this.userId", username ) );
		}

		return getProjection( crit );
	}

	public List<GenericStorageEntity> findByUserId( String username )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.eq( "this.store", Store.USER ) );
		crit.add( Restrictions.eq( "this.userId", username ) );

		return getProjection( crit );
	}

	private List<GenericStorageEntity> getProjection( Criteria crit )
	{
		ProjectionList projections = Projections.projectionList();
		projections.add( Projections.property( "id" ), "id" );
		projections.add( Projections.property( "store" ), "store" );
		projections.add( Projections.property( "objectId" ), "objectId" );
		projections.add( Projections.property( "userId" ), "userId" );
		projections.add( Projections.property( "appId" ), "appId" );
		projections.add( Projections.property( "size" ), "size" );
		crit.setProjection( projections );

		crit.setResultTransformer( Transformers.aliasToBean( GenericStorageEntity.class ) );

		List<GenericStorageEntity> storageEntityList = crit.list();
		return storageEntityList;
	}

	public long getSize( Store store, String userId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.add( Restrictions.eq( "store", store ) );
		if ( store == Store.USER )
		{
			crit.add( Restrictions.eq( "userId", userId ) );
		}
		crit.setProjection( Projections.sum( "size" ) );

		Long size = ( Long ) crit.uniqueResult();
		if ( size == null )
		{
			return 0L;
		}
		return size.longValue();
	}

	public long getTotalSize()
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.setProjection( Projections.sum( "size" ) );

		Long size = ( Long ) crit.uniqueResult();
		if ( size == null )
		{
			return 0L;
		}
		return size.longValue();
	}

	public long getTotalUserSize()
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.add( Restrictions.isNotNull( "userId" ) );
		crit.setProjection( Projections.sum( "size" ) );

		Long size = ( Long ) crit.uniqueResult();
		if ( size == null )
		{
			return 0L;
		}
		return size.longValue();
	}

	public int deleteByUserId( String userId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append( " delete " );
		hqlQuery.append( entityType.getName() );
		hqlQuery.append( " where userId = :userId " );

		Query deleteQuery = session.createQuery( hqlQuery.toString() ).setString( "userId", userId );

		int deletedCount = deleteQuery.executeUpdate();
		return deletedCount;
	}

	public int deleteByAppId( String appId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append( " delete " );
		hqlQuery.append( entityType.getName() );
		hqlQuery.append( " where appId = :appId " );

		Query deleteQuery = session.createQuery( hqlQuery.toString() ).setString( "appId", appId );

		int deletedCount = deleteQuery.executeUpdate();
		return deletedCount;
	}
}

