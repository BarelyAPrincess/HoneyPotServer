package com.marchnetworks.esm.util;

import javax.naming.directory.DirContext;

public class UserManagementContext
{
	private static UserManagementContext instance = new UserManagementContext();

	private DirContext ctx = null;

	private transient String loginUser = null;

	private transient String loginPasswd = null;

	public static UserManagementContext getInstance()
	{
		return instance;
	}

	public DirContext getDirContext()
	{
		return ctx;
	}

	public void setDirContext( DirContext newCtx )
	{
		ctx = newCtx;
	}

	public String getLoginUser()
	{
		return loginUser;
	}

	public void setLoginUser( String loginUser )
	{
		this.loginUser = loginUser;
	}

	public String getLoginPasswd()
	{
		return loginPasswd;
	}

	public void setLoginPasswd( String loginPasswd )
	{
		this.loginPasswd = loginPasswd;
	}
}
