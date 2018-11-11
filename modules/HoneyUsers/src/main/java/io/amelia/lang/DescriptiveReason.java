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

public class DescriptiveReason
{
	public static final DescriptiveReason NULL = new DescriptiveReason( ReportingLevel.L_DEFAULT, "This is an empty result and should never be used." );

	public static final DescriptiveReason GENERAL_SUCCESS = new DescriptiveReason( ReportingLevel.L_SUCCESS, "The requested action was completed successfully!" );
	public static final DescriptiveReason LOGOUT_SUCCESS = new DescriptiveReason( ReportingLevel.L_SUCCESS, "You have been successfully logged out." );
	public static final DescriptiveReason LOGIN_SUCCESS = new DescriptiveReason( ReportingLevel.L_SUCCESS, "Your login has been successfully authenticated." );

	public static final DescriptiveReason EMPTY_USERNAME = new DescriptiveReason( ReportingLevel.L_ERROR, "Please provide a valid username." );
	public static final DescriptiveReason EMPTY_CREDENTIALS = new DescriptiveReason( ReportingLevel.L_ERROR, "Please provide a valid password or other credentials." );
	public static final DescriptiveReason EMPTY_ID = new DescriptiveReason( ReportingLevel.L_ERROR, "Please provide a valid user id number." );

	public static final DescriptiveReason INCORRECT_LOGIN = new DescriptiveReason( ReportingLevel.L_DENIED, "Incorrect Login" );
	public static final DescriptiveReason EXPIRED_LOGIN = new DescriptiveReason( ReportingLevel.L_EXPIRED, "Your user is expired, access denied." );
	public static final DescriptiveReason PASSWORD_UNSET = new DescriptiveReason( ReportingLevel.L_DENIED, "Your user has no password, access denied." );
	public static final DescriptiveReason UNAUTHORIZED = new DescriptiveReason( ReportingLevel.L_DENIED, "You are unauthorized to access the requested resource." );

	public static final DescriptiveReason USER_EXISTS = new DescriptiveReason( ReportingLevel.L_DENIED, "The username specified is already in use. Please try a different username." );

	public static final DescriptiveReason USER_NOT_INITIALIZED = new DescriptiveReason( ReportingLevel.L_ERROR, "User Failed Validation" );
	public static final DescriptiveReason USER_NOT_ACTIVATED = new DescriptiveReason( ReportingLevel.L_DENIED, "User Not Activated" );
	public static final DescriptiveReason USER_NOT_WHITELISTED = new DescriptiveReason( ReportingLevel.L_SECURITY, "User Not Whitelisted" );
	public static final DescriptiveReason USER_BANNED = new DescriptiveReason( ReportingLevel.L_SECURITY, "User Banned. THE BAN HAMMER HAS SPOKEN!" );

	public static final DescriptiveReason UNDER_ATTACK = new DescriptiveReason( ReportingLevel.L_SECURITY, "You've exceeded your max number of failed login, temporarily access denied." );
	public static final DescriptiveReason IP_BANNED = new DescriptiveReason( ReportingLevel.L_SECURITY, "You have been banned from this server." );
	public static final DescriptiveReason NONCE_REQUIRED = new DescriptiveReason( ReportingLevel.L_SECURITY, "Your login failed the NONCE validation." );

	public static final DescriptiveReason INTERNAL_ERROR = new DescriptiveReason( ReportingLevel.E_ERROR, "Internal Server Error" );
	public static final DescriptiveReason UNKNOWN_ERROR = new DescriptiveReason( ReportingLevel.E_ERROR, "Unknown Internal Error" );
	public static final DescriptiveReason PERMISSION_ERROR = new DescriptiveReason( ReportingLevel.L_PERMISSION, "Permission Error" ); // TODO Implement custom class
	public static final DescriptiveReason CONFIGURATION_ERROR = new DescriptiveReason( ReportingLevel.L_ERROR, "Server Configuration Error." );
	public static final DescriptiveReason CANCELLED_BY_EVENT = new DescriptiveReason( ReportingLevel.L_ERROR, "Your login was cancelled by an internal event." ); // TODO Implement custom class

	public static final DescriptiveReason FEATURE_DISABLED = new DescriptiveReason( ReportingLevel.L_ERROR, "Feature Disallowed" );
	public static final DescriptiveReason FEATURE_NOT_IMPLEMENTED = new DescriptiveReason( ReportingLevel.L_ERROR, "Feature Not Implemented" );

	private final ReportingLevel level;
	private final String reasonMessage;

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
}
