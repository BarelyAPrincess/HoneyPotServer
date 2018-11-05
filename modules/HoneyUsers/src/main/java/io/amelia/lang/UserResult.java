/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

import io.amelia.support.Objs;
import io.amelia.support.TriFunction;
import io.amelia.users.UserMeta;

public class UserResult
{
	public static final DescriptiveReason NULL = GeneralDescriptiveReason.make( ReportingLevel.L_DEFAULT, "This is an empty result and should never be used." );

	public static final DescriptiveReason GENERAL_SUCCESS = GeneralDescriptiveReason.make( ReportingLevel.L_SUCCESS, "The requested action was completed successfully!" );
	public static final DescriptiveReason LOGOUT_SUCCESS = GeneralDescriptiveReason.make( ReportingLevel.L_SUCCESS, "You have been successfully logged out." );
	public static final DescriptiveReason LOGIN_SUCCESS = GeneralDescriptiveReason.make( ReportingLevel.L_SUCCESS, "Your login has been successfully authenticated." );

	public static final DescriptiveReason EMPTY_USERNAME = GeneralDescriptiveReason.make( ReportingLevel.L_ERROR, "Please provide a valid username." );
	public static final DescriptiveReason EMPTY_CREDENTIALS = GeneralDescriptiveReason.make( ReportingLevel.L_ERROR, "Please provide a valid password or other credentials." );
	public static final DescriptiveReason EMPTY_ID = GeneralDescriptiveReason.make( ReportingLevel.L_ERROR, "Please provide a valid user id number." );

	public static final DescriptiveReason INCORRECT_LOGIN = GeneralDescriptiveReason.make( ReportingLevel.L_DENIED, "Incorrect Login" );
	public static final DescriptiveReason EXPIRED_LOGIN = GeneralDescriptiveReason.make( ReportingLevel.L_EXPIRED, "Your user is expired, access denied." );
	public static final DescriptiveReason PASSWORD_UNSET = GeneralDescriptiveReason.make( ReportingLevel.L_DENIED, "Your user has no password, access denied." );
	public static final DescriptiveReason UNAUTHORIZED = GeneralDescriptiveReason.make( ReportingLevel.L_DENIED, "You are unauthorized to access the requested resource." );

	public static final DescriptiveReason USER_EXISTS = GeneralDescriptiveReason.make( ReportingLevel.L_DENIED, "The username specified is already in use. Please try a different username." );

	public static final DescriptiveReasonWithDefinedException<UserException.Error> USER_NOT_INITIALIZED = DescriptiveReasonWithDefinedException.make( ReportingLevel.L_ERROR, "User Failed Validation" );
	public static final DescriptiveReasonWithDefinedException<UserException.Error> USER_NOT_ACTIVATED = DescriptiveReasonWithDefinedException.make( ReportingLevel.L_DENIED, "User Not Activated" );
	public static final DescriptiveReasonWithDefinedException<UserException.Error> USER_NOT_WHITELISTED = DescriptiveReasonWithDefinedException.make( ReportingLevel.L_SECURITY, "User Not Whitelisted" );
	public static final DescriptiveReasonWithDefinedException<UserException.Error> USER_BANNED = DescriptiveReasonWithDefinedException.make( ReportingLevel.L_SECURITY, "User Banned. THE BAN HAMMER HAS SPOKEN!" );

	public static final DescriptiveReasonWithDefinedException<UserException.Error> UNDER_ATTACK = DescriptiveReasonWithDefinedException.make( ReportingLevel.L_SECURITY, "You've exceeded your max number of failed login, temporarily access denied." );
	public static final DescriptiveReasonWithDefinedException<UserException.Error> IP_BANNED = DescriptiveReasonWithDefinedException.make( ReportingLevel.L_SECURITY, "You have been banned from this server." );
	public static final DescriptiveReasonWithDefinedException<UserException.Error> NONCE_REQUIRED = DescriptiveReasonWithDefinedException.make( ReportingLevel.L_SECURITY, "Your login failed the NONCE validation." );

	public static final DescriptiveReasonWithUserException<Exception> INTERNAL_ERROR = DescriptiveReasonWithUserException.make( ReportingLevel.E_ERROR, "Internal Server Error" );
	public static final DescriptiveReasonWithDefinedException<UserException.Error> UNKNOWN_ERROR = DescriptiveReasonWithDefinedException.make( ReportingLevel.E_ERROR, "Unknown Internal Error" );
	public static final DescriptiveReasonWithDefinedException<UserException.Error> PERMISSION_ERROR = DescriptiveReasonWithDefinedException.make( ReportingLevel.L_PERMISSION, "Permission Error" ); // TODO Implement custom class
	public static final DescriptiveReasonWithDefinedException<UserException.Error> CONFIGURATION_ERROR = DescriptiveReasonWithDefinedException.make( ReportingLevel.L_ERROR, "Server Configuration Error." );
	public static final DescriptiveReasonWithDefinedException<UserException.Error> CANCELLED_BY_EVENT = DescriptiveReasonWithDefinedException.make( ReportingLevel.L_ERROR, "Your login was cancelled by an internal event." ); // TODO Implement custom class

	public static final DescriptiveReasonWithDefinedException<UserException.Error> FEATURE_DISABLED = DescriptiveReasonWithDefinedException.make( ReportingLevel.L_ERROR, "Feature Disallowed" );
	public static final DescriptiveReasonWithDefinedException<UserException.Error> FEATURE_NOT_IMPLEMENTED = DescriptiveReasonWithDefinedException.make( ReportingLevel.L_ERROR, "Feature Not Implemented" );

	private final DescriptiveReason descriptiveReason;

	public UserResult( DescriptiveReason descriptiveReason )
	{
		this.descriptiveReason = descriptiveReason;
	}

	public DescriptiveReason getDescriptiveReason()
	{
		return descriptiveReason;
	}

	public ReportingLevel getReportingLevel()
	{
		return descriptiveReason.getReportingLevel();
	}

	public static class UserResultWithException<E extends Exception> extends UserResult
	{
		private E exception;

		public UserResultWithException( DescriptiveReason reason )
		{
			super( reason );
		}

		public UserResultWithException( DescriptiveReason reason, E exception )
		{
			super( reason );
			setException( exception );
		}

		public void setException( E exception )
		{
			this.exception = exception;
		}

		public E getException()
		{
			return exception;
		}
	}

	public static class DescriptiveReasonWithDefinedException<E extends Exception> extends DescriptiveReason
	{
		private final TriFunction<UserMeta, UserResultWithException<E>, DescriptiveReasonWithDefinedException<E>, E> exceptionFunction;

		static DescriptiveReasonWithDefinedException<UserException.Error> make( ReportingLevel level, String reason )
		{
			return make( level, reason, ( userMeta, userResult, descriptiveReason ) -> new UserException.Error( userMeta, descriptiveReason.getReportingLevel(), descriptiveReason.getReasonMessage() ) );
		}

		static <E extends Exception> DescriptiveReasonWithDefinedException<E> make( ReportingLevel level, String reason, TriFunction<UserMeta, UserResultWithException<E>, DescriptiveReasonWithDefinedException<E>, E> exceptionFunction )
		{
			return new DescriptiveReasonWithDefinedException( level, reason, exceptionFunction );
		}

		private DescriptiveReasonWithDefinedException( ReportingLevel level, String reason, TriFunction<UserMeta, UserResultWithException<E>, DescriptiveReasonWithDefinedException<E>, E> exceptionFunction )
		{
			super( level, reason );
			this.exceptionFunction = exceptionFunction;
		}

		public UserResultWithException<E> getUserResult( UserMeta userMeta )
		{
			UserResultWithException<E> userResult = new UserResultWithException( this );
			userResult.setException( Objs.notNull( exceptionFunction.apply( userMeta, userResult, this ) ) );
			return userResult;
		}
	}

	public static class DescriptiveReasonWithUserException<E extends Exception> extends DescriptiveReason
	{
		static <E extends Exception> DescriptiveReasonWithUserException<E> make( ReportingLevel level, String reason )
		{
			return new DescriptiveReasonWithUserException( level, reason );
		}

		private DescriptiveReasonWithUserException( ReportingLevel level, String reason )
		{
			super( level, reason );
		}

		public UserResultWithException<E> getUserResult( E exception )
		{
			UserResultWithException<E> userResult = new UserResultWithException( this );
			userResult.setException( exception );
			return userResult;
		}
	}

	public static class GeneralDescriptiveReason extends DescriptiveReason
	{
		public static GeneralDescriptiveReason make( ReportingLevel level, String reasonMessage )
		{
			return new GeneralDescriptiveReason( level, reasonMessage );
		}

		private GeneralDescriptiveReason( ReportingLevel level, String reasonMessage )
		{
			super( level, reasonMessage );
		}
	}

	public static class DescriptiveReason
	{
		private final String reasonMessage;
		private final ReportingLevel level;

		private DescriptiveReason( ReportingLevel level, String reasonMessage )
		{
			this.reasonMessage = reasonMessage;
			this.level = level;
		}

		public String getReasonMessage()
		{
			return reasonMessage;
		}

		public ReportingLevel getReportingLevel()
		{
			return level;
		}

		public UserResult newUserResult()
		{
			return new UserResult( this );
		}

		public <E extends Exception> UserResultWithException<E> newUserResult( E exception )
		{
			return new UserResultWithException<>( this, exception );
		}
	}
}
