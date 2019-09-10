package com.marchnetworks.command.common.user.data;

public enum MemberTypeEnum
{
	LDAP_USER( "LDAP User" ),
	LOCAL_USER( "Local User" ),
	GROUP_USER( "LDAP Group User" ),
	GROUP( "LDAP Group" );

	private String value;

	private MemberTypeEnum( String value )
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}
}
