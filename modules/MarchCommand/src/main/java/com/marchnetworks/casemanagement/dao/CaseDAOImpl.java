package com.marchnetworks.casemanagement.dao;

import com.marchnetworks.casemanagement.model.CaseEntity;
import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class CaseDAOImpl extends GenericHibernateDAO<CaseEntity, Long> implements CaseDAO
{
	public List<CaseEntity> getAllCases( String userName, boolean loadChildren )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.eq( "member", userName ) );

		if ( loadChildren )
		{
			criteria.setFetchMode( "childNodes", FetchMode.SELECT );
		}

		List<CaseEntity> result = criteria.list();
		return result;
	}

	public List<CaseEntity> getAllOrphan()
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.isNull( "member" ) );

		return criteria.list();
	}

	public List<CaseEntity> getAllCases( String userName, List<Long> groupsIds )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );
		Disjunction disjunction = Restrictions.disjunction();
		for ( Long groupId : groupsIds )
		{
			String jsonId = CoreJsonSerializer.toJson( groupId.toString() );
			disjunction.add( Restrictions.like( "groupsString", jsonId, MatchMode.ANYWHERE ) );
		}

		disjunction.add( Restrictions.eq( "member", userName ) );
		criteria.add( disjunction );

		return criteria.list();
	}

	public List<CaseEntity> getAllByGroupId( Long groupId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		Criteria criteria = session.createCriteria( entityType );

		criteria.add( Restrictions.like( "groupsString", CoreJsonSerializer.toJson( groupId.toString() ), MatchMode.ANYWHERE ) );
		return criteria.list();
	}
}
