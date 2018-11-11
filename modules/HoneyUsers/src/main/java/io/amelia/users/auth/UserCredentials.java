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

import javax.annotation.Nonnull;

import io.amelia.lang.ReportingLevel;
import io.amelia.lang.UserException;
import io.amelia.lang.DescriptiveReason;
import io.amelia.users.UserBackend;
import io.amelia.users.UserMeta;
import io.amelia.users.UserPermissible;

/**
 * Provides login credentials to the {@link UserAuthenticator}
 */
public abstract class UserCredentials
{
	private final UserAuthenticator userAuthenticator;
	private final DescriptiveReason userResult;
	private final UserMeta userMeta;

	UserCredentials( UserAuthenticator userAuthenticator, UserMeta userMeta, DescriptiveReason userResult )
	{
		this.userAuthenticator = userAuthenticator;
		this.userMeta = userMeta;
		this.userResult = userResult;
	}

	public final UserMeta getUser()
	{
		return userMeta;
	}

	public final UserAuthenticator getUserAuthenticator()
	{
		return userAuthenticator;
	}

	public final DescriptiveReason getUserResult()
	{
		return userResult;
	}

	/**
	 * Saves credentials to the session for later retrieval.
	 *
	 * @param user The UserPermissible (A wrapper class of the session) to store the credentials
	 *
	 * @throws UserException.Error If there are issues handling the account
	 */
	public void saveCredentialsToSession( @Nonnull UserPermissible user ) throws UserException.Error
	{
		if ( user.getUserMeta() != userMeta )
			throw new UserException.Error( userMeta, ReportingLevel.L_ERROR, "These credentials don't match the provided permissible." );

		if ( !userResult.getReportingLevel().isSuccess() )
			throw new UserException.Error( userMeta, ReportingLevel.L_DENIED, "Can't save credentials unless they were successful." );

		if ( UserBackend.isNoneAccount( userMeta ) )
			throw new UserException.Error( userMeta, ReportingLevel.L_SECURITY, "These credentials can't be saved." );

		if ( "token".equals( user.getVariable( "auth" ) ) && user.getVariable( "token" ) != null )
			UserAuthenticator.TOKEN.deleteToken( user.getVariable( "userId" ), user.getVariable( "token" ) );

		user.setVariable( "auth", "token" );
		user.setVariable( "locationId", userMeta.getLocationId() );
		user.setVariable( "userId", userMeta.getUuid() );
		user.setVariable( "token", UserAuthenticator.TOKEN.issueToken( userMeta ) );
	}
}
