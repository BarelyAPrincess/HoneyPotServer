package com.marchnetworks.common.diagnostics.database;

public class DatabaseSize
{
	private String databaseName;

	private long databaseSize;

	private long databaseUsedSize;

	private long logSize;

	private long logUsedSize;

	public DatabaseSize( String databaseName )
	{
		this.databaseName = databaseName;
	}

	public String getDatabaseName()
	{
		return databaseName;
	}

	public void setDatabaseName( String databaseName )
	{
		this.databaseName = databaseName;
	}

	public long getDatabaseSize()
	{
		return databaseSize;
	}

	public void setDatabaseSize( long databaseSize )
	{
		this.databaseSize = databaseSize;
	}

	public long getLogSize()
	{
		return logSize;
	}

	public void setLogSize( long logSize )
	{
		this.logSize = logSize;
	}

	public long getDatabaseUsedSize()
	{
		return databaseUsedSize;
	}

	public void setDatabaseUsedSize( long databaseUsedSize )
	{
		this.databaseUsedSize = databaseUsedSize;
	}

	public long getLogUsedSize()
	{
		return logUsedSize;
	}

	public void setLogUsedSize( long logUsedSize )
	{
		this.logUsedSize = logUsedSize;
	}
}
