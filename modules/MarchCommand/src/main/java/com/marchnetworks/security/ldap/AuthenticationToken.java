package com.marchnetworks.security.ldap;

public class AuthenticationToken
{
	private boolean authenticated;
	private LDAPField ldapField;

	public AuthenticationToken()
	{
		authenticated = false;
	}

	public boolean isAuthenticated()
	{
		return authenticated;
	}

	public void setAuthenticated( boolean authenticated )
	{
		this.authenticated = authenticated;
	}

	public void setAuthenticated( boolean authenticated, LDAPField ldapField )
	{
		this.authenticated = authenticated;
		this.ldapField = ldapField;
	}

	public LDAPField getLdapField()
	{
		return ldapField;
	}

	public void setLdapField( LDAPField ldapField )
	{
		this.ldapField = ldapField;
	}
}

