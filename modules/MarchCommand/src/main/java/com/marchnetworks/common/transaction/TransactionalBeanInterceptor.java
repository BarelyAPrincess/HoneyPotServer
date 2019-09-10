package com.marchnetworks.common.transaction;

import com.marchnetworks.command.common.transaction.BaseTransactionalBeanInterceptor;
import com.marchnetworks.command.common.transaction.TransactionContext;
import com.marchnetworks.common.diagnostics.DiagnosticError;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.monitoring.diagnostics.DiagnosticsServiceIF;
import com.marchnetworks.server.event.EventRegistry;

import java.util.List;

public class TransactionalBeanInterceptor extends BaseTransactionalBeanInterceptor
{
	private static ThreadLocal<TransactionContext<Event>> transactionContext = new ThreadLocal()
	{
		protected TransactionContext<Event> initialValue()
		{
			return new TransactionContext();
		}
	};

	protected EventRegistry eventRegistry;
	protected DiagnosticsServiceIF diagnosticsService;

	public static void sendEventAfterTransactionCommits( Event event )
	{
		TransactionContext<Event> context = ( TransactionContext ) transactionContext.get();
		if ( !context.isTransactionStarted() )
		{
			LOG.warn( "No transaction found, ignoring event to send after commit {}", event );
			transactionContext.remove();
			return;
		}
		context.addEvent( event );
	}

	public void onTransactionSuccess()
	{
		TransactionContext<Event> context = ( TransactionContext ) transactionContext.get();
		List<Event> eventsToSend = context.getEvents();
		transactionContext.remove();

		if ( !eventsToSend.isEmpty() )
		{
			eventRegistry.send( eventsToSend );
		}
	}

	public void onTransactionDatabaseFailure()
	{
		diagnosticsService.notifyFailure( DiagnosticError.DATABASE, "Database failure detected, check server logs for exception stack traces" );
	}

	public TransactionContext<Event> getTransactionContext()
	{
		return ( TransactionContext ) transactionContext.get();
	}

	public void removeTransactionContext()
	{
		transactionContext.remove();
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setDiagnosticsService( DiagnosticsServiceIF diagnosticsService )
	{
		this.diagnosticsService = diagnosticsService;
	}
}
