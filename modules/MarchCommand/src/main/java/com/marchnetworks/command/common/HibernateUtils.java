package com.marchnetworks.command.common;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.engine.LoadQueryInfluencers;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.hql.QueryTranslator;
import org.hibernate.hql.QueryTranslatorFactory;
import org.hibernate.hql.ast.ASTQueryTranslatorFactory;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.impl.SessionImpl;
import org.hibernate.loader.OuterJoinLoader;
import org.hibernate.loader.criteria.CriteriaLoader;
import org.hibernate.persister.entity.OuterJoinLoadable;

import java.lang.reflect.Field;
import java.util.Collections;

public class HibernateUtils
{
	public static final int SESSION_FLUSH_THRESHOLD = 10000;

	public static String getSql( Criteria crit )
	{
		try
		{
			CriteriaImpl criteriaImpl = ( CriteriaImpl ) crit;
			SessionImpl sessionImpl = ( SessionImpl ) criteriaImpl.getSession();
			SessionFactoryImplementor sessionFactory = ( SessionFactoryImplementor ) sessionImpl.getSessionFactory();
			String[] criteriaImplementors = sessionFactory.getImplementors( criteriaImpl.getEntityOrClassName() );
			CriteriaLoader criteriaLoader = new CriteriaLoader( ( OuterJoinLoadable ) sessionFactory.getEntityPersister( criteriaImplementors[0] ), sessionFactory, criteriaImpl, criteriaImplementors[0], LoadQueryInfluencers.NONE );

			Field sqlField = OuterJoinLoader.class.getDeclaredField( "sql" );

			sqlField.setAccessible( true );
			return ( String ) sqlField.get( criteriaLoader );
		}
		catch ( Exception localException )
		{
		}

		return null;
	}

	public static String getSql( String hqlQuery, Session session )
	{
		if ( ( hqlQuery != null ) && ( hqlQuery.length() > 0 ) )
		{
			QueryTranslatorFactory queryTranslatorFactory = new ASTQueryTranslatorFactory();
			SessionFactoryImplementor sessionFactory = ( SessionFactoryImplementor ) session.getSessionFactory();
			QueryTranslator queryTranslator = queryTranslatorFactory.createQueryTranslator( hqlQuery.toString(), hqlQuery.toString(), Collections.EMPTY_MAP, sessionFactory );
			queryTranslator.compile( Collections.EMPTY_MAP, false );
			return queryTranslator.getSQLString();
		}
		return null;
	}
}
