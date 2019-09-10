package com.marchnetworks.app.data;

import com.marchnetworks.license.model.ApplicationIdentityToken;

import javax.xml.bind.annotation.XmlElement;

public class App
{
	private ApplicationIdentityToken identity;
	private String version;
	private String targetSDKVersion;
	private boolean hasClientApp;
	private AppStatus status;
	private long installedTime;
	private long startedTime;

	public App()
	{
	}

	public App( ApplicationIdentityToken identity, String version, String targetSDKVersion, boolean hasClientApp, AppStatus status, long installedTime, long startedTime )
	{
		this.identity = identity;
		this.version = version;
		this.targetSDKVersion = targetSDKVersion;
		this.hasClientApp = hasClientApp;
		this.status = status;
		this.installedTime = ( installedTime * 1000L );
		this.startedTime = ( startedTime * 1000L );
	}

	@XmlElement( required = true )
	public ApplicationIdentityToken getIdentity()
	{
		return identity;
	}

	public void setIdentity( ApplicationIdentityToken identity )
	{
		this.identity = identity;
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

	public boolean getHasClientApp()
	{
		return hasClientApp;
	}

	public void setHasClientApp( boolean hasClientApp )
	{
		this.hasClientApp = hasClientApp;
	}

	@XmlElement( required = true )
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
}
