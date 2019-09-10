package com.marchnetworks.monitoring.diagnostics;

public class StartupDiagnosticsInit
{
	private StartupDiagnostics startupDiagnostics;

	public void init()
	{
		startupDiagnostics.init();
	}

	public void setStartupDiagnostics( StartupDiagnostics startupDiagnostics )
	{
		this.startupDiagnostics = startupDiagnostics;
	}
}

