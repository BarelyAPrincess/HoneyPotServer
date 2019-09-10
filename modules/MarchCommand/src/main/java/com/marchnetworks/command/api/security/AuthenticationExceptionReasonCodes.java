package com.marchnetworks.command.api.security;

public enum AuthenticationExceptionReasonCodes
{
	NOT_AUTHORIZED( "not_authorized" ),
	FAILED( "failed" ),
	DATABASE_CONNECTION_LOST( "database_connection_lost" ),
	LOCKED( "locked" ),
	BUSY( "busy" ),
	INVALID_CERTIFICATE( "invalid_certificate" ),
	LDAP_CONNECTION_FAILED( "ldap_connection_failed" ),
	LDAP_ACCOUNT_NOT_UNIQUE( "ldap_account_not_unique" ),
	LDAP_ACCOUNT_NOT_FOUND( "ldap_account_not_found" ),
	LDAP_GROUP_NOT_IN_CES( "ldap_group_not_in_ces" );

	private final String id;

	private AuthenticationExceptionReasonCodes( String id )
	{
		this.id = id;
	}

	public String getValue()
	{
		return id;
	}
}
