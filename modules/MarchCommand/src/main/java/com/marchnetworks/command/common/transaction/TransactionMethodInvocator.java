package com.marchnetworks.command.common.transaction;

import org.aopalliance.intercept.MethodInvocation;

public interface TransactionMethodInvocator
{
	Object proceed( MethodInvocation paramMethodInvocation ) throws Throwable;
}
