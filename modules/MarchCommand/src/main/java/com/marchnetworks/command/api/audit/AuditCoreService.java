package com.marchnetworks.command.api.audit;

public abstract interface AuditCoreService
{
	public abstract void logAppAudit( AppAuditData paramAppAuditData, UserContext paramUserContext );
}
