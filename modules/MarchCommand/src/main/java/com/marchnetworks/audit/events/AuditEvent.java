package com.marchnetworks.audit.events;

import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.common.event.Event;

public class AuditEvent extends Event
{
	private AuditView auditView;

	public AuditEvent()
	{
		super( AuditEvent.class.getName() );
	}

	public AuditEvent( AuditView auditView )
	{
		super( AuditEvent.class.getName() );
		this.auditView = auditView;
	}

	public AuditView getAuditView()
	{
		return auditView;
	}

	public void setAuditView( AuditView auditView )
	{
		this.auditView = auditView;
	}
}
