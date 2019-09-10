package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.CommonAppUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table( name = "LOB_DATA" )
public class LobDataEntity
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Lob
	@Column( name = "CLOB", length = 2000000 )
	private byte[] clob;
	@Lob
	@Column( name = "BLOB", length = 2000000 )
	private byte[] blob;
	@Column( name = "DATA_TYPE" )
	private String dataType;
	@Version
	@Column( name = "VERSION" )
	protected Long version;

	public String getClob()
	{
		return CommonAppUtils.encodeToUTF8String( clob );
	}

	public void setClob( String clob )
	{
		this.clob = clob.getBytes();
	}

	public byte[] getBlob()
	{
		return blob;
	}

	public void setBlob( byte[] blob )
	{
		this.blob = blob;
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public String getDataType()
	{
		return dataType;
	}

	public void setDataType( String dataType )
	{
		this.dataType = dataType;
	}
}

