package com.marchnetworks.command.api.migration;

import java.util.List;

public abstract interface MigrationDAO
{
	public abstract void dropTables( String... paramVarArgs );

	public abstract void dropColumns( String paramString, String... paramVarArgs );

	public abstract void initializeColumn( String paramString1, String paramString2, Object paramObject );

	public abstract void deleteRows( String paramString1, String paramString2 );

	public abstract List<Object[]> findAllRowsFromTable( String paramString );

	public abstract boolean tableExists( String paramString );

	public abstract boolean renameTable( String paramString1, String paramString2 );

	public abstract boolean columnExists( String paramString1, String paramString2 );

	public abstract void addColumn( String paramString1, String paramString2, String paramString3 );

	public abstract void createTable( String paramString, String[] paramArrayOfString1, String[] paramArrayOfString2 );

	public abstract void addForeignKeyConstraint( String paramString1, String paramString2, String paramString3, String paramString4, String paramString5 );

	public abstract void addPrimaryKeyConstraint( String paramString, String... paramVarArgs );

	public abstract void addUniqueConstraint( String paramString, String... paramVarArgs );

	public abstract boolean constraintExists( String paramString1, String paramString2, MigrationDAOImpl.ConstraintType paramConstraintType );

	public abstract void dropConstraint( String paramString1, String paramString2, MigrationDAOImpl.ConstraintType paramConstraintType );

	public abstract void dropAutogenerateConstraint( String paramString1, String paramString2 );

	public abstract String findFirstConstraintName( String paramString, MigrationDAOImpl.ConstraintType paramConstraintType );

	public abstract <T> List<T> findItemsToMigrate( String paramString1, String paramString2, String... paramVarArgs );

	public abstract void updateTable( String paramString1, String paramString2, Object paramObject, String paramString3 );

	public abstract void appendStringToColumn( String paramString1, String paramString2, String paramString3, String paramString4 );

	public abstract void renameColumn( String paramString1, String paramString2, String paramString3 );

	public abstract void modifyColumnType( String paramString1, String paramString2, String paramString3 );

	public abstract void copyToStringValueFromColumn( String paramString1, String paramString2, String paramString3 );

	public abstract void copyToStringValueFromColumn( String paramString1, String paramString2, String paramString3, String paramString4, String paramString5 );

	public abstract void initializeColumnWithUUID( String paramString1, String paramString2 );

	public abstract void updateVarcharColumns( String paramString1, String paramString2, String paramString3, String paramString4 );

	public abstract void query( String paramString );
}
