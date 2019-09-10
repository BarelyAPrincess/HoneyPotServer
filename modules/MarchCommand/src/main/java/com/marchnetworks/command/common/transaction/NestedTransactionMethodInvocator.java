package com.marchnetworks.command.common.transaction;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class NestedTransactionMethodInvocator implements TransactionMethodInvocator
{
	@Transactional( propagation = Propagation.REQUIRED )
	public Object proceed( MethodInvocation invocation ) throws Throwable
	{
		return invocation.proceed();
	}
}
