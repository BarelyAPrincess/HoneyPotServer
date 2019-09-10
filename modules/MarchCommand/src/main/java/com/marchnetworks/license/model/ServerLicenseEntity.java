package com.marchnetworks.license.model;

import com.marchnetworks.command.common.CommonAppUtils;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table( name = "SERVER_LICENSE" )
public class ServerLicenseEntity implements Serializable
{
	private static final long serialVersionUID = -8527011775183547650L;
	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	@Column( name = "ID" )
	private Long id;
	@Lob
	@Column( name = "LICENSE", length = 2000000 )
	private byte[] license;

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public String getLicense()
	{
		return CommonAppUtils.encodeToUTF8String( license );
	}

	public void setLicense( String license )
	{
		this.license = CommonAppUtils.encodeStringToBytes( license );
	}
}
