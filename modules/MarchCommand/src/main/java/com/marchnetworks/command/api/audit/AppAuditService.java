package com.marchnetworks.command.api.audit;

public abstract interface AppAuditService
{
	public abstract void logAudit( AppAuditData paramAppAuditData );

	public abstract void logNonProxyAudit( AppAuditData paramAppAuditData );
}
