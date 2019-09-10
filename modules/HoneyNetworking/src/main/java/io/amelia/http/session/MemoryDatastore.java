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

import java.util.ArrayList;
import java.util.List;

import io.amelia.lang.SessionException;

public class MemoryDatastore implements SessionAdapterImpl
{
	@Override
	public SessionData createSession( String sessionId, SessionWrapper wrapper ) throws SessionException.Error
	{
		return new MemorySessionData( sessionId, wrapper );
	}

	@Override
	public List<SessionData> getSessions() throws SessionException.Error
	{
		return new ArrayList<>();
	}

	class MemorySessionData extends SessionData
	{
		MemorySessionData( String sessionId, SessionWrapper wrapper )
		{
			super( MemoryDatastore.this, false );
			this.sessionId = sessionId;

			webroot = wrapper.getWebroot().getWebrootId();
			ipAddress = wrapper.getIpAddress();
		}

		@Override
		protected void destroy() throws SessionException.Error
		{
			// Do Nothing
		}

		@Override
		protected void reload() throws SessionException.Error
		{
			// Do Nothing
		}

		@Override
		protected void save() throws SessionException.Error
		{
			// Do Nothing
		}
	}
}
