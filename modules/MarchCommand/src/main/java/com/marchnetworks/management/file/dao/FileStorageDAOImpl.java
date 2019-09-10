package com.marchnetworks.management.file.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.management.file.model.FileStorageEntity;
import com.marchnetworks.management.file.model.FileStorageType;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

public class FileStorageDAOImpl extends GenericHibernateDAO<FileStorageEntity, Long> implements FileStorageDAO
{
	private final String FIND_USERS_WITH_FILE = "SELECT CERT_FILEOBJECT_ID FROM USERDETAILS WHERE CERT_FILEOBJECT_ID = ?";

	public List<FileStorageEntity> findByPath( String path )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE ).add( Restrictions.eq( "filePath", path ) );

		return criteria.list();
	}

	public List<FileStorageEntity> findByName( String name )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE ).add( Restrictions.eq( "name", name ) );

		return criteria.list();
	}

	public List<FileStorageEntity> findByCategory( FileStorageType category )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE ).add( Restrictions.eq( "category", category ) );

		return criteria.list();
	}

	public List<FileStorageEntity> findByPathCategory( String path, FileStorageType category )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE ).add( Restrictions.conjunction().add( Restrictions.eq( "filePath", path ) ).add( Restrictions.eq( "category", category ) ) );

		return criteria.list();
	}

	public FileStorageEntity findByNameCategory( String name, FileStorageType category )
	{
		Session session = ( Session ) entityManager.getDelegate();

		Criteria criteria = session.createCriteria( entityType ).setResultTransformer( DistinctRootEntityResultTransformer.INSTANCE ).add( Restrictions.conjunction().add( Restrictions.eq( "name", name ) ).add( Restrictions.eq( "category", category ) ) );

		List<FileStorageEntity> result = criteria.list();

		if ( ( result != null ) && ( result.size() > 0 ) )
		{
			return ( FileStorageEntity ) result.get( 0 );
		}
		return null;
	}

	public void deleteByPath( String path )
	{
		List<FileStorageEntity> objectList = findByPath( path );
		for ( FileStorageEntity fileStorageObject : objectList )
		{
			delete( fileStorageObject );
		}
	}

	public void deleteByCategory( FileStorageType category )
	{
		List<FileStorageEntity> objectList = findByCategory( category );
		for ( FileStorageEntity fileStorageObject : objectList )
		{
			delete( fileStorageObject );
		}
	}

	public boolean usersHaveFile( String anId )
	{
		Session session = ( Session ) entityManager.getDelegate();
		SQLQuery q = session.createSQLQuery( "SELECT CERT_FILEOBJECT_ID FROM USERDETAILS WHERE CERT_FILEOBJECT_ID = ?" );
		q.setParameter( 0, anId );
		return !q.list().isEmpty();
	}
}

