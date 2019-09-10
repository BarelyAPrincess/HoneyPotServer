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

import java.util.Arrays;
import java.util.Optional;

/**
 * Method used to keep a Session persistent from request to request.
 *
 * TODO This feature is from since version 6 of CWS, not sure if it's still perperly working as of the latest version.
 */
public enum SessionPersistenceMethod
{
	COOKIE,
	PARAM;

	public static Optional<SessionPersistenceMethod> valueOfIgnoreCase( String key )
	{
		return Arrays.stream( values() ).filter( e -> e.name().equalsIgnoreCase( key ) ).findAny();
	}
}
