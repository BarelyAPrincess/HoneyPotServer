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

import com.chiorichan.account.AccountManager;
import com.chiorichan.account.AccountMeta;
import com.chiorichan.account.AccountPermissible;
import com.chiorichan.account.AccountType;
import com.chiorichan.account.lang.AccountDescriptiveReason;
import com.chiorichan.account.lang.AccountException;
import com.chiorichan.tasks.Timings;

import org.apache.commons.codec.digest.DigestUtils;

import io.amelia.lang.DatabaseException;
import io.amelia.storage.Database;
import io.amelia.storage.StorageModule;
import io.amelia.storage.elegant.ElegantQuerySelect;

/**
 * Used to authenticate an account using a Username and Password combination
 */
public final class PlainTextUserAuthenticator extends UserAuthenticator
{
	class PlainTextUserCredentials extends UserCredentials
	{
		PlainTextUserCredentials( AccountDescriptiveReason result, AccountMeta meta )
		{
			super( PlainTextUserAuthenticator.this, meta, result );
		}
	}

	private final Database db = StorageModule.i().getDatabase();

	PlainTextUserAuthenticator()
	{
		super( "plaintext" );

		try
		{
			if ( !db.table( "accounts_plaintext" ).exists() )
				db.table( "accounts_plaintext" ).columnCreateVar( "acctId", 255 ).columnCreateVar( "password", 255 ).columnCreateInt( "expires", 12 );
		}
		catch ( DatabaseException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public UserCredentials authorize( AccountMeta acct, AccountPermissible perm ) throws AccountException
	{
		/**
		 * Session Logins are not resumed using plain text. See {@link UserCredentials#saveCredentialsToSession}
		 */
		throw new AccountException( AccountDescriptiveReason.FEATURE_NOT_IMPLEMENTED, acct );
	}

	@Override
	public UserCredentials authorize( AccountMeta acct, Object... credentials ) throws AccountException
	{
		if ( credentials.length < 1 || !( credentials[0] instanceof String ) )
			throw new AccountException( AccountDescriptiveReason.INTERNAL_ERROR, acct );

		String pass = ( String ) credentials[0];

		if ( acct == null )
			throw new AccountException( AccountDescriptiveReason.INCORRECT_LOGIN, AccountType.ACCOUNT_NONE );

		if ( pass == null || pass.isEmpty() )
			throw new AccountException( AccountDescriptiveReason.EMPTY_CREDENTIALS, acct );

		String acctId = acct.getId();
		String password = null;
		try
		{
			ElegantQuerySelect select = db.table( "accounts_plaintext" ).select().where( "acctId" ).matches( acctId ).limit( 1 ).executeWithException();

			if ( select == null || select.count() < 1 )
				throw new AccountException( AccountDescriptiveReason.PASSWORD_UNSET, acct );

			if ( select.getInt( "expires" ) > -1 && select.getInt( "expires" ) < Timings.epoch() )
				throw new AccountException( AccountDescriptiveReason.EXPIRED_LOGIN, acct );

			password = select.getString( "password" );
		}
		catch ( AccountException e )
		{
			if ( acct.getString( "password" ) != null && !acct.getString( "password" ).isEmpty() )
			{
				// Compatibility with older versions, may soon get removed
				password = acct.getString( "password" );
				setPassword( acct, password, -1 );
				acct.set( "password", null );
			}
			else
				throw e;
		}
		catch ( DatabaseException e )
		{
			throw new AccountException( AccountDescriptiveReason.INTERNAL_ERROR, e, acct );
		}

		// TODO Encrypt all passwords
		if ( password.equals( pass ) || password.equals( DigestUtils.md5Hex( pass ) ) || DigestUtils.md5Hex( password ).equals( pass ) )
			return new PlainTextUserCredentials( AccountDescriptiveReason.LOGIN_SUCCESS, acct );
		else
			throw new AccountException( AccountDescriptiveReason.INCORRECT_LOGIN, acct );
	}

	/**
	 * Similar to {@link #setPassword(AccountMeta, String, int)} except password never expires
	 */
	public void setPassword( AccountMeta acct, String password )
	{
		setPassword( acct, password, -1 );
	}

	/**
	 * Sets the Account Password which is stored in a separate getTable for security
	 *
	 * @param acct     The Account to set password for
	 * @param password The password to set
	 * @param expires  The password expiration. Use -1 for no expiration
	 * @return True if we successfully set the password
	 */
	public boolean setPassword( AccountMeta acct, String password, int expires )
	{
		try
		{
			if ( db.table( "accounts_plaintext" ).insert().value( "acctId", acct.getId() ).value( "password", password ).value( "expires", expires ).executeWithException().count() < 0 )
			{
				AccountManager.getLogger().severe( "We had an unknown issue inserting password for acctId '" + acct.getId() + "' into the database!" );
				return false;
			}

			// db.queryUpdate( "INSERT INTO `accounts_plaintext` (`acctId`,`password`,`expires`) VALUES ('" + acct.getId() + "','" + password + "','" + expires + "');" );
		}
		catch ( DatabaseException e )
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
