package com.marchnetworks.casemanagement.dao;

import com.marchnetworks.casemanagement.model.CaseNodeEntity;
import com.marchnetworks.command.common.dao.GenericHibernateDAO;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class CaseNodeDAOImpl extends GenericHibernateDAO<CaseNodeEntity, Long> implements CaseNodeDAO
{
	public List<CaseNodeEntity> findByResourceId( Long resourceId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.eq( "associatedResourceId", resourceId ) );

		List<CaseNodeEntity> result = criteria.list();
		return result;
	}

	public CaseNodeEntity findCaseNode( Long caseNodeId, boolean loadAttachment )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType );
		criteria.add( Restrictions.eq( "id", caseNodeId ) );

		if ( loadAttachment )
		{
			criteria.setFetchMode( "attachment", FetchMode.SELECT );
		}

		return ( CaseNodeEntity ) criteria.uniqueResult();
	}

	public CaseNodeEntity findByGuid( String jobId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.eq( "guid", jobId ) );

		return ( CaseNodeEntity ) criteria.uniqueResult();
	}

	public List<CaseNodeEntity> findAllWithGuid()
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.isNotNull( "guid" ) );

		List<CaseNodeEntity> results = criteria.list();
		evict( results );
		return results;
	}

	public List<CaseNodeEntity> findAllBySerial( String extractorSerial )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.eq( "extractorSerial", extractorSerial ) );

		List<CaseNodeEntity> results = criteria.list();
		return results;
	}
}
