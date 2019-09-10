package com.marchnetworks.app.core;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class AppContextInterceptor implements MethodInterceptor
{
	private static final ClassLoader COMMAND_CLASSLOADER = AppContextInterceptor.class.getClassLoader();

	public Object invoke( MethodInvocation invocation ) throws Throwable
	{
		Object ret;

		ClassLoader callingThreadClassLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader( COMMAND_CLASSLOADER );
			ret = invocation.proceed();
		}
		finally
		{
			Thread.currentThread().setContextClassLoader( callingThreadClassLoader );
		}

		return ret;
	}
}
