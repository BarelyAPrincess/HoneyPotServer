package com.marchnetworks.monitoring.diagnostics;

import com.marchnetworks.command.common.transaction.DatabaseFailureException;
import com.marchnetworks.command.common.transaction.TransactionExceptionTranslator;
import com.marchnetworks.common.diagnostics.DiagnosticTestDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupDiagnosticsImpl implements StartupDiagnostics
{
	private static final Logger LOG = LoggerFactory.getLogger( StartupDiagnosticsImpl.class );

	private DiagnosticTestDAO diagnosticTestDAO;

	public void init()
	{
		LOG.info( "CES starting" );
		for ( ; ; )
		{
			try
			{
				diagnosticTestDAO.findAll();

			}
			catch ( Exception e )
			{
				Exception translated = TransactionExceptionTranslator.translateException( e );

				if ( ( translated instanceof DatabaseFailureException ) )
				{
					LOG.error( "Database not available on startup, Exception:" + translated.getMessage() );
					try
					{
						Thread.sleep( 20000L );
					}
					catch ( InterruptedException localInterruptedException )
					{
					}
				}
			}
		}
	}

	public void setDiagnosticTestDAO( DiagnosticTestDAO diagnosticTestDAO )
	{
		this.diagnosticTestDAO = diagnosticTestDAO;
	}
}

