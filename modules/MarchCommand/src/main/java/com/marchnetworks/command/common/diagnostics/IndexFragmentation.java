package com.marchnetworks.command.common.diagnostics;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class IndexFragmentation
{
	private String databaseName;
	private String tableName;
	private String indexName;
	private double averageFragmentation;

	public IndexFragmentation( String databaseName, String tableName, String indexName, double averageFragmentation )
	{
		this.databaseName = databaseName;
		this.tableName = tableName;
		this.indexName = indexName;
		this.averageFragmentation = new BigDecimal( averageFragmentation ).setScale( 2, RoundingMode.UP ).doubleValue();
	}

	public String getDatabaseName()
	{
		return databaseName;
	}

	public String getTableName()
	{
		return tableName;
	}

	public String getIndexName()
	{
		return indexName;
	}

	public double getAverageFragmentation()
	{
		return averageFragmentation;
	}
}
