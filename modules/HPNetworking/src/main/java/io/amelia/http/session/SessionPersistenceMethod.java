/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.session;

/**
 * Method used to keep a Session persistent from request to request.
 * 
 * XXX This is an outdated feature from like version 6, not sure if it's still working as of version 9.
 */
public enum SessionPersistenceMethod
{
	COOKIE, PARAM
}
