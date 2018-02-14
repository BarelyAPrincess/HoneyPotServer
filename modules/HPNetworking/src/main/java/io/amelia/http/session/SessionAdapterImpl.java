/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.session;

import java.util.List;

import io.amelia.lang.SessionException;

/**
 * Base class for Session Storage
 */
public interface SessionAdapterImpl
{
	SessionData createSession( String sessionId, SessionWrapper wrapper ) throws SessionException.Error;

	List<SessionData> getSessions() throws SessionException.Error;
}
