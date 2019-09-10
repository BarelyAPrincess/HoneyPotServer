package com.marchnetworks.monitoring.diagnostics;

public abstract interface DatabaseDiagnosticCheck
{
	public abstract void checkDatabase();

	public abstract void checkDatabaseInternal();
}

