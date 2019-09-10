package com.marchnetworks.monitoring.diagnostics;

import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.transaction.DatabaseFailureException;
import com.marchnetworks.command.common.transaction.TransactionExceptionTranslator;
import com.marchnetworks.common.diagnostics.DiagnosticError;
import com.marchnetworks.common.diagnostics.DiagnosticTestDAO;
import com.marchnetworks.common.diagnostics.DiagnosticTestEntity;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.JavaUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DatabaseDiagnosticCheckImpl implements DatabaseDiagnosticCheck
{
	private static final Logger LOG = LoggerFactory.getLogger( DatabaseDiagnosticCheckImpl.class );

	private static final long MAX_WAIT_TIME = 120000L;

	private DiagnosticTestDAO diagnosticTestDAO;
	private DiagnosticsServiceIF diagnosticsService;
	private TaskScheduler taskScheduler;

	public void checkDatabase()
	{
		long start = System.currentTimeMillis();

		final CountDownLatch notification = new CountDownLatch( 1 );
		taskScheduler.executeSerial( new Runnable()
		{
			public void run()
			{
				DatabaseDiagnosticCheck databaseDiagnosticCheck = ( DatabaseDiagnosticCheck ) ApplicationContextSupport.getBean( "databaseDiagnosticCheckProxy" );
				databaseDiagnosticCheck.checkDatabaseInternal();

				notification.countDown();
			}
		}, DatabaseDiagnosticCheck.class.getSimpleName() );

		try
		{
			if ( !notification.await( 120000L, TimeUnit.MILLISECONDS ) )
			{
				JavaUtils.generateThreadDump();
				LOG.error( "Diagnostics unable to complete database check, time " + ( System.currentTimeMillis() - start ) );
				diagnosticsService.notifyFailure( DiagnosticError.DATABASE, "Database failure detected, check server logs for exception stack traces" );
			}
		}
		catch ( InterruptedException e )
		{
			LOG.error( "Interrupted while waiting for database check, " + e.getMessage() );
		}
	}

	public void checkDatabaseInternal()
	{
		long start = System.currentTimeMillis();
		try
		{
			DiagnosticTestEntity test = null;
			List<DiagnosticTestEntity> results = diagnosticTestDAO.findAll();
			if ( !results.isEmpty() )
			{
				test = ( DiagnosticTestEntity ) results.get( 0 );
			}
			if ( test == null )
			{
				test = new DiagnosticTestEntity();
				diagnosticTestDAO.create( test );

				test.setTestValue( "test" );
				diagnosticTestDAO.flush();
			}
			diagnosticsService.notifyResolved( DiagnosticError.DATABASE );
		}
		catch ( Exception e )
		{
			LOG.info( "Total time spent checking database in case of error: " + ( System.currentTimeMillis() - start ) );
			Exception translated = TransactionExceptionTranslator.translateException( e );

			if ( ( translated instanceof DatabaseFailureException ) )
			{
				JavaUtils.generateThreadDump();

				LOG.error( "Diagnostics detected a database failure", translated );
				diagnosticsService.notifyFailure( DiagnosticError.DATABASE, "Database failure detected, check server logs for exception stack traces" );
			}
			else
			{
				LOG.info( "Diagnostics ignoring database exception", translated );
			}
		}
	}

	public void setDiagnosticTestDAO( DiagnosticTestDAO diagnosticTestDAO )
	{
		this.diagnosticTestDAO = diagnosticTestDAO;
	}

	public void setDiagnosticsService( DiagnosticsServiceIF diagnosticsService )
	{
		this.diagnosticsService = diagnosticsService;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}
}

