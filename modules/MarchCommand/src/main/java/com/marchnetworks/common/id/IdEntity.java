package com.marchnetworks.common.id;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "LAST_IDS" )
public class IdEntity
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Column( name = "TABLE_NAME" )
	private String tableName;
	@Column( name = "LAST_ID" )
	private Long lastId;

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName( String tableName )
	{
		this.tableName = tableName;
	}

	public Long getLastId()
	{
		return lastId;
	}

	public void setLastId( Long lastId )
	{
		this.lastId = lastId;
	}
}
