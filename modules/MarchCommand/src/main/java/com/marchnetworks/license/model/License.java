package com.marchnetworks.license.model;

import javax.xml.bind.annotation.XmlElement;

public class License
{
	private String id;
	private AppLicenseType type;
	private String feature;
	private LicenseStatus status;
	private Expiry expiryType;
	private int count;
	private int inUse;
	private long start;
	private long end;
	private boolean isCommercial;
	private boolean isOpen;
	private ApplicationIdentityToken appIdentity;
	private Long[] resources;
	private String[] resourceTypes;

	public License( String id, AppLicenseType type, String feature, LicenseStatus status, Expiry expiryType, int count, int inUse, long start, long end, boolean isCommercial, boolean isOpen, ApplicationIdentityToken appIdentity, Long[] resources, String[] resourceTypes )
	{
		this.id = id;
		this.type = type;
		this.feature = feature;
		this.status = status;
		this.expiryType = expiryType;
		this.count = count;
		this.inUse = inUse;
		this.start = start;
		this.end = end;
		this.isCommercial = isCommercial;
		this.isOpen = isOpen;
		this.appIdentity = appIdentity;
		this.resources = resources;
		this.resourceTypes = resourceTypes;
	}

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	@XmlElement( required = true )
	public AppLicenseType getType()
	{
		return type;
	}

	public void setType( AppLicenseType type )
	{
		this.type = type;
	}

	public String getFeature()
	{
		return feature;
	}

	public void setFeature( String feature )
	{
		this.feature = feature;
	}

	@XmlElement( required = true )
	public LicenseStatus getStatus()
	{
		return status;
	}

	public void setStatus( LicenseStatus status )
	{
		this.status = status;
	}

	@XmlElement( required = true )
	public Expiry getExpiryType()
	{
		return expiryType;
	}

	public void setExpiryType( Expiry expiryType )
	{
		this.expiryType = expiryType;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount( int count )
	{
		this.count = count;
	}

	public int getInUse()
	{
		return inUse;
	}

	public void setInUse( int inUse )
	{
		this.inUse = inUse;
	}

	public long getStart()
	{
		return start;
	}

	public void setStart( long start )
	{
		this.start = start;
	}

	public long getEnd()
	{
		return end;
	}

	public void setEnd( long end )
	{
		this.end = end;
	}

	public boolean isCommercial()
	{
		return isCommercial;
	}

	public void setCommercial( boolean isCommercial )
	{
		this.isCommercial = isCommercial;
	}

	public boolean isOpen()
	{
		return isOpen;
	}

	public void setOpen( boolean isOpen )
	{
		this.isOpen = isOpen;
	}

	@XmlElement( required = true )
	public ApplicationIdentityToken getAppIdentity()
	{
		return appIdentity;
	}

	public void setAppIdentity( ApplicationIdentityToken appIdentity )
	{
		this.appIdentity = appIdentity;
	}

	public Long[] getResources()
	{
		return resources;
	}

	public void setResources( Long[] resources )
	{
		this.resources = resources;
	}

	public String[] getResourceTypes()
	{
		return resourceTypes;
	}

	public void setResourceTypes( String[] resourceTypes )
	{
		this.resourceTypes = resourceTypes;
	}
}
