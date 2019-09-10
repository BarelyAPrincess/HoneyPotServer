package com.marchnetworks.app.core;

import com.marchnetworks.command.api.alarm.AlarmCoreService;
import com.marchnetworks.command.api.app.AppCoreService;
import com.marchnetworks.command.api.audit.AuditCoreService;
import com.marchnetworks.command.api.diagnostics.SystemInfoCoreService;
import com.marchnetworks.command.api.event.AppEventCoreService;
import com.marchnetworks.command.api.execution.trigger.ExecutionTriggerCoreService;
import com.marchnetworks.command.api.extractor.GpsExtractionService;
import com.marchnetworks.command.api.extractor.TransactionExtractionService;
import com.marchnetworks.command.api.id.IdCoreService;
import com.marchnetworks.command.api.license.LicenseCoreService;
import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.api.notification.NotificationCoreService;
import com.marchnetworks.command.api.schedule.ScheduleConsumerService;
import com.marchnetworks.command.api.schedule.ScheduleCoreService;
import com.marchnetworks.command.api.security.DeviceSessionCoreService;
import com.marchnetworks.command.api.security.SecurityTokenCoreService;
import com.marchnetworks.command.api.security.SessionCoreService;
import com.marchnetworks.command.api.serialization.JsonSerializer;
import com.marchnetworks.command.api.simulator.DeviceTestCoreService;
import com.marchnetworks.command.api.topology.ArchiverAssociationCoreService;
import com.marchnetworks.command.api.topology.GenericStorageCoreService;
import com.marchnetworks.command.api.topology.TopologyCoreService;
import com.marchnetworks.command.api.user.UserCoreService;
import com.marchnetworks.command.export.ExporterCoreService;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import javax.annotation.Resource;

public class OsgiManagerImpl implements OsgiManager
{
	private static final Logger LOG = LoggerFactory.getLogger( OsgiManagerImpl.class );

	private SessionCoreService sessionCoreService;

	private TopologyCoreService topologyCoreService;

	private GenericStorageCoreService genericStorageCoreService;

	private AppEventCoreService appEventCoreService;
	private JsonSerializer jsonSerializer;
	private UserCoreService userCoreService;
	private AlarmCoreService alarmCoreService;
	private DeviceSessionCoreService deviceSessionCoreService;
	private SecurityTokenCoreService securityTokenCoreService;
	private AppCoreService appCoreService;
	private LicenseCoreService licenseCoreService;
	private ArchiverAssociationCoreService archiverAssociationCoreService;
	private NotificationCoreService notificationCoreService;
	private ExporterCoreService exporterCoreService;
	private ScheduleCoreService scheduleCoreService;
	private AuditCoreService auditCoreService;
	private ScheduleConsumerService scheduleConsumerService;
	private IdCoreService idCoreService;
	private SystemInfoCoreService systemInfoCoreService;
	private MetricsCoreService metricsCoreService;
	private ExecutionTriggerCoreService executionTriggerCoreService;
	private GpsExtractionService gpsExtractionService;
	private TransactionExtractionService transactionExtractionService;
	private DeviceTestCoreService deviceTestCoreService;
	private ServiceRegistration<SessionCoreService> sessionCoreServiceReg;
	private ServiceRegistration<TopologyCoreService> topologyCoreServiceReg;
	private ServiceRegistration<GenericStorageCoreService> genericStorageCoreServiceReg;
	private ServiceRegistration<AppEventCoreService> appEventCoreServiceReg;
	private ServiceRegistration<JsonSerializer> jsonSerializerReg;
	private ServiceRegistration<UserCoreService> userCoreServiceReg;
	private ServiceRegistration<AlarmCoreService> alarmCoreServiceReg;
	private ServiceRegistration<DeviceSessionCoreService> deviceSessionCoreServiceReg;
	private ServiceRegistration<SecurityTokenCoreService> securityTokenCoreServiceReg;
	private ServiceRegistration<AppCoreService> appCoreServiceReg;
	private ServiceRegistration<LicenseCoreService> licenseCoreServiceReg;
	private ServiceRegistration<ArchiverAssociationCoreService> archiverAssociationCoreServiceReg;
	private ServiceRegistration<NotificationCoreService> notificationCoreServiceReg;
	private ServiceRegistration<ExporterCoreService> exporterCoreServiceReg;
	private ServiceRegistration<ScheduleCoreService> scheduleCoreServiceReg;
	private ServiceRegistration<AuditCoreService> auditCoreServiceReg;
	private ServiceRegistration<ScheduleConsumerService> scheduleConsumerServiceReg;
	private ServiceRegistration<IdCoreService> idCoreServiceReg;
	private ServiceRegistration<SystemInfoCoreService> systemInfoCoreServiceReg;
	private ServiceRegistration<MetricsCoreService> metricsCoreServiceReg;
	private ServiceRegistration<ExecutionTriggerCoreService> executionTriggerCoreServiceReg;
	private ServiceRegistration<GpsExtractionService> gpsExtractionServiceReg;
	private ServiceRegistration<TransactionExtractionService> transactionExtractionServiceReg;
	private ServiceRegistration<DeviceTestCoreService> deviceTestCoreServiceReg;
	private BundleContext context = null;

	public void init()
	{
		Bundle bundle = FrameworkUtil.getBundle( Resource.class );
		BundleContext bundleContext = bundle.getBundleContext();
		Bundle systemBundle = bundleContext.getBundle( 0L );
		context = systemBundle.getBundleContext();

		sessionCoreServiceReg = registerService( SessionCoreService.class, sessionCoreService, null );
		topologyCoreServiceReg = registerService( TopologyCoreService.class, topologyCoreService, null );
		genericStorageCoreServiceReg = registerService( GenericStorageCoreService.class, genericStorageCoreService, null );
		appEventCoreServiceReg = registerService( AppEventCoreService.class, appEventCoreService, null );
		jsonSerializerReg = registerService( JsonSerializer.class, jsonSerializer, null );
		userCoreServiceReg = registerService( UserCoreService.class, userCoreService, null );
		alarmCoreServiceReg = registerService( AlarmCoreService.class, alarmCoreService, null );
		deviceSessionCoreServiceReg = registerService( DeviceSessionCoreService.class, deviceSessionCoreService, null );
		securityTokenCoreServiceReg = registerService( SecurityTokenCoreService.class, securityTokenCoreService, null );
		appCoreServiceReg = registerService( AppCoreService.class, appCoreService, null );
		licenseCoreServiceReg = registerService( LicenseCoreService.class, licenseCoreService, null );
		archiverAssociationCoreServiceReg = registerService( ArchiverAssociationCoreService.class, archiverAssociationCoreService, null );
		notificationCoreServiceReg = registerService( NotificationCoreService.class, notificationCoreService, null );
		exporterCoreServiceReg = registerService( ExporterCoreService.class, exporterCoreService, null );
		scheduleCoreServiceReg = registerService( ScheduleCoreService.class, scheduleCoreService, null );
		auditCoreServiceReg = registerService( AuditCoreService.class, auditCoreService, null );
		scheduleConsumerServiceReg = registerService( ScheduleConsumerService.class, scheduleConsumerService, null );
		idCoreServiceReg = registerService( IdCoreService.class, idCoreService, null );
		systemInfoCoreServiceReg = registerService( SystemInfoCoreService.class, systemInfoCoreService, null );
		metricsCoreServiceReg = registerService( MetricsCoreService.class, metricsCoreService, null );
		executionTriggerCoreServiceReg = registerService( ExecutionTriggerCoreService.class, executionTriggerCoreService, null );
		gpsExtractionServiceReg = registerService( GpsExtractionService.class, gpsExtractionService, null );
		transactionExtractionServiceReg = registerService( TransactionExtractionService.class, transactionExtractionService, null );
		deviceTestCoreServiceReg = registerService( DeviceTestCoreService.class, deviceTestCoreService, null );
	}

	public void destroy()
	{
		unregisterService( sessionCoreServiceReg );
		unregisterService( topologyCoreServiceReg );
		unregisterService( genericStorageCoreServiceReg );
		unregisterService( appEventCoreServiceReg );
		unregisterService( jsonSerializerReg );
		unregisterService( userCoreServiceReg );
		unregisterService( alarmCoreServiceReg );
		unregisterService( deviceSessionCoreServiceReg );
		unregisterService( securityTokenCoreServiceReg );
		unregisterService( appCoreServiceReg );
		unregisterService( licenseCoreServiceReg );
		unregisterService( archiverAssociationCoreServiceReg );
		unregisterService( notificationCoreServiceReg );
		unregisterService( exporterCoreServiceReg );
		unregisterService( scheduleCoreServiceReg );
		unregisterService( auditCoreServiceReg );
		unregisterService( scheduleConsumerServiceReg );
		unregisterService( idCoreServiceReg );
		unregisterService( systemInfoCoreServiceReg );
		unregisterService( metricsCoreServiceReg );
		unregisterService( executionTriggerCoreServiceReg );
		unregisterService( gpsExtractionServiceReg );
		unregisterService( transactionExtractionServiceReg );
		unregisterService( deviceTestCoreServiceReg );
	}

	public <T> ServiceRegistration<T> registerService( Class<T> clazz, T service, Dictionary<String, ?> properties )
	{
		return context.registerService( clazz, service, properties );
	}

	public <T> T getService( Class<T> clazz, String appId )
	{
		T t = null;
		ServiceReference<?>[] serviceReferences = null;

		try
		{
			String filter = "(AppID=" + appId + ")";
			serviceReferences = context.getAllServiceReferences( clazz.getName(), filter );
		}
		catch ( InvalidSyntaxException e )
		{
			LOG.error( "Invalid filter expression." );
			return null;
		}

		if ( serviceReferences == null )
		{
			LOG.info( "There is no " + clazz.getSimpleName() + " registered for App id " + appId );
		}
		else
		{
			t = ( T ) context.getService( serviceReferences[0] );
		}

		return t;
	}

	public <T> List<T> getServices( Class<T> clazz )
	{
		List<T> t = new ArrayList<T>();
		ServiceReference<?>[] serviceReferences = null;
		try
		{
			serviceReferences = context.getAllServiceReferences( clazz.getName(), null );
		}
		catch ( InvalidSyntaxException e )
		{
			LOG.error( "Invalid filter expression." );
			return null;
		}

		if ( serviceReferences == null )
		{
			LOG.debug( "No services with the name {} were registered", clazz.getSimpleName() );
			return t;
		}

		for ( ServiceReference<?> serviceReference : serviceReferences )
		{
			t.add( (T) context.getService( serviceReference ) );
		}

		return t;
	}

	public void unregisterService( ServiceRegistration<?> reg )
	{
		reg.unregister();
	}

	public long installBundle( String filename ) throws BundleException
	{
		File appFile = new File( filename );
		String filePath = appFile.toURI().toString();

		Bundle bundle = context.installBundle( filePath );
		return bundle.getBundleId();
	}

	@Transactional( propagation = Propagation.REQUIRES_NEW )
	public void uninstallBundle( long bundleId ) throws BundleException
	{
		Bundle bundle = context.getBundle( bundleId );
		if ( bundle == null )
		{
			LOG.info( "App with bundleId " + bundleId + " not found when uninstalling" );
			return;
		}
		bundle.uninstall();
	}

	@Transactional( propagation = Propagation.REQUIRES_NEW )
	public void startBundle( long bundleId ) throws BundleException
	{
		Bundle bundle = context.getBundle( bundleId );
		if ( bundle == null )
		{
			LOG.info( "App with bundleId " + bundleId + " not found when starting" );
		}
		else
		{
			bundle.start();
		}
	}

	@Transactional( propagation = Propagation.REQUIRES_NEW )
	public void stopBundle( long bundleId ) throws BundleException
	{
		Bundle bundle = context.getBundle( bundleId );
		if ( bundle == null )
		{
			LOG.info( "App with bundleId " + bundleId + " not found when stopping" );
			return;
		}
		bundle.stop();
	}

	public void setTopologyCoreService( TopologyCoreService topologyCoreService )
	{
		this.topologyCoreService = topologyCoreService;
	}

	public void setGenericStorageCoreService( GenericStorageCoreService genericStorageCoreService )
	{
		this.genericStorageCoreService = genericStorageCoreService;
	}

	public void setSessionCoreService( SessionCoreService sessionCoreService )
	{
		this.sessionCoreService = sessionCoreService;
	}

	public void setAppEventCoreService( AppEventCoreService appEventCoreService )
	{
		this.appEventCoreService = appEventCoreService;
	}

	public void setJsonSerializer( JsonSerializer jsonSerializer )
	{
		this.jsonSerializer = jsonSerializer;
	}

	public void setUserCoreService( UserCoreService userCoreService )
	{
		this.userCoreService = userCoreService;
	}

	public void setAlarmCoreService( AlarmCoreService alarmCoreService )
	{
		this.alarmCoreService = alarmCoreService;
	}

	public void setDeviceSessionCoreService( DeviceSessionCoreService deviceSessionCoreService )
	{
		this.deviceSessionCoreService = deviceSessionCoreService;
	}

	public void setSecurityTokenCoreService( SecurityTokenCoreService securityTokenCoreService )
	{
		this.securityTokenCoreService = securityTokenCoreService;
	}

	public void setAppCoreService( AppCoreService appCoreService )
	{
		this.appCoreService = appCoreService;
	}

	public void setLicenseCoreService( LicenseCoreService licenseCoreService )
	{
		this.licenseCoreService = licenseCoreService;
	}

	public void setArchiverAssociationCoreService( ArchiverAssociationCoreService archiverAssociationCoreService )
	{
		this.archiverAssociationCoreService = archiverAssociationCoreService;
	}

	public void setNotificationCoreService( NotificationCoreService notificationCoreService )
	{
		this.notificationCoreService = notificationCoreService;
	}

	public void setExporterCoreService( ExporterCoreService exporterCoreService )
	{
		this.exporterCoreService = exporterCoreService;
	}

	public void setScheduleCoreService( ScheduleCoreService scheduleCoreService )
	{
		this.scheduleCoreService = scheduleCoreService;
	}

	public void setAuditCoreService( AuditCoreService auditCoreService )
	{
		this.auditCoreService = auditCoreService;
	}

	public void setScheduleConsumerService( ScheduleConsumerService scheduleConsumerService )
	{
		this.scheduleConsumerService = scheduleConsumerService;
	}

	public void setIdCoreService( IdCoreService idCoreService )
	{
		this.idCoreService = idCoreService;
	}

	public void setSystemInfoCoreService( SystemInfoCoreService systemInfoCoreService )
	{
		this.systemInfoCoreService = systemInfoCoreService;
	}

	public MetricsCoreService getMetricsCoreService()
	{
		return metricsCoreService;
	}

	public void setMetricsCoreService( MetricsCoreService metricsCoreService )
	{
		this.metricsCoreService = metricsCoreService;
	}

	public void setExecutionTriggerCoreService( ExecutionTriggerCoreService executionTriggerCoreService )
	{
		this.executionTriggerCoreService = executionTriggerCoreService;
	}

	public void setDeviceTestCoreService( DeviceTestCoreService deviceTestCoreService )
	{
		this.deviceTestCoreService = deviceTestCoreService;
	}

	public void setGpsExtractionService( GpsExtractionService gpsExtractionService )
	{
		this.gpsExtractionService = gpsExtractionService;
	}

	public void setTransactionExtractionService( TransactionExtractionService transactionExtractionService )
	{
		this.transactionExtractionService = transactionExtractionService;
	}
}
