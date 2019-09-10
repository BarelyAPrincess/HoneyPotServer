package com.marchnetworks.command.api.audit;

import com.marchnetworks.command.api.event.AppEventCoreService;
import com.marchnetworks.command.api.event.AppSubscriptionPackage;
import com.marchnetworks.command.api.event.EventListener;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventProcessingType;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.scheduling.TaskScheduler;

import java.util.List;

class AppAuditServiceImpl implements EventListener, InitializationListener, AppAuditService
{
	private static final String IPADDRESS_KEY = "ipAddress";
	private static final String USERNAME_KEY = "userName";
	private static final String AUDIT_INTERNAL = "audit.internal";
	private AppEventCoreService appEventCoreService;
	private TaskScheduler taskScheduler;
	private AuditCoreService auditCoreService;
	private String appId;

	public void onAppInitialized()
	{
		AppSubscriptionPackage appSubscriptionPackage = new AppSubscriptionPackage.Builder( appId, this, taskScheduler ).addEvents( EventProcessingType.ASYNCHRONOUS_SERIAL, new String[] {"audit.internal"} ).build();

		appEventCoreService.subscribe( appSubscriptionPackage );
	}

	public void logAudit( AppAuditData auditData )
	{
		EventNotification event = generateEvent( auditData );

		if ( event == null )
		{
			return;
		}

		appEventCoreService.sendAfterTransactionCommits( event );
	}

	public void logNonProxyAudit( AppAuditData auditData )
	{
		EventNotification event = generateEvent( auditData );

		if ( event == null )
		{
			return;
		}

		appEventCoreService.send( event );
	}

	private EventNotification generateEvent( AppAuditData auditData )
	{
		if ( auditData.getUser() == null )
		{
			String userName = CommonAppUtils.getUsernameFromSecurityContext();

			if ( userName == null )
			{
				return null;
			}

			auditData.setUser( userName );
		}

		if ( auditData.getAddress() == null )
		{
			String ipAddress = CommonAppUtils.getRemoteIpAddressFromSecurityContext();
			auditData.setAddress( ipAddress );
		}

		auditData.setAppId( appId );
		return new EventNotification.Builder( "audit.internal" ).value( auditData ).info( "ipAddress", auditData.getAddress() ).info( "userName", auditData.getUser() ).build();
	}

	public void processEvents( List<EventNotification> events )
	{
		for ( EventNotification event : events )
		{
			UserContext context = new UserContext( event.getInfo( "userName" ), event.getInfo( "ipAddress" ) );
			auditCoreService.logAppAudit( ( AppAuditData ) event.getValue(), context );
		}
	}

	public void setAppEventCoreService( AppEventCoreService appEventCoreService )
	{
		this.appEventCoreService = appEventCoreService;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public void setAuditCoreService( AuditCoreService auditCoreService )
	{
		this.auditCoreService = auditCoreService;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}
}
