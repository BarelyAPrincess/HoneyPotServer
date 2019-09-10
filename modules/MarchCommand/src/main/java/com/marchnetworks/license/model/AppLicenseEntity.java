package com.marchnetworks.license.model;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.license.data.AppLicenseInfo;
import com.marchnetworks.common.crypto.CryptoException;
import com.marchnetworks.common.crypto.CryptoUtils;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.utils.DateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
@Table( name = "APP_LICENSE" )
public class AppLicenseEntity
{
	private static final Logger LOG = LoggerFactory.getLogger( AppLicenseEntity.class );

	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;

	@Version
	@Column( name = "OPT_LOCK" )
	private Long optLock;

	@Column( name = "APP_ID" )
	private String appId;

	@Column( name = "LICENSE_ID" )
	private String licenseId;
	@Column( name = "LAST_WARNING_DAY" )
	private long lastWarningDay = -1L;

	@Lob
	@Column( name = "LICENSE" )
	private byte[] license;

	@Transient
	private SerializedLicense serializedLicense = new SerializedLicense();

	public void readFromImport( AppLicenseImport licenseImport )
	{
		appId = licenseImport.getIdentity().getId();
		licenseId = licenseImport.getLicenseId();
		serializedLicense.type = licenseImport.getType();
		serializedLicense.feature = licenseImport.getFeature();
		serializedLicense.expiry = licenseImport.getExpiry();
		serializedLicense.commercial = licenseImport.isCommercial();
		serializedLicense.open = licenseImport.isOpen();
		serializedLicense.start = ( licenseImport.getStart() != null ? DateUtils.getUTCMicrosFromDate( licenseImport.getStart() ) : 0L );
		serializedLicense.end = ( licenseImport.getEnd() != null ? DateUtils.getUTCMicrosFromDate( licenseImport.getEnd() ) : 0L );
		serializedLicense.count = licenseImport.getCount();
		serializedLicense.resourceTypes = licenseImport.getResourceTypes();
		serializedLicense.identity = licenseImport.getIdentity();
		serializedLicense.licenseIds.clear();
		serializedLicense.licenseIds.add( licenseImport.getLicenseId() );

		serializedLicense.status = LicenseStatus.OK;
	}

	public void addFromImport( AppLicenseImport licenseImport )
	{
		licenseId = licenseImport.getLicenseId();
		serializedLicense.type = licenseImport.getType();
		serializedLicense.commercial = licenseImport.isCommercial();
		serializedLicense.open = licenseImport.isOpen();
		serializedLicense.count += licenseImport.getCount();

		serializedLicense.licenseIds.add( licenseImport.getLicenseId() );
	}

	public void update()
	{
		String json = CoreJsonSerializer.toJson( serializedLicense );

		try
		{
			license = CryptoUtils.encrypt( CommonAppUtils.encodeStringToBytes( json ) );
		}
		catch ( CryptoException e )
		{
			LOG.error( "Failed to save App license " + e.getMessage() );
		}
	}

	public boolean read()
	{
		String json;

		try
		{
			json = CommonAppUtils.encodeToUTF8String( CryptoUtils.decrypt( license ) );
		}
		catch ( CryptoException e )
		{
			LOG.error( "Failed to read App license " + e.getMessage() );
			return false;
		}

		serializedLicense = ( ( SerializedLicense ) CoreJsonSerializer.fromJson( json, SerializedLicense.class ) );

		String serializedLicenseId = ( String ) serializedLicense.licenseIds.get( serializedLicense.licenseIds.size() - 1 );
		return ( appId.equals( serializedLicense.identity.getId() ) ) && ( licenseId.equals( serializedLicenseId ) );
	}

	public License toDataObject( Set<Long> resources )
	{
		License result = new License( licenseId, serializedLicense.type, serializedLicense.feature, serializedLicense.status, serializedLicense.expiry, serializedLicense.count, serializedLicense.resources.size(), serializedLicense.start, serializedLicense.end, serializedLicense.commercial, serializedLicense.open, serializedLicense.identity, resources != null ? ( Long[] ) resources.toArray( new Long[resources.size()] ) : null, serializedLicense.resourceTypes != null ? ( String[] ) serializedLicense.resourceTypes.toArray( new String[serializedLicense.resourceTypes.size()] ) : null );

		return result;
	}

	public AppLicenseInfo toAppLicenseInfo()
	{
		AppLicenseInfo result = new AppLicenseInfo( serializedLicense.identity.getId(), serializedLicense.feature, isValid(), serializedLicense.count, serializedLicense.resources != null ? ( Long[] ) serializedLicense.resources.toArray( new Long[serializedLicense.resources.size()] ) : null, serializedLicense.resourceTypes != null ? ( String[] ) serializedLicense.resourceTypes.toArray( new String[serializedLicense.resourceTypes.size()] ) : null );

		return result;
	}

	public String toString()
	{
		return "licenseId:" + licenseId + ", App name:" + getIdentity().getName();
	}

	public boolean isValid()
	{
		return ( serializedLicense.status != LicenseStatus.EXPIRED ) && ( serializedLicense.status != LicenseStatus.FAILED );
	}

	public boolean isFailed()
	{
		return ( serializedLicense.status == LicenseStatus.FAILGRACE ) || ( serializedLicense.status == LicenseStatus.FAILED );
	}

	public Long getId()
	{
		return id;
	}

	public Long getOptLock()
	{
		return optLock;
	}

	public void setOptLock( Long optLock )
	{
		this.optLock = optLock;
	}

	public String getAppId()
	{
		return appId;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}

	public String getLicenseId()
	{
		return licenseId;
	}

	public void setLicenseId( String licenseId )
	{
		this.licenseId = licenseId;
	}

	public long getLastWarningDay()
	{
		return lastWarningDay;
	}

	public void setLastWarningDay( long lastWarningDay )
	{
		this.lastWarningDay = lastWarningDay;
	}

	public byte[] getLicense()
	{
		return license;
	}

	public String getLicenseString()
	{
		return CommonAppUtils.byteToBase64( license );
	}

	public void setLicense( byte[] license )
	{
		this.license = license;
	}

	public AppLicenseType getType()
	{
		return serializedLicense.type;
	}

	public void setType( AppLicenseType type )
	{
		serializedLicense.type = type;
	}

	public String getFeature()
	{
		return serializedLicense.feature;
	}

	public void setFeature( String feature )
	{
		serializedLicense.feature = feature;
	}

	public Expiry getExpiry()
	{
		return serializedLicense.expiry;
	}

	public void setExpiry( Expiry expiry )
	{
		serializedLicense.expiry = expiry;
	}

	public boolean isCommercial()
	{
		return serializedLicense.commercial;
	}

	public void setCommercial( boolean commercial )
	{
		serializedLicense.commercial = commercial;
	}

	public boolean isOpen()
	{
		return serializedLicense.open;
	}

	public void setOpen( boolean open )
	{
		serializedLicense.open = open;
	}

	public long getStart()
	{
		return serializedLicense.start;
	}

	public void setStart( long start )
	{
		serializedLicense.start = start;
	}

	public long getEnd()
	{
		return serializedLicense.end;
	}

	public void setEnd( long end )
	{
		serializedLicense.end = end;
	}

	public int getCount()
	{
		return serializedLicense.count;
	}

	public void setCount( int count )
	{
		serializedLicense.count = count;
	}

	public ApplicationIdentityToken getIdentity()
	{
		return serializedLicense.identity;
	}

	public void setIdentity( ApplicationIdentityToken identity )
	{
		serializedLicense.identity = identity;
	}

	public byte[] getServerId()
	{
		return CommonAppUtils.stringBase64ToByte( serializedLicense.serverId );
	}

	public void setServerId( byte[] serverId )
	{
		serializedLicense.serverId = CommonAppUtils.byteToBase64( serverId );
	}

	public List<String> getLicenseIds()
	{
		return serializedLicense.licenseIds;
	}

	public void setLicenseIds( List<String> licenseIds )
	{
		serializedLicense.licenseIds = licenseIds;
	}

	public Set<Long> getResources()
	{
		return serializedLicense.resources;
	}

	public void setResources( Set<Long> resources )
	{
		serializedLicense.resources = resources;
	}

	public void clearResources()
	{
		serializedLicense.resources.clear();
	}

	public LicenseStatus getStatus()
	{
		return serializedLicense.status;
	}

	public void setStatus( LicenseStatus status )
	{
		serializedLicense.status = status;
	}

	public List<String> getResourceTypes()
	{
		return serializedLicense.resourceTypes;
	}

	public void setResourceTypes( List<String> resourceTypes )
	{
		serializedLicense.resourceTypes = resourceTypes;
	}

	public int getAllocated()
	{
		return serializedLicense.resources.size();
	}

	public String getName()
	{
		String feature = getFeature();
		return getIdentity().getName() + ( feature != null ? " " + feature : "" );
	}
}
