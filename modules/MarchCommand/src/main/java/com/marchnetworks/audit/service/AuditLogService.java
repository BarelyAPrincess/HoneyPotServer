package com.marchnetworks.audit.service;

import com.marchnetworks.audit.common.AuditLogException;
import com.marchnetworks.audit.data.AuditSearchQuery;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.DeviceAuditView;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;

import java.util.List;

public abstract interface AuditLogService
{
	public abstract List<AuditView> getAuditLogs( AuditSearchQuery paramAuditSearchQuery ) throws AuditLogException;

	public abstract List<DeviceAuditView> getDeviceAuditLogs( long paramLong1, long paramLong2 ) throws AuditLogException;

	public abstract void logAuditEvent( Event paramEvent );

	public abstract void logAudit( AuditView paramAuditView );

	public abstract void purgeOldAuditLogs( long paramLong );

	public abstract void processDeviceUnregistration( DeviceRegistrationEvent paramDeviceRegistrationEvent );

	public abstract void deleteAuditLogsByAppid( String paramString );
}
