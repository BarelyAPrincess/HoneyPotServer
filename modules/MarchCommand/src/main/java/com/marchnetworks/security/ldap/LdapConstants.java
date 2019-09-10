package com.marchnetworks.security.ldap;

public class LdapConstants
{
	public static final int MAX_AUTHENTICATION_RETRY = 3;

	public static final String SIMPLE_AUTH_METHOD = "SIMPLE";

	public static final String DIGEST_AUTH_METHOD = "DIGEST";

	private static final String DOMAIN_COMPONENT = "DC=";

	public static boolean isUsernameQualified( String username )
	{
		return ( username.contains( "DC=" ) ) || ( username.contains( "@" ) );
	}

	public static boolean isDistinguishedName( String value )
	{
		return value.contains( "DC=" );
	}
}

