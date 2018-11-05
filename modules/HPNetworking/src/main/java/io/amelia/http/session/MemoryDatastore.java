/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.session;

import com.google.common.collect.Lists;

import java.util.List;

import io.amelia.lang.SessionException;

/**
 */
public class MemoryDatastore extends SessionAdapterImpl
{
	class MemorySessionData extends SessionData
	{
		MemorySessionData( String sessionId, SessionWrapper wrapper )
		{
			super( MemoryDatastore.this, false );
			this.sessionId = sessionId;

			site = wrapper.getWebroot().getId();
			ipAddress = wrapper.getIpAddress();
		}

		@Override
		protected void destroy() throws SessionException
		{
			// Do Nothing
		}

		@Override
		void reload() throws SessionException
		{
			// Do Nothing
		}

		@Override
		void save() throws SessionException
		{
			// Do Nothing
		}
	}

	@Override
	SessionData createSession( String sessionId, SessionWrapper wrapper ) throws SessionException
	{
		return new MemorySessionData( sessionId, wrapper );
	}

	@Override
	List<SessionData> getSessions() throws SessionException
	{
		return Lists.newArrayList();
	}
}
