package com.marchnetworks.command.common.data;

import com.marchnetworks.command.common.dao.sql.Operation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class Filter
{
	private Long columnId;
	private String fieldName;
	private Operation operation;
	@XmlElement( required = true )
	private GenericValue value;

	public Filter()
	{
	}

	public Filter( Long columnId, String fieldName, Operation operation, Object value )
	{
		this.columnId = columnId;
		this.fieldName = fieldName;
		this.operation = operation;
		this.value = GenericValue.newGenericValue( value );
	}

	public Filter( String fieldName, Operation operation, Object value )
	{
		this.fieldName = fieldName;
		this.operation = operation;
		this.value = GenericValue.newGenericValue( value );
	}

	@XmlElement( required = true )
	public String getFieldName()
	{
		return fieldName;
	}

	public void setFieldName( String fieldName )
	{
		this.fieldName = fieldName;
	}

	@XmlElement( required = true )
	public Operation getOperation()
	{
		return operation;
	}

	public void setOperation( Operation operation )
	{
		this.operation = operation;
	}

	public void setValue( Object value )
	{
		this.value = GenericValue.newGenericValue( value );
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

	@XmlTransient
	public Object getValue()
	{
		return value == null ? null : value.getValue();
	}

	public String toString()
	{
		String result = fieldName + " " + operation.getShortString();
		if ( value != null )
		{
			result = result + " " + value.toString();
		}
		return result;
	}

	protected void setValue( GenericValue value )
	{
		this.value = value;
	}
}
