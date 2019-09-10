package com.marchnetworks.license.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.license.model.AppLicenseEntity;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AppLicenseDAOImpl extends GenericHibernateDAO<AppLicenseEntity, Long> implements AppLicenseDAO
{
	private static final Logger LOG = LoggerFactory.getLogger( AppLicenseDAOImpl.class );

	public AppLicenseEntity findOneByAppId( String appId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.like( "appId", appId ) );

		List<AppLicenseEntity> appLicenses = crit.list();
		AppLicenseEntity appLicense = null;
		if ( !appLicenses.isEmpty() )
		{
			appLicense = ( AppLicenseEntity ) appLicenses.get( 0 );
			if ( !appLicense.read() )
			{
				LOG.warn( "License is corrputed in database, App id:" + appLicense.getAppId() + ", license dump:" + appLicense.getLicenseString() );
				return null;
			}
		}
		return appLicense;
	}

	public List<AppLicenseEntity> findAllByAppId( String appId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.add( Restrictions.like( "appId", appId ) );

		List<AppLicenseEntity> appLicenses = crit.list();

		for ( AppLicenseEntity appLicense : appLicenses )
		{
			if ( !appLicense.read() )
			{
				LOG.warn( "License is corrputed in database, App id:" + appLicense.getAppId() + ", license dump:" + appLicense.getLicenseString() );
				return Collections.emptyList();
			}
		}

		return appLicenses;
	}

	public AppLicenseEntity findByLicenseId( String licenseId )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.like( "licenseId", licenseId ) );

		AppLicenseEntity appLicense = ( AppLicenseEntity ) crit.uniqueResult();
		if ( ( appLicense != null ) && ( !appLicense.read() ) )
		{
			LOG.warn( "License is corrputed in database, App id:" + appLicense.getAppId() + ", license dump:" + appLicense.getLicenseString() );
			return null;
		}

		return appLicense;
	}

	public List<AppLicenseEntity> findAll()
	{
		List<AppLicenseEntity> list = super.findAll();
		readLicenses( list );
		return list;
	}

	public List<AppLicenseEntity> findAllExcludeId( Long id )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );
		crit.add( Restrictions.ne( "id", id ) );

		List<AppLicenseEntity> appLicenses = crit.list();
		readLicenses( appLicenses );
		return appLicenses;
	}

	public List<AppLicenseEntity> findAllDetached()
	{
		List<AppLicenseEntity> list = super.findAllDetached();
		readLicenses( list );
		return list;
	}

	private void readLicenses( List<AppLicenseEntity> list )
	{
		for ( Iterator<AppLicenseEntity> iterator = list.iterator(); iterator.hasNext(); )
		{
			AppLicenseEntity appLicense = ( AppLicenseEntity ) iterator.next();
			if ( !appLicense.read() )
			{
				LOG.warn( "License is corrputed in database, App id:" + appLicense.getAppId() + ", license dump:" + appLicense.getLicenseString() );
				iterator.remove();
			}
		}
	}
}
