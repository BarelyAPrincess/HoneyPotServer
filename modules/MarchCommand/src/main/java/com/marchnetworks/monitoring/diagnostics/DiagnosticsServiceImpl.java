package com.marchnetworks.monitoring.diagnostics;

import com.marchnetworks.command.api.alert.AlertDefinitionEnum;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.common.diagnostics.DiagnosticError;
import com.marchnetworks.common.diagnostics.DiagnosticResult;
import com.marchnetworks.common.diagnostics.DiagnosticResultType;
import com.marchnetworks.common.diagnostics.WatchdogNotification;
import com.marchnetworks.common.diagnostics.memory.MemoryPool;
import com.marchnetworks.common.diagnostics.memory.MemoryPoolListener;
import com.marchnetworks.common.diagnostics.memory.MemoryPoolThreshold;
import com.marchnetworks.common.diagnostics.memory.MemoryPoolWatcher;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.health.input.AlertInput;
import com.marchnetworks.health.input.ServerAlertInput;
import com.marchnetworks.health.service.HealthServiceIF;
import com.marchnetworks.management.instrumentation.events.DatabaseStatusNotification;
import com.marchnetworks.server.event.EventRegistry;
import com.sun.management.OperatingSystemMXBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiagnosticsServiceImpl implements DiagnosticsServiceIF, MemoryPoolListener, InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( DiagnosticsServiceImpl.class );

	private MemoryPoolWatcher memoryPoolWatcher;
	private double[] memoryThresholds;
	private double finalMemoryThreshold;
	private boolean databaseConnected = true;
	private boolean currentlyRestarting = false;

	private HealthServiceIF healthService;
	private EventRegistry eventRegistry;
	private Map<DiagnosticError, DiagnosticResult> diagnosticNotifications = new HashMap<DiagnosticError, DiagnosticResult>();
	private List<MemoryPoolThreshold> thresholds = new ArrayList<MemoryPoolThreshold>();

	public void onAppInitialized()
	{
		for ( double value : memoryThresholds )
		{
			thresholds.add( new MemoryPoolThreshold( value ) );
		}

		memoryPoolWatcher.addListener( this, MemoryPool.TENURED, memoryThresholds );

		checkStartupMemory();
	}

	public DiagnosticResult checkFailures()
	{
		for ( DiagnosticError error : DiagnosticError.values() )
		{
			DiagnosticResult result = checkFailure( error );

			if ( result != DiagnosticResult.RESULT_OK )
			{
				if ( result.getType() == DiagnosticResultType.FAILURE )
				{
					synchronized ( this )
					{
						currentlyRestarting = true;
						diagnosticNotifications.put( error, null );
					}
				}
				LOG.info( "Reporting failure to watchdog: " + result.getError().getName() );
				return result;
			}
		}
		return DiagnosticResult.RESULT_OK;
	}

	private DiagnosticResult checkFailure( DiagnosticError error )
	{
		DiagnosticResult notified = ( DiagnosticResult ) diagnosticNotifications.get( error );
		if ( notified != null )
		{
			return notified;
		}

		if ( error == DiagnosticError.APPLICATION_DEADLOCK )
			return checkApplicationDeadlock();
		if ( error == DiagnosticError.DATABASE )
			return checkDatabaseFailure();
		if ( error == DiagnosticError.LOW_MEMORY )
		{
			return checkLowMemoryWarning();
		}
		return DiagnosticResult.RESULT_OK;
	}

	public synchronized void notifyFailure( DiagnosticError error, String message )
	{
		if ( !currentlyRestarting )
		{
			LOG.error( "Diagnostics service was notified of a failure: " + error.getName() );
			DiagnosticResult result = new DiagnosticResult( DiagnosticResultType.FAILURE, error, message );

			diagnosticNotifications.put( error, result );

			if ( error == DiagnosticError.DATABASE )
			{
				notifyDatabaseStatus( false );
			}
		}
		else
		{
			LOG.error( "Diagnostics service was notified of a failure " + error.getName() + " while restarting, ignoring." );
		}
	}

	public void notifyResolved( DiagnosticError error )
	{
		if ( error == DiagnosticError.DATABASE )
		{
			notifyDatabaseStatus( true );
			diagnosticNotifications.put( DiagnosticError.DATABASE, DiagnosticResult.RESULT_OK );
		}
	}

	public void notifyRestartComplete( WatchdogNotification notification )
	{
		currentlyRestarting = false;

		LOG.info( "Watchdog Notification: " + notification );

		if ( ( !notification.getProcessRestarted() ) && ( notification.getDependenciesRestarted() == 0 ) )
		{
			LOG.info( "Restart notification received but no restarts were performed by the watchdog, not creating health alert" );
			return;
		}

		try
		{
			String sourceId = "CES";

			if ( notification.getReason().equals( DiagnosticError.DATABASE.getCode() ) )
			{
				sourceId = "Database";
			}

			AlertInput alert = new ServerAlertInput( AlertDefinitionEnum.WATCHDOG_RESTART, sourceId, notification.getTimestamp() * 1000L, "", notification.getReason(), true );

			healthService.processHealthAlert( alert );
		}
		catch ( Exception e )
		{
			LOG.error( "Error while trying to create watchdog health alert", e );
		}
	}

	private void issueLowMemoryHealthAlert( AlertDefinitionEnum alertDefinition, String sourceId )
	{
		try
		{
			LOG.warn( "Issuing low memory health alert: " + alertDefinition.getPath() + " " + sourceId );

			AlertInput alert = new ServerAlertInput( alertDefinition, sourceId, System.currentTimeMillis(), "", "", true );

			healthService.processHealthAlert( alert );
		}
		catch ( Exception e )
		{
			LOG.error( "Error while trying to issue low memory health alert: " + alertDefinition, e );
		}
	}

	private void clearLowMemoryHealthAlert( AlertDefinitionEnum alertDefinition, String sourceId )
	{
		try
		{
			AlertInput alert = new ServerAlertInput( alertDefinition, sourceId, System.currentTimeMillis(), "", "", false );

			healthService.processHealthAlert( alert );
		}
		catch ( Exception e )
		{
			LOG.error( "Error while trying to clear low memory health alert: " + alertDefinition, e );
		}
	}

	private DiagnosticResult checkApplicationDeadlock()
	{
		ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
		long[] ids = tmx.findDeadlockedThreads();
		if ( ids != null )
		{
			ThreadInfo[] infos = tmx.getThreadInfo( ids, true, true );
			LOG.error( "Diagnostics detected deadlock, printing stack traces of deadlocked threads:" );
			for ( ThreadInfo ti : infos )
			{
				LOG.info( ti.toString() );
			}
			return new DiagnosticResult( DiagnosticResultType.FAILURE, DiagnosticError.APPLICATION_DEADLOCK, "Server reported deadlocked state, check the server logs for thread stack traces" );
		}
		return DiagnosticResult.RESULT_OK;
	}

	private DiagnosticResult checkDatabaseFailure()
	{
		return DiagnosticResult.RESULT_OK;
	}

	private DiagnosticResult checkLowMemoryWarning()
	{
		DiagnosticResult result = DiagnosticResult.RESULT_OK;

		for ( int i = thresholds.size() - 1; i >= 0; i-- )
		{
			MemoryPoolThreshold t = ( MemoryPoolThreshold ) thresholds.get( i );
			if ( t.isTriggered() )
			{
				String message = "Tenured pool memory has exceeded " + ( int ) ( t.getThreshold() * 100.0D ) + " percent.";
				result = new DiagnosticResult( DiagnosticResultType.WARNING, DiagnosticError.LOW_MEMORY, message );
				resetMemoryThresholds();
				break;
			}
		}
		memoryPoolWatcher.resetThresholdsIfDisabled( MemoryPool.TENURED );
		return result;
	}

	public void handleMemoryThresholdExceeded( MemoryPool memPool, double memThreshold )
	{
		LOG.warn( "Diagnostics detected low memory: threshold for memory pool " + memPool + " exceeded " + ( int ) ( memThreshold * 100.0D ) );

		for ( MemoryPoolThreshold threshold : thresholds )
		{
			if ( memThreshold == threshold.getThreshold() )
			{
				threshold.setTriggered( true );
				if ( finalMemoryThreshold <= memThreshold )
				{
					issueLowMemoryHealthAlert( AlertDefinitionEnum.SERVER_MEMORY, "runtime" );
				}
			}
		}
	}

	private void checkStartupMemory()
	{
		OperatingSystemMXBean osMxBean = ( OperatingSystemMXBean ) ManagementFactory.getOperatingSystemMXBean();
		long systemFreeMemory = osMxBean.getFreePhysicalMemorySize();
		LOG.info( "Operating System Memory: Total Memory: " + osMxBean.getTotalPhysicalMemorySize() + "  bytes, Free Memory: " + systemFreeMemory + "  bytes" );

		long maxHeapMemory = 0L;
		long minHeapMemory = 0L;
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		List<String> arguments = runtimeMxBean.getInputArguments();
		for ( String arg : arguments )
		{
			if ( ( StringUtils.hasText( arg ) ) && ( ( arg.contains( "-Xms" ) ) || ( arg.contains( "-Xmx" ) ) ) )
			{
				if ( arg.contains( "-Xms" ) )
				{
					arg = arg.replace( "-Xms", "" );
					arg = arg.replace( "m", "" );
					try
					{
						minHeapMemory = Long.valueOf( arg ).longValue() * 1024L * 1024L;
					}
					catch ( NumberFormatException e )
					{
						LOG.info( "Error trying to figure out min heap memory.", e );
					}
				}
				else
				{
					arg = arg.replace( "-Xmx", "" );
					arg = arg.replace( "m", "" );
					try
					{
						maxHeapMemory = Long.valueOf( arg ).longValue() * 1024L * 1024L;
					}
					catch ( NumberFormatException e )
					{
						LOG.info( "Error trying to figure out max heap memory.", e );
					}
				}
			}
		}

		long maxPermMemory = 0L;
		for ( MemoryPoolMXBean mx : ManagementFactory.getMemoryPoolMXBeans() )
		{
			if ( "PS Perm Gen".equals( mx.getName() ) )
			{
				maxPermMemory = mx.getUsage().getMax();
			}
		}

		LOG.info( "Memory Settings: Min Heap Memory: " + minHeapMemory + " bytes, Max Heap Memory: " + maxHeapMemory + " bytes, Max Perm Memory: " + maxPermMemory + " bytes" );

		if ( maxHeapMemory <= minHeapMemory + maxPermMemory )
		{
			issueLowMemoryHealthAlert( AlertDefinitionEnum.SERVER_MEMORY, "startup" );
		}
		else
		{
			clearLowMemoryHealthAlert( AlertDefinitionEnum.SERVER_MEMORY, "startup" );
		}
	}

	private void resetMemoryThresholds()
	{
		for ( MemoryPoolThreshold t : thresholds )
		{
			t.setTriggered( false );
		}
	}

	private void notifyDatabaseStatus( boolean connected )
	{
		if ( ( databaseConnected ) && ( !connected ) )
		{
			LOG.error( "Database connection has been lost, sending event" );
			databaseConnected = false;
			DatabaseStatusNotification notification = new DatabaseStatusNotification( connected );
			getEventRegistry().send( notification );

		}
		else if ( ( !databaseConnected ) && ( connected ) )
		{
			LOG.info( "Database connection has been restored, sending event" );
			databaseConnected = true;
			DatabaseStatusNotification notification = new DatabaseStatusNotification( connected );
			getEventRegistry().send( notification );
		}
	}

	public void setMemoryPoolWatcher( MemoryPoolWatcher memoryPoolWatcher )
	{
		this.memoryPoolWatcher = memoryPoolWatcher;
	}

	public MemoryPoolWatcher getMemoryPoolWatcher()
	{
		return memoryPoolWatcher;
	}

	public void setHealthService( HealthServiceIF healthService )
	{
		this.healthService = healthService;
	}

	public void setMemoryThresholds( double[] memoryThresholds )
	{
		this.memoryThresholds = memoryThresholds;
	}

	public void setFinalMemoryThreshold( double finalMemoryThreshold )
	{
		this.finalMemoryThreshold = finalMemoryThreshold;
	}

	public EventRegistry getEventRegistry()
	{
		if ( eventRegistry == null )
		{
			eventRegistry = ( ( EventRegistry ) ApplicationContextSupport.getBean( "eventRegistry" ) );
		}
		return eventRegistry;
	}
}

