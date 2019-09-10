package com.marchnetworks.app.model;

import com.marchnetworks.app.data.App;
import com.marchnetworks.app.data.AppStatus;
import com.marchnetworks.app.service.AppConstants;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.crypto.CryptoException;
import com.marchnetworks.common.crypto.CryptoUtils;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.license.model.AppType;
import com.marchnetworks.license.model.ApplicationIdentityToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
@Table( name = "APP" )
public class AppEntity
{
	private static final Logger LOG = LoggerFactory.getLogger( AppEntity.class );

	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;

	@Version
	@Column( name = "OPTLOCK" )
	private Long optLock;

	@Column( name = "GUID" )
	private String guid;

	@Transient
	private ApplicationIdentityToken identity;

	@Lob
	@Column( name = "IDENTITY_STRING" )
	private byte[] identityString;

	@Column( name = "BUNDLE_ID" )
	private Long bundleId;

	@Column( name = "VERSION" )
	private String version;

	@Column( name = "TARGET_SDK_VERSION" )
	private String targetSDKVersion;

	@Column( name = "MINIMUM_CES_VERSION" )
	private String minimumCESVersion;

	@Column( name = "DATABASE_VERSION" )
	private Integer databaseVersion;

	@Column( name = "SERVER_FILE", length = 500 )
	private String serverFile;

	@Column( name = "CLIENT_FILE", length = 500 )
	private String clientFile;

	@Column( name = "STATUS" )
	@Enumerated( EnumType.STRING )
	private AppStatus status;

	@Column( name = "INSTALLED_TIME" )
	private long installedTime;

	@Column( name = "STARTED_TIME" )
	private long startedTime;

	@Column( name = "UPGRADED" )
	private Boolean upgraded;

	public App toDataObject()
	{
		ApplicationIdentityToken identity = getIdentity();
		App app = new App( identity, version, targetSDKVersion, clientFile != null, status, installedTime, startedTime );
		return app;
	}

	public String getAppFolder()
	{
		return AppConstants.APP_DIRECTORY + getIdentity().getName() + "-" + id;
	}

	public boolean hasServerFile()
	{
		return serverFile != null;
	}

	public String getAppServerFile()
	{
		return getAppFolder() + File.separator + serverFile;
	}

	public boolean hasClientFile()
	{
		return clientFile != null;
	}

	public String getAppClientFile()
	{
		return getAppFolder() + File.separator + clientFile;
	}

	public boolean requiresLicense()
	{
		AppType appType = identity.getAppType();
		return ( appType != AppType.BUILT_IN_APP ) && ( appType != AppType.LICENSE_EXEMPT_APP );
	}

	public String toString()
	{
		return getIdentity().getName() + "[id:" + id + ", version:" + version + ", status:" + status + ", bundleId:" + bundleId + "]";
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public Long getOptLock()
	{
		return optLock;
	}

	public void setOptLock( Long optLock )
	{
		this.optLock = optLock;
	}

	public String getGuid()
	{
		return guid;
	}

	public void setGuid( String guid )
	{
		this.guid = guid;
	}

	public boolean readIdentity()
	{
		String json;

		try
		{
			json = CommonAppUtils.encodeToUTF8String( CryptoUtils.decrypt( identityString ) );
		}
		catch ( CryptoException e )
		{
			LOG.error( "Failed to read App identity " + e.getMessage() );
			return false;
		}

		identity = ( ( ApplicationIdentityToken ) CoreJsonSerializer.fromJson( json, ApplicationIdentityToken.class ) );
		if ( !guid.equals( identity.getId() ) )
		{
			return false;
		}
		return true;
	}

	public ApplicationIdentityToken getIdentity()
	{
		return identity;
	}

	public void setIdentity( ApplicationIdentityToken identity )
	{
		String json = CoreJsonSerializer.toJson( identity );
		try
		{
			identityString = CryptoUtils.encrypt( CommonAppUtils.encodeStringToBytes( json ) );
		}
		catch ( CryptoException e )
		{
			LOG.error( "Failed to save App identity " + e.getMessage() );
		}
		this.identity = identity;
		guid = identity.getId();
	}

	public byte[] getIdentityString()
	{
		return identityString;
	}

	public String getIdentityAsString()
	{
		return CommonAppUtils.byteToBase64( identityString );
	}

	protected void setIdentityString( byte[] identityString )
	{
		this.identityString = identityString;
	}

	public Long getBundleId()
	{
		return bundleId;
	}

	public void setBundleId( Long bundleId )
	{
		this.bundleId = bundleId;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion( String version )
	{
		this.version = version;
	}

	public String getTargetSDKVersion()
	{
		return targetSDKVersion;
	}

	public void setTargetSDKVersion( String targetSDKVersion )
	{
		this.targetSDKVersion = targetSDKVersion;
	}

	public String getMinimumCESVersion()
	{
		return minimumCESVersion;
	}

	public void setMinimumCESVersion( String minimumCESVersion )
	{
		this.minimumCESVersion = minimumCESVersion;
	}

	public Integer getDatabaseVersion()
	{
		return databaseVersion;
	}

	public void setDatabaseVersion( int databaseVersion )
	{
		this.databaseVersion = Integer.valueOf( databaseVersion );
	}

	public String getServerFile()
	{
		return serverFile;
	}

	public void setServerFile( String serverFile )
	{
		this.serverFile = serverFile;
	}

	public String getClientFile()
	{
		return clientFile;
	}

	public void setClientFile( String clientFile )
	{
		this.clientFile = clientFile;
	}

	public AppStatus getStatus()
	{
		return status;
	}

	public void setStatus( AppStatus status )
	{
		this.status = status;
	}

	public long getInstalledTime()
	{
		return installedTime;
	}

	public void setInstalledTime( long installedTime )
	{
		this.installedTime = installedTime;
	}

	public long getStartedTime()
	{
		return startedTime;
	}

	public void setStartedTime( long startedTime )
	{
		this.startedTime = startedTime;
	}

	public boolean isUpgraded()
	{
		if ( upgraded == null )
		{
			return false;
		}
		return upgraded.booleanValue();
	}

	public void setUpgraded( boolean upgraded )
	{
		this.upgraded = Boolean.valueOf( upgraded );
	}
}
