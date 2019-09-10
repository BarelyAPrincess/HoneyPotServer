package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.data.GenericObjectInfo;
import com.marchnetworks.command.common.topology.data.Store;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table( name = "GENERIC_STORAGE" )
public class GenericStorageEntity
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Version
	@Column( name = "VERSION" )
	private Long version;
	@Column( name = "STORE", nullable = false )
	@Enumerated( EnumType.STRING )
	private Store store;
	@Column( name = "OBJECT_ID", nullable = false )
	private String objectId;
	@Column( name = "USER_ID" )
	private String userId;
	@Column( name = "APP_ID" )
	private String appId;
	@Lob
	@Column( name = "DATA" )
	private byte[] data;
	@Column( name = "SIZE" )
	private long size;

	public GenericObjectInfo toInfoObject()
	{
		GenericObjectInfo result = new GenericObjectInfo( size, objectId, appId, userId );
		return result;
	}

	public String getTagId()
	{
		return store + "/" + objectId + "/" + appId + "/" + userId;
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public Long getVersion()
	{
		return version;
	}

	public void setVersion( Long version )
	{
		this.version = version;
	}

	public Store getStore()
	{
		return store;
	}

	public void setStore( Store store )
	{
		this.store = store;
	}

	public String getObjectId()
	{
		return objectId;
	}

	public void setObjectId( String objectId )
	{
		this.objectId = objectId;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId( String userId )
	{
		this.userId = userId;
	}

	public String getAppId()
	{
		return appId;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}

	public byte[] getData()
	{
		return data;
	}

	public void setData( byte[] data )
	{
		this.data = data;
	}

	public long getSize()
	{
		return size;
	}

	public void setSize( long size )
	{
		this.size = size;
	}
}

