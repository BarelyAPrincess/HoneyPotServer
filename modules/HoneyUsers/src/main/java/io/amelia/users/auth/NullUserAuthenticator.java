/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users.auth;

import io.amelia.lang.UserException;
import io.amelia.lang.DescriptiveReason;
import io.amelia.users.UserBackend;
import io.amelia.users.UserMeta;
import io.amelia.users.UserPermissible;

/**
 * Used to authenticate the NULL user
 */
public final class NullUserAuthenticator extends UserAuthenticator
{
	class NullUserCredentials extends UserCredentials
	{
		NullUserCredentials( UserMeta user, DescriptiveReason.DescriptiveReason descriptiveReason )
		{
			super( NullUserAuthenticator.this, user, descriptiveReason.newUserResult() );
		}
	}

	NullUserAuthenticator()
	{
		super( "null" );
	}

	@Override
	public UserCredentials authorize( UserMeta user, UserPermissible permissible ) throws UserException.Error
	{
		return new NullUserCredentials( user, UserBackend.isNullUser( user ) ? DescriptiveReason.LOGIN_SUCCESS : DescriptiveReason.INCORRECT_LOGIN );
	}

	@Override
	public UserCredentials authorize( UserMeta user, Object... credentials ) throws UserException.Error
	{
		return new NullUserCredentials( user, UserBackend.isNullUser( user ) ? DescriptiveReason.LOGIN_SUCCESS : DescriptiveReason.INCORRECT_LOGIN );
	}
}
