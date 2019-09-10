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

import io.amelia.http.webroot.Webroot;

/**
 * Provides a context to when a session is created
 */
public interface SessionContext
{
	String getIpAddress();

	Webroot getWebroot();
}
