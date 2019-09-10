package com.marchnetworks.command.common.data;

import com.marchnetworks.command.common.dao.sql.AggregateOperation;

import javax.xml.bind.annotation.XmlElement;

public class Aggregate
{
	private Long columnId;
	private String fieldName;
	private AggregateOperation aggregateOperation;

	public Aggregate()
	{
	}

	public Aggregate( Long columnId, String fieldName, AggregateOperation aggregate )
	{
		this.columnId = columnId;
		this.fieldName = fieldName;
		aggregateOperation = aggregate;
	}

	public Aggregate( String fieldName, AggregateOperation aggregate )
	{
		this.fieldName = fieldName;
		aggregateOperation = aggregate;
	}

	public String toString()
	{
		return aggregateOperation + " (" + fieldName + ")";
	}

	@XmlElement( required = true )
	public Long getColumnId()
	{
		return columnId;
	}

	public void setColumnId( Long columnId )
	{
		this.columnId = columnId;
	}

	@XmlElement( required = true )
	public AggregateOperation getAggregateOperation()
	{
		return aggregateOperation;
	}

	public void setAggregateOperation( AggregateOperation aggregate )
	{
		aggregateOperation = aggregate;
	}

	public String getFieldName()
	{
		return fieldName;
	}

	public void setFieldName( String fieldName )
	{
		this.fieldName = fieldName;
	}
}
