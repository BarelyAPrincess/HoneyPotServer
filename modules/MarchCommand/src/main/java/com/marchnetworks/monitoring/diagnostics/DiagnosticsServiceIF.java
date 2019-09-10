package com.marchnetworks.monitoring.diagnostics;

import com.marchnetworks.common.diagnostics.DiagnosticError;
import com.marchnetworks.common.diagnostics.DiagnosticResult;
import com.marchnetworks.common.diagnostics.WatchdogNotification;

public abstract interface DiagnosticsServiceIF
{
	public abstract DiagnosticResult checkFailures();

	public abstract void notifyFailure( DiagnosticError paramDiagnosticError, String paramString );

	public abstract void notifyResolved( DiagnosticError paramDiagnosticError );

	public abstract void notifyRestartComplete( WatchdogNotification paramWatchdogNotification );
}

