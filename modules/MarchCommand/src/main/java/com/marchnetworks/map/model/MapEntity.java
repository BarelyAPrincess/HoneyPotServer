package com.marchnetworks.map.model;

import com.marchnetworks.common.utils.CommonUtils;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table( name = "MAP_DATA" )
public class MapEntity
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Version
	@Column( name = "VERSION" )
	private Long version;
	@Lob
	@Column( name = "MAP_DATA" )
	private byte[] mapData;
	@Column( name = "HASH", length = 20 )
	private byte[] hash;
	@Column( name = "RESOURCE_REFERENCES", length = 4000 )
	private String referencesString;

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

	public byte[] getMapData()
	{
		return mapData;
	}

	public void setMapData( byte[] mapData )
	{
		this.mapData = mapData;
	}

	public byte[] getHash()
	{
		return hash;
	}

	public void setHash( byte[] hash )
	{
		this.hash = hash;
	}

	public Set<Long> getReferences()
	{
		return CommonUtils.jsonToLongSet( referencesString );
	}

	public void setReferences( Set<Long> references )
	{
		referencesString = CommonUtils.setToJson( references );
	}

	protected String getReferencesString()
	{
		return referencesString;
	}

	protected void setReferencesString( String referencesString )
	{
		this.referencesString = referencesString;
	}
}

