package com.marchnetworks.management.file.model;

import com.marchnetworks.command.common.CommonAppUtils;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table( name = "FILEPROTERTY_TABLE" )
public class FileStoragePropertyEntity implements Serializable
{
	private static Logger LOG = LoggerFactory.getLogger( FileStoragePropertyEntity.class );

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	private Long id;

	@ManyToOne
	@JoinColumn( name = "FK_FILEOBJECT_ID" )
	private FileStorageEntity fileObject;

	@Column( name = "NAME" )
	private String name;

	@Lob
	@Column( name = "PROPERTYVALUE" )
	private byte[] propertyValue;

	public FileStoragePropertyEntity()
	{
	}

	public FileStoragePropertyEntity( FileStorageEntity fileObject, String name, String propertyValue )
	{
		this.fileObject = fileObject;
		this.name = name;
		this.propertyValue = CommonAppUtils.encodeStringToBytes( propertyValue );
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public int hashCode()
	{
		int hash = 0;
		hash += ( id != null ? id.hashCode() : 0 );
		return hash;
	}

	public boolean equals( Object object )
	{
		if ( !( object instanceof FileStoragePropertyEntity ) )
		{
			return false;
		}
		FileStoragePropertyEntity other = ( FileStoragePropertyEntity ) object;
		if ( ( ( id == null ) && ( id != null ) ) || ( ( id != null ) && ( !id.equals( id ) ) ) )
		{
			return false;
		}
		return true;
	}

	public String toString()
	{
		return "com.marchnetworks.file.model.FileProperty[id=" + id + "]";
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getPropertyValue()
	{
		LOG.debug( "get property {}, value: {}.", name, CommonAppUtils.encodeToUTF8String( propertyValue ) );
		return CommonAppUtils.encodeToUTF8String( propertyValue );
	}

	public void setPropertyValue( String propertyValue )
	{
		this.propertyValue = CommonAppUtils.encodeStringToBytes( propertyValue );
	}

	public FileStorageEntity getFileObject()
	{
		return fileObject;
	}
}

