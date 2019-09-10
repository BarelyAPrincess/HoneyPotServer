package com.marchnetworks.esm.common.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.command.common.user.data.MemberTypeEnum;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.esm.common.model.MemberEntity;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;

public class MemberDAOImpl extends GenericHibernateDAO<MemberEntity, Long> implements MemberDAO
{
	private static final Logger LOG = LoggerFactory.getLogger( MemberDAOImpl.class );

	public MemberEntity deleteMember( MemberEntity member )
	{
		if ( ( member == null ) || ( member.getId() == null ) )
		{
			throw new IllegalArgumentException( "Invalid arguments passed into deleteUser method" );
		}

		LOG.info( "Changing status of user ID={} for deleted.", member.getId() );
		super.delete( member );

		return member;
	}

	public MemberEntity findMemberByName( String accountName )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.ilike( "name", accountName, MatchMode.EXACT ) ).setFetchMode( "profileId", FetchMode.JOIN );

		MemberEntity aMember = null;
		try
		{
			List<MemberEntity> queryResult = criteria.list();
			if ( ( queryResult != null ) && ( !queryResult.isEmpty() ) )
			{
				aMember = ( MemberEntity ) queryResult.get( 0 );
			}
		}
		catch ( Exception e )
		{
			if ( ( e instanceof NoResultException ) )
			{
				LOG.debug( "No result found for query" );
			}
			else
			{
				LOG.debug( "Error finding user by account name", e );
			}
		}
		return aMember;
	}

	public List<String> findMembersNames( String... usernames )
	{
		Session session = ( Session ) entityManager.getDelegate();

		String[] params = new String[usernames.length];
		for ( int i = 0; i < usernames.length; i++ )
		{
			params[i] = usernames[i].toLowerCase();
		}

		StringBuilder sb = new StringBuilder();
		sb.append( " select name from " ).append( entityType.getName() );
		sb.append( " where lower(name) in (:params) " );
		Query query = session.createQuery( sb.toString() );
		query.setParameterList( "params", params );

		return query.list();
	}

	public List<MemberEntity> findAllMembersByRootResource( Long resourceId )
	{
		Session session = ( Session ) entityManager.getDelegate();

		String jsonID = CoreJsonSerializer.toJson( resourceId.toString() );

		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.disjunction().add( Restrictions.like( "systemRoots", jsonID, MatchMode.ANYWHERE ) ).add( Restrictions.like( "logicalRoots", jsonID, MatchMode.ANYWHERE ) ) );

		List<MemberEntity> queryResult = criteria.list();

		return queryResult;
	}

	public List<MemberEntity> findGroupByName( List<String> groupName )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.in( "name", groupName ) ).add( Restrictions.eq( "type", MemberTypeEnum.GROUP ) ).setFetchMode( "profileId", FetchMode.JOIN ).setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		List<MemberEntity> queryResult = criteria.list();
		return queryResult;
	}

	public List<MemberEntity> findGroupsByIds( List<Long> groupIds )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.in( "id", groupIds ) ).add( Restrictions.eq( "type", MemberTypeEnum.GROUP ) ).setFetchMode( "profileId", FetchMode.JOIN ).setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		List<MemberEntity> queryResult = criteria.list();
		return queryResult;
	}

	public List<MemberEntity> findAllMembersByProfileId( Long aProfileId )
	{
		List<MemberEntity> memberBySystemList = new ArrayList();
		if ( aProfileId != null )
		{
			Session session = ( Session ) entityManager.getDelegate();

			Criteria criteria = session.createCriteria( entityType ).add( Restrictions.eq( "profileId", aProfileId ) ).setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			memberBySystemList = criteria.list();
		}
		return memberBySystemList;
	}

	public List<MemberEntity> findAllMembersWithPersonalResource()
	{
		List<MemberEntity> members = new ArrayList();

		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.ne( "personalId", null ) ).setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		members = criteria.list();

		return members;
	}

	public List<MemberEntity> findAllMembersByProfileIdsDetached( List<Long> ids )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.in( "profileId", ids ) ).setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		List<MemberEntity> result = criteria.list();
		evict( result );
		return result;
	}

	public List<MemberEntity> findByGroupId( Long id )
	{
		Session session = ( Session ) entityManager.getDelegate();

		String jsonID = CoreJsonSerializer.toJson( id.toString() );

		Criteria criteria = session.createCriteria( entityType ).add( Restrictions.like( "groups", jsonID, MatchMode.ANYWHERE ) );

		List<MemberEntity> queryResult = criteria.list();

		return queryResult;
	}
}
