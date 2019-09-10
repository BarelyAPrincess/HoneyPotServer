package com.marchnetworks.command.api.migration;

import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.CommonAppUtils;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

public class MigrationDAOImpl implements MigrationDAO
{
	private static final Logger LOG = LoggerFactory.getLogger( MigrationDAOImpl.class );

	protected EntityManager entityManager;

	public void query( String query )
	{
		Session session = ( Session ) entityManager.getDelegate();
		session.createSQLQuery( query ).executeUpdate();
	}

	public void dropTables( String... tableNames )
	{
		Session session = ( Session ) entityManager.getDelegate();
		for ( String tableName : tableNames )
		{
			if ( tableExists( tableName ) )
			{
				session.createSQLQuery( "DROP TABLE " + tableName ).executeUpdate();
			}
		}
	}

	public void dropColumns( String tableName, String... columnNames )
	{
		Session session = ( Session ) entityManager.getDelegate();
		if ( tableExists( tableName ) )
		{
			for ( String columnName : columnNames )
			{
				if ( columnExists( tableName, columnName ) )
				{
					session.createSQLQuery( "ALTER TABLE " + tableName + " DROP COLUMN " + columnName ).executeUpdate();
				}
			}
		}
	}

	public void initializeColumn( String tableName, String columnName, Object value )
	{
		Session session = ( Session ) entityManager.getDelegate();
		if ( ( tableExists( tableName ) ) && ( columnExists( tableName, columnName ) ) )
		{
			session.createSQLQuery( "UPDATE " + tableName + " SET " + columnName + " = '" + value + "'" ).executeUpdate();
		}
	}

	public void initializeColumnWithUUID( String tableName, String columnName )
	{
		Session session = ( Session ) entityManager.getDelegate();
		if ( ( tableExists( tableName ) ) && ( columnExists( tableName, columnName ) ) )
		{
			session.createSQLQuery( "UPDATE " + tableName + " SET " + columnName + " = NEWID() " ).executeUpdate();
		}
	}

	public void deleteRows( String tableName, String whereClause )
	{
		Session session = ( Session ) entityManager.getDelegate();
		if ( tableExists( tableName ) )
		{
			String sql = "DELETE FROM " + tableName;
			if ( whereClause != null )
			{
				sql = sql + " WHERE " + whereClause;
			}
			session.createSQLQuery( sql ).executeUpdate();
		}
	}

	public List<Object[]> findAllRowsFromTable( String tableName )
	{
		Session session = ( Session ) entityManager.getDelegate();
		List<Object[]> objects = new ArrayList();
		if ( tableExists( tableName ) )
		{
			SQLQuery query = session.createSQLQuery( "SELECT * FROM " + tableName );
			objects = query.list();
		}
		return objects;
	}

	public boolean tableExists( String tableName )
	{
		boolean result = false;
		Session session = ( Session ) entityManager.getDelegate();
		SQLQuery query = session.createSQLQuery( "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + tableName + "'" );
		if ( !query.list().isEmpty() )
		{
			result = true;
		}
		else
		{
			LOG.info( "Table {} is not present in the current schema", tableName );
		}
		return result;
	}

	public boolean renameTable( String oldName, String newName )
	{
		boolean result = false;
		Session session = ( Session ) entityManager.getDelegate();
		if ( tableExists( oldName ) )
		{
			SQLQuery query = session.createSQLQuery( "EXEC sp_rename '" + oldName + "', '" + newName + "'" );
			try
			{
				query.executeUpdate();
				result = true;
			}
			catch ( SQLGrammarException sge )
			{
				LOG.info( "Table renaming  could not be done for table {} ", oldName );
			}
		}
		return result;
	}

	public boolean columnExists( String tableName, String columnName )
	{
		boolean result = false;
		Session session = ( Session ) entityManager.getDelegate();
		SQLQuery query = session.createSQLQuery( "select COLUMN_NAME from INFORMATION_SCHEMA.columns where table_name = '" + tableName + "' and column_name = '" + columnName + "'" );
		if ( !query.list().isEmpty() )
		{
			result = true;
		}
		return result;
	}

	public <T> List<T> findItemsToMigrate( String tableName, String whereClause, String... columns )
	{
		Session session = ( Session ) entityManager.getDelegate();
		ensureExist( tableName, columns );
		String columnsString = CollectionUtils.arrayToString( columns, ",", false );
		String sql = "SELECT " + columnsString + " FROM " + tableName;
		if ( whereClause != null )
		{
			sql = sql + " WHERE " + whereClause;
		}
		SQLQuery query = session.createSQLQuery( sql );
		return query.list();
	}

	private void ensureExist( String tableName, String... columns )
	{
		if ( !tableExists( tableName ) )
		{
			throw new IllegalArgumentException( "Table " + tableName + " does not exist" );
		}
		if ( columns != null )
		{
			for ( String column : columns )
			{
				if ( !columnExists( tableName, column ) )
				{
					throw new IllegalArgumentException( "Column " + column + " does not exist on table " + tableName );
				}
			}
		}
	}

	public void addColumn( String tableName, String columnName, String columnType )
	{
		Session session = ( Session ) entityManager.getDelegate();
		if ( ( tableExists( tableName ) ) && ( !columnExists( tableName, columnName ) ) )
		{
			SQLQuery query = session.createSQLQuery( "ALTER TABLE " + tableName + " ADD " + columnName + " " + columnType );
			query.executeUpdate();
		}
	}

	public void createTable( String tableName, String[] columnNames, String[] columnTypes )
	{
		Session session = ( Session ) entityManager.getDelegate();
		StringBuilder sb = new StringBuilder( "CREATE TABLE " );
		sb.append( tableName );
		sb.append( " ( " );
		int i = 0;
		for ( String columnName : columnNames )
		{
			sb.append( columnName );
			sb.append( " " );
			sb.append( columnTypes[i] );
			i++;
			if ( i < columnNames.length )
				sb.append( ", " );
		}
		sb.append( ") " );
		session.createSQLQuery( sb.toString() ).executeUpdate();
	}

	public void addForeignKeyConstraint( String tableName, String foreignKeyName, String foreignKeyColumn, String referenceTable, String referenceColumn )
	{
		Session session = ( Session ) entityManager.getDelegate();
		if ( ( tableExists( tableName ) ) && ( tableExists( referenceTable ) ) && ( columnExists( tableName, foreignKeyColumn ) ) && ( columnExists( referenceTable, referenceColumn ) ) )
		{
			session.createSQLQuery( "ALTER TABLE " + tableName + " ADD CONSTRAINT " + foreignKeyName + " FOREIGN KEY (" + foreignKeyColumn + ") REFERENCES " + referenceTable + "(" + referenceColumn + ")" ).executeUpdate();
		}
	}

	public void addPrimaryKeyConstraint( String tableName, String... primaryKeyColumns )
	{
		Session session = ( Session ) entityManager.getDelegate();
		ensureExist( tableName, primaryKeyColumns );
		String columnsString = CollectionUtils.arrayToString( primaryKeyColumns, ",", false );
		session.createSQLQuery( "ALTER TABLE " + tableName + " ADD PRIMARY KEY (" + columnsString + ")" ).executeUpdate();
	}

	public void addUniqueConstraint( String tableName, String... uniqueColumns )
	{
		Session session = ( Session ) entityManager.getDelegate();
		ensureExist( tableName, uniqueColumns );
		String columnsString = CollectionUtils.arrayToString( uniqueColumns, ",", false );
		session.createSQLQuery( "ALTER TABLE " + tableName + " ADD UNIQUE (" + columnsString + ")" ).executeUpdate();
	}

	public boolean constraintExists( String tableName, String constraintName, ConstraintType constraintType )
	{
		Session session = ( Session ) entityManager.getDelegate();
		SQLQuery query = session.createSQLQuery( "SELECT constraint_name from INFORMATION_SCHEMA.TABLE_CONSTRAINTS where constraint_type = '" + constraintType.getValue() + "' and table_name = '" + tableName + "' and constraint_name = '" + constraintName + "'" );

		query.setMaxResults( 1 );
		boolean result = query.list().size() > 0;
		if ( !result )
		{
			LOG.info( "Constraint {} does not exist in table {}.", constraintName, tableName );
		}
		return result;
	}

	public void dropConstraint( String tableName, String constraintName, ConstraintType constraintType )
	{
		Session session = ( Session ) entityManager.getDelegate();
		if ( constraintExists( tableName, constraintName, constraintType ) )
		{
			SQLQuery q = session.createSQLQuery( "ALTER TABLE " + tableName + " DROP CONSTRAINT " + constraintName );
			q.executeUpdate();
		}
	}

	public void dropAutogenerateConstraint( String tableName, String idColumnName )
	{
		addColumn( tableName, "TEMP_ID", "NUMERIC(19,0)" );
		copyToStringValueFromColumn( tableName, "TEMP_ID", idColumnName );
		String constraintName = findFirstConstraintName( tableName, ConstraintType.PRIMARY_KEY );
		dropConstraint( tableName, constraintName, ConstraintType.PRIMARY_KEY );
		dropColumns( tableName, new String[] {idColumnName} );
		modifyColumnType( tableName, "TEMP_ID", "NUMERIC(19,0) NOT NULL" );
		renameColumn( tableName, "TEMP_ID", idColumnName );
		addPrimaryKeyConstraint( tableName, new String[] {idColumnName} );
	}

	public String findFirstConstraintName( String tableName, ConstraintType constraintType )
	{
		Session session = ( Session ) entityManager.getDelegate();
		SQLQuery q = session.createSQLQuery( "SELECT constraint_name from INFORMATION_SCHEMA.TABLE_CONSTRAINTS where constraint_type = '" + constraintType.getValue() + "' and TABLE_NAME = '" + tableName + "'" );

		q.setMaxResults( 1 );
		return ( String ) q.uniqueResult();
	}

	public void updateTable( String tableName, String column, Object value, String whereClause )
	{
		Session session = ( Session ) entityManager.getDelegate();
		ensureExist( tableName, new String[] {column} );
		String sql = "UPDATE " + tableName + " SET " + column + " = ?";
		if ( whereClause != null )
		{
			sql = sql + " WHERE " + whereClause;
		}
		SQLQuery q = session.createSQLQuery( sql );
		q.setParameter( 0, value );
		q.executeUpdate();
	}

	public void appendStringToColumn( String tableName, String column, String suffix, String whereClause )
	{
		Session session = ( Session ) entityManager.getDelegate();
		ensureExist( tableName, new String[] {column} );
		String sql = "UPDATE " + tableName + " SET " + column + " = " + column + " + " + "'" + suffix + "'";
		if ( whereClause != null )
		{
			sql = sql + " WHERE " + whereClause;
		}
		SQLQuery q = session.createSQLQuery( sql );
		q.executeUpdate();
	}

	public void renameColumn( String tableName, String oldName, String newName )
	{
		Session session = ( Session ) entityManager.getDelegate();
		ensureExist( tableName, new String[] {oldName} );
		SQLQuery query = session.createSQLQuery( "EXEC sp_rename '" + tableName + "." + oldName + "', '" + newName + "', 'COLUMN'" );
		query.executeUpdate();
	}

	public void modifyColumnType( String tableName, String column, String columnType )
	{
		Session session = ( Session ) entityManager.getDelegate();
		ensureExist( tableName, new String[] {column} );
		String sql = "ALTER TABLE " + tableName + " ALTER COLUMN " + column + " " + columnType;
		SQLQuery q = session.createSQLQuery( sql );
		q.executeUpdate();
	}

	public void copyToStringValueFromColumn( String tableName, String toColumn, String fromColumn )
	{
		copyToStringValueFromColumn( tableName, toColumn, fromColumn, null, null );
	}

	public void copyToStringValueFromColumn( String tableName, String toColumn, String fromColumn, String prepend, String append )
	{
		if ( columnExists( tableName, fromColumn ) )
		{
			Session session = ( Session ) entityManager.getDelegate();
			String sql = "UPDATE " + tableName + " SET " + toColumn + " = ";
			if ( !CommonAppUtils.isNullOrEmptyString( prepend ) )
			{
				sql = sql + "'" + prepend + "'+";
			}
			sql = sql + "CAST(" + fromColumn + " AS NVARCHAR(MAX))";
			if ( !CommonAppUtils.isNullOrEmptyString( append ) )
			{
				sql = sql + "+'" + append + "'";
			}
			SQLQuery q = session.createSQLQuery( sql );
			q.executeUpdate();
		}
	}

	public void setEntityManager( EntityManager entityManager )
	{
		this.entityManager = entityManager;
	}

	public static enum ConstraintType
	{
		PRIMARY_KEY( "PRIMARY KEY" ),
		FOREIGN_KEY( "FOREIGN KEY" ),
		UNIQUE_CONSTRAINT( "UNIQUE" );

		private String value;

		private ConstraintType( String value )
		{
			this.value = value;
		}

		public String getValue()
		{
			return value;
		}
	}

	public void updateVarcharColumns( String tableName, String columnName, String length, String addNullable )
	{
		Session session = ( Session ) entityManager.getDelegate();
		SQLQuery q = session.createSQLQuery( "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " NVARCHAR(" + length + ") " + addNullable );
		q.executeUpdate();
	}
}
