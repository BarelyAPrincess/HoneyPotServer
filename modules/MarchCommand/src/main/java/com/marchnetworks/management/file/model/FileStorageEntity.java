package com.marchnetworks.management.file.model;

import com.marchnetworks.management.data.FileStorageView;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table( name = "FILEOBJECT_TABLE", uniqueConstraints = {@javax.persistence.UniqueConstraint( columnNames = {"CATEGORY", "NAME"} )} )
public class FileStorageEntity implements FileStorageMBean, Serializable
{
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	@Column( name = "FILEOBJECT_ID" )
	private Long id;
	@Column( name = "NAME", nullable = false )
	private String name;
	@Lob
	@Column( name = "FILEBYTES", nullable = true )
	private byte[] theFile;
	@Enumerated( EnumType.STRING )
	@Column( name = "CATEGORY", nullable = false, length = 50 )
	private FileStorageType category;
	@Column( name = "CREATEDATE", nullable = false )
	@Temporal( TemporalType.TIMESTAMP )
	private Calendar createDate;
	@Column( name = "MODIFYDATE", nullable = false )
	@Temporal( TemporalType.TIMESTAMP )
	private Calendar modifyDate;
	@Column( name = "FILEREPOSITORYPATH", nullable = true )
	private String fileRepositoryPath;
	@OneToMany( cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.EAGER )
	@JoinColumn( name = "FK_FILEOBJECT_ID", nullable = true )
	private Map<String, FileStoragePropertyEntity> filePropertyMap = new HashMap();

	public FileStorageEntity()
	{
	}

	public FileStorageEntity( String name, FileStorageType category )
	{
		this.name = name;
		this.category = category;
		Calendar _createDate = Calendar.getInstance();
		setCreateDate( _createDate );
		setModifyDate( _createDate );
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
		if ( !( object instanceof FileStorageEntity ) )
		{
			return false;
		}
		FileStorageEntity other = ( FileStorageEntity ) object;
		if ( ( ( id == null ) && ( id != null ) ) || ( ( id != null ) && ( !id.equals( id ) ) ) )
		{
			return false;
		}
		return true;
	}

	public String toString()
	{
		return "com.marchnetworks.management.file.model.FileObject[id=" + id + "]";
	}

	public String getName()
	{
		return name;
	}

	protected void setName( String name )
	{
		this.name = name;
		updateModifyDate();
	}

	public Calendar getCreateDate()
	{
		return createDate;
	}

	protected void setCreateDate( Calendar createDate )
	{
		this.createDate = createDate;
	}

	public Calendar getModifyDate()
	{
		return modifyDate;
	}

	private void updateModifyDate()
	{
		Calendar _createDate = Calendar.getInstance();
		setModifyDate( _createDate );
	}

	protected void setModifyDate( Calendar modifyDate )
	{
		this.modifyDate = modifyDate;
	}

	public FileStorageType getCategory()
	{
		return category;
	}

	protected void setCategory( FileStorageType category )
	{
		this.category = category;
		updateModifyDate();
	}

	public String getFileRepositoryPath()
	{
		return fileRepositoryPath;
	}

	public void setFileRepositoryPath( String fileRepositoryPath )
	{
		this.fileRepositoryPath = fileRepositoryPath;
	}

	public void setProperty( String name, String property )
	{
		FileStoragePropertyEntity fileProperty = ( FileStoragePropertyEntity ) filePropertyMap.get( name );
		if ( fileProperty != null )
		{
			fileProperty.setPropertyValue( property );
		}
		else
		{
			fileProperty = new FileStoragePropertyEntity( this, name, property );
			filePropertyMap.put( name, fileProperty );
		}
		updateModifyDate();
	}

	public String getProperty( String name )
	{
		String property = null;
		FileStoragePropertyEntity fileProperty = ( FileStoragePropertyEntity ) filePropertyMap.get( name );
		if ( fileProperty != null )
		{
			property = fileProperty.getPropertyValue();
		}
		return property;
	}

	public void removeProperty( String name )
	{
		filePropertyMap.remove( name );
		updateModifyDate();
	}

	public Map<String, FileStoragePropertyEntity> getProperties()
	{
		return filePropertyMap;
	}

	public void clearAllProperties()
	{
		Set<String> keySet = filePropertyMap.keySet();
		Iterator<String> keyList = keySet.iterator();
		String keyName = null;
		while ( keyList.hasNext() )
		{
			keyName = ( String ) keyList.next();
			filePropertyMap.remove( keyName );
		}
	}

	public void readFromDataObject( FileStorageView aStorage )
	{
		setId( Long.valueOf( aStorage.getFileId() ) );
		setName( aStorage.getName() );
	}

	public byte[] getTheFile()
	{
		return theFile;
	}

	public void setTheFile( byte[] theFile )
	{
		this.theFile = theFile;
	}
}

