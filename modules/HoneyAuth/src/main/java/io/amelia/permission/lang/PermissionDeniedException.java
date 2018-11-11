/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission.lang;

import io.amelia.permission.Permission;

/**
 * Used to communicate Permission Denied back to a calling EvalFactory.
 */
public class PermissionDeniedException extends PermissionException
{
	private static final long serialVersionUID = -6010688682541616132L;

	final PermissionDeniedReason reason;

	public PermissionDeniedException( PermissionDeniedReason reason )
	{
		super( reason.getMessage() );
		this.reason = reason;
	}

	public int getHttpCode()
	{
		return reason.getHttpCode();
	}

	@Override
	public String getMessage()
	{
		return reason.getMessage();
	}

	public PermissionDeniedReason getReason()
	{
		return reason;
	}

	public enum PermissionDeniedReason
	{
		LOGIN_PAGE,
		OP_ONLY,
		DENIED;

		int httpCode = 401;
		Permission perm = null;

		public int getHttpCode()
		{
			return httpCode;
		}

		public PermissionDeniedReason setHttpCode( int httpCode )
		{
			this.httpCode = httpCode;
			return this;
		}

		public String getMessage()
		{
			switch ( this )
			{
				case LOGIN_PAGE:
					return "You must be logged in to view this page!";
				case OP_ONLY:
					return "This page is limited to server operators only!";
				case DENIED:
					if ( perm != null )
						return "You do not possess the permission \"" + perm.getNamespace() + "\", which is required to view this page.";
					return "You do not possess the permissions required to view this page.";
				default:
					return "<Unknown Reason>";
			}
		}

		public PermissionDeniedReason setPermission( Permission perm )
		{
			this.perm = perm;
			return this;
		}
	}
}
