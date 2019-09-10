/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.session;

import io.amelia.data.parcel.Parcel;
import io.amelia.data.parcel.ParcelLoader;
import io.amelia.lang.SessionException;
import io.amelia.support.DateAndTime;
import io.amelia.support.Strs;

/**
 * Stores arbitrary data for sessions being loaded from their datastore.
 */
public abstract class SessionData
{
	private final SessionAdapterImpl datastore;
	/**
	 * Persistent session variables<br>
	 * Session variables will live outside of the sessions's life
	 */
	protected Parcel data = Parcel.empty();
	protected String ipAddress;
	protected String sessionId;
	protected String sessionName;
	protected boolean stale;
	protected long timeout;
	protected String webroot;

	protected SessionData( SessionAdapterImpl datastore, boolean stale )
	{
		this.datastore = datastore;
		this.stale = stale;
		defaults();
	}

	protected final SessionAdapterImpl datastore()
	{
		return datastore;
	}

	protected void defaults()
	{
		timeout = DateAndTime.epoch() + SessionRegistry.getDefaultTimeout();
		ipAddress = null;
		sessionName = SessionRegistry.getDefaultSessionName();
		sessionId = null;
		webroot = "default";
	}

	protected abstract void destroy() throws SessionException.Error;

	protected abstract void reload() throws SessionException.Error;

	protected abstract void save() throws SessionException.Error;

	@Override
	public String toString()
	{
		return "SessionData{name=" + sessionName + ",id=" + sessionId + ",ip=" + ipAddress + ",timeout=" + timeout + ",webroot=" + webroot + "," + Strs.join( ParcelLoader.encodeMap( data ), ",", "=", "<null>" ) + "}";
	}
}
