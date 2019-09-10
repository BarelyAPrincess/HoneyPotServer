package com.marchnetworks.command.common.scheduling;

import com.marchnetworks.command.api.metrics.ApiMetricsTypes;
import com.marchnetworks.command.api.metrics.MetricsCoreService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedPoolSerialExecutor
{
	private static final Logger LOG = LoggerFactory.getLogger( FixedPoolSerialExecutor.class );

	private int poolSize;
	private List<SerialTask> queue = new ArrayList();
	private Map<String, HashSet<String>> queuedNonConcurrents = new HashMap();
	private Map<String, SerialTask> executingTasks;
	private ThreadPoolExecutor taskExecutor;
	private MetricsCoreService metricsService;

	public FixedPoolSerialExecutor( int corePoolSize, int keepAliveTime, MetricsCoreService metricsService )
	{
		poolSize = corePoolSize;
		taskExecutor = new ThreadPoolExecutor( corePoolSize, corePoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue() );
		taskExecutor.allowCoreThreadTimeOut( true );
		executingTasks = new HashMap( corePoolSize );
		this.metricsService = metricsService;
	}

	public synchronized void shutdownNow()
	{
		taskExecutor.shutdownNow();
		queue.clear();
		executingTasks.clear();
	}

	public void submit( String id, Runnable runnable )
	{
		long start = System.nanoTime();
		int queueSize = -1;
		synchronized ( this )
		{
			if ( ( runnable instanceof NonConcurrentRunnable ) )
			{
				NonConcurrentRunnable nonConcurrentRunnable = ( NonConcurrentRunnable ) runnable;

				if ( isCurrentlyExecutingOrQueued( id, nonConcurrentRunnable ) )
				{
					LOG.debug( "Similar Task {} scheduled to run or running. Not scheduling new task. ", id );
					return;
				}
			}

			SerialTask task = new SerialTask( id, runnable, this );

			if ( ( executingTasks.size() >= poolSize ) || ( isCurrentlyExecuting( task ) ) )
			{
				queue.add( task );
				if ( ( runnable instanceof NonConcurrentRunnable ) )
				{
					NonConcurrentRunnable nonConcurrentRunnable = ( NonConcurrentRunnable ) runnable;
					HashSet<String> nonConcurrents = ( HashSet ) queuedNonConcurrents.get( id );
					if ( nonConcurrents == null )
					{
						nonConcurrents = new HashSet( 1 );
						queuedNonConcurrents.put( id, nonConcurrents );
					}
					nonConcurrents.add( nonConcurrentRunnable.getTaskId() );
				}
				queueSize = queue.size();
			}
			else
			{
				executingTasks.put( task.getId(), task );
				taskExecutor.execute( task );
			}
		}
		metricsService.addBucketMinMaxAvg( ApiMetricsTypes.SERIAL_TASK.getName(), "Submit", ( System.nanoTime() - start ) / 1000L );
		if ( queueSize != -1 )
		{
			metricsService.addCurrentMaxAvg( ApiMetricsTypes.SERIAL_TASK_QUEUE.getName(), queueSize );
		}
	}

	public void notifyTaskComplete( SerialTask task, long runTime )
	{
		long start = System.nanoTime();
		Iterator<SerialTask> iterator;
		synchronized ( this )
		{
			executingTasks.remove( task.getId() );

			for ( iterator = queue.iterator(); iterator.hasNext(); )
			{
				SerialTask queuedTask = ( SerialTask ) iterator.next();

				if ( !isCurrentlyExecuting( queuedTask ) )
				{
					iterator.remove();
					Runnable runnable = queuedTask.getRunnable();
					if ( ( runnable instanceof NonConcurrentRunnable ) )
					{
						NonConcurrentRunnable nonConcurrentRunnable = ( NonConcurrentRunnable ) runnable;
						HashSet<String> nonConcurrents = ( HashSet ) queuedNonConcurrents.get( queuedTask.getId() );
						nonConcurrents.remove( nonConcurrentRunnable.getTaskId() );
					}
					executingTasks.put( queuedTask.getId(), queuedTask );
					taskExecutor.execute( queuedTask );

					if ( executingTasks.size() >= poolSize )
					{
						break;
					}
				}
			}
		}
		metricsService.addBucketMinMaxAvg( ApiMetricsTypes.SERIAL_TASK.getName(), "Notify", ( System.nanoTime() - start ) / 1000L );
		metricsService.addBucketMinMaxAvg( ApiMetricsTypes.DEVICE_TASKS.getName(), task.getName(), runTime );
	}

	private boolean isCurrentlyExecuting( SerialTask task )
	{
		return executingTasks.containsKey( task.getId() );
	}

	private boolean isCurrentlyExecutingOrQueued( String id, NonConcurrentRunnable task )
	{
		String taskId = task.getTaskId();

		SerialTask currentTask = ( SerialTask ) executingTasks.get( id );
		if ( ( currentTask != null ) && ( ( currentTask.getRunnable() instanceof NonConcurrentRunnable ) ) && ( ( ( NonConcurrentRunnable ) currentTask.getRunnable() ).getTaskId().equals( taskId ) ) )
		{
			return true;
		}

		HashSet<String> nonConcurrents = ( HashSet ) queuedNonConcurrents.get( id );
		if ( nonConcurrents != null )
		{
			return nonConcurrents.contains( taskId );
		}
		return false;
	}
}
