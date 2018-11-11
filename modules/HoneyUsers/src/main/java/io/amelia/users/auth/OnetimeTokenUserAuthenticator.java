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

import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.DatabaseException;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.DescriptiveReason;
import io.amelia.support.DateAndTime;
import io.amelia.support.Encrypt;
import io.amelia.users.UserMeta;

/**
 * Used to authenticate an account using an Account Id and Token combination
 */
public class OnetimeTokenUserAuthenticator extends UserAuthenticator
{
	class OnetimeTokenUserCredentials extends UserCredentials
	{
		private String token;

		OnetimeTokenUserCredentials( UserMeta meta, DescriptiveReason.DescriptiveReason descriptiveReason, String token )
		{
			super( OnetimeTokenUserAuthenticator.this, meta, descriptiveReason.newUserResult() );
			this.token = token;
		}

		public String getToken()
		{
			return token;
		}
	}

	private final Database db = StorageModule.i().getDatabase();

	OnetimeTokenUserAuthenticator()
	{
		super( "token" );

		try
		{
			if ( !db.table( "accounts_token" ).exists() )
				db.table( "accounts_token" ).columnCreateVar( "acctId", 255 ).columnCreateVar( "token", 255 ).columnCreateInt( "expires", 12 );
		}
		catch ( DatabaseException e )
		{
			e.printStackTrace();
		}

		TaskManager.instance().scheduleAsyncRepeatingTask( AccountManager.i(), 0L, Ticks.MINUTE * ConfigRegistry.i().getInt( "sessions.cleanupInterval", 5 ), new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					int deleted = db.table( "accounts_token" ).delete().where( "expires" ).moreThan( 0 ).and().where( "expires" ).lessThan( Timings.epoch() ).executeWithException().count();
					if ( deleted > 0 )
						AccountManager.getLogger().info( EnumColor.DARK_AQUA + "The cleanup task deleted " + deleted + " expired login token(s)." );
				}
				catch ( DatabaseException e )
				{
					e.printStackTrace();
				}
			}
		} );

		EventDispatcher.i().registerEvents( this, this );
	}

	@Override
	public UserCredentials authorize( AccountMeta acct, AccountPermissible perm ) throws AccountException
	{
		if ( acct == null )
			throw new AccountException( AccountDescriptiveReason.INCORRECT_LOGIN, acct );

		String token = perm.getVariable( "token" );

		if ( token == null )
			throw new AccountException( new AccountDescriptiveReason( "The account '" + acct.getId() + "' has no resumable login using the token method.", ReportingLevel.L_ERROR ), acct );

		return authorize( acct, token );
	}

	@Override
	public UserCredentials authorize( AccountMeta acct, Object... credentials ) throws AccountException
	{
		if ( acct == null )
			throw new AccountException( AccountDescriptiveReason.INCORRECT_LOGIN, acct );

		if ( credentials[0] instanceof AccountPermissible )
			return authorize( acct, ( AccountPermissible ) credentials[0] );

		if ( credentials.length == 0 || !( credentials[0] instanceof String ) )
			throw new AccountException( AccountDescriptiveReason.INTERNAL_ERROR, acct );

		String acctId = acct.getId();
		String token = ( String ) credentials[0];

		try
		{
			if ( token == null || token.isEmpty() )
				throw new AccountException( AccountDescriptiveReason.EMPTY_CREDENTIALS, acct );

			ElegantQuerySelect select = db.table( "accounts_token" ).select().where( "acctId" ).matches( acctId ).and().where( "token" ).matches( token ).limit( 1 ).executeWithException();

			if ( select.count() == 0 )
				throw new AccountException( AccountDescriptiveReason.INCORRECT_LOGIN, acct );
			// throw AccountResult.INCORRECT_LOGIN.setMessage( "The provided token did not match any saved tokens" + ( Versioning.isDevelopment() ? ", token: " + token : "." ) ).exception();

			if ( select.getInt( "expires" ) >= 0 && select.getInt( "expires" ) < Timings.epoch() )
				throw new AccountException( AccountDescriptiveReason.EXPIRED_LOGIN, acct );

			// deleteToken( acctId, token );
			expireToken( acctId, token );
			return new OnetimeTokenUserCredentials( AccountDescriptiveReason.LOGIN_SUCCESS, acct, token );
		}
		catch ( DatabaseException e )
		{
			throw new AccountException( AccountDescriptiveReason.INTERNAL_ERROR, e, acct );
		}
	}

	/**
	 * Deletes provided token from database
	 *
	 * @param acctId The acctId associated with Token
	 * @param token  The login token
	 */
	public boolean deleteToken( String acctId, String token )
	{
		Validate.notNull( acctId );
		Validate.notNull( token );

		try
		{
			return db.table( "accounts_token" ).delete().where( "acctId" ).matches( acctId ).and().where( "token" ).matches( token ).executeWithException().count() > 0;
		}
		catch ( DatabaseException e )
		{
			return false;
		}
	}

	/**
	 * Expires the provided token from database
	 *
	 * @param acctId The acctId associated with Token
	 * @param token  The login token
	 */
	private boolean expireToken( @Nonnull String acctId, @Nonnull String token )
	{
		try
		{
			return db.table( "accounts_token" ).update().value( "expires", 0 ).where( "acctId" ).matches( acctId ).and().where( "token" ).matches( token ).executeWithException().count() > 0;
		}
		catch ( DatabaseException e )
		{
			return false;
		}
	}

	/**
	 * Issues a new login token used for authenticating users later without storing the password.
	 *
	 * @param user The user to issue to the token to
	 *
	 * @return The issued token, be sure to save the token, authenticate with the token later. Token is valid for 7 days.
	 */
	public String issueToken( @Nonnull UserMeta user )
	{
		String token = Encrypt.randomize( user.getUuid() ) + DateAndTime.epoch();
		try
		{
			user.getUserBackend()

			// if ( db.queryUpdate( "INSERT INTO `accounts_token` (`acctId`,`token`,`expires`) VALUES (?,?,?);", acct.getId(), token, ( Timings.epoch() + ( 60 * 60 * 24 * 7 ) ) ) < 1 )
			if ( db.table( "accouts_token" ).insert().value( "acctId", user.getId() ).value( "token", token ).value( "expires", Timings.epoch() + 60 * 60 * 24 * 7 ).executeWithException().count() < 0 )
			{
				AccountManager.getLogger().severe( "We had an unknown issue inserting token '" + token + "' into the database!" );
				return null;
			}
		}
		catch ( DatabaseException e )
		{
			e.printStackTrace();
			return null;
		}
		return token;
	}
}
