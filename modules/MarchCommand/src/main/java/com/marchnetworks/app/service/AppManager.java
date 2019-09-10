package com.marchnetworks.app.service;

import com.marchnetworks.app.data.App;
import com.marchnetworks.app.data.TestApp;
import com.marchnetworks.command.common.app.AppException;

import java.util.List;

public abstract interface AppManager
{
	public abstract App[] getApps() throws AppException;

	public abstract App getApp( String paramString );

	public abstract String getClientFile( String paramString );

	public abstract String install( String paramString ) throws AppException;

	public abstract void start( String paramString ) throws AppException;

	public abstract void restart( String paramString ) throws AppException;

	public abstract void stop( String paramString ) throws AppException;

	public abstract void uninstall( String paramString ) throws AppException;

	public abstract void upgrade( String paramString1, String paramString2 ) throws AppException;

	public abstract void notifyAppLicenseChange( String paramString, boolean paramBoolean );

	public abstract List<TestApp> getAvailableApps();

	public abstract List<String> getAppGuids();

	public abstract String getAppName( String paramString );

	public abstract App[] getAllApps();
}
