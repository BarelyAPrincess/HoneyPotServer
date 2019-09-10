package com.marchnetworks.app.core;

import com.marchnetworks.app.service.OsgiService;

import org.osgi.framework.BundleException;

public abstract interface OsgiManager extends OsgiService
{
	public abstract long installBundle( String paramString ) throws BundleException;

	public abstract void uninstallBundle( long paramLong ) throws BundleException;

	public abstract void startBundle( long paramLong ) throws BundleException;

	public abstract void stopBundle( long paramLong ) throws BundleException;
}
