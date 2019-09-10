package com.marchnetworks.command.common.scheduling;

import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.common.scheduling.task.Task;
import com.marchnetworks.command.common.scheduling.task.TaskAsync;
import com.marchnetworks.command.common.scheduling.task.TaskFixedPool;
import com.marchnetworks.command.common.scheduling.task.TaskFixedPoolSerial;
import com.marchnetworks.command.common.scheduling.task.TaskSerial;
import com.marchnetworks.command.common.transaction.BaseTransactionalBeanInterceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskScheduler
{
	public static final String GENERIC_EXECUTOR = "generic";
	private static final Logger LOG = LoggerFactory.getLogger( TaskScheduler.class );

	private ScheduledThreadPoolExecutor scheduledTaskExecutor;

	private ThreadPoolExecutor taskExecutor;
	private Map<String, ExecutorService> serialTaskExecutorsMap = Collections.synchronizedMap( new HashMap() );

	private Map<String, ExecutorService> parallelTaskExecutorsMap = Collections.synchronizedMap( new HashMap() );

	private Map<String, ScheduledThreadPoolExecutor> scheduledExecutorsMap = Collections.synchronizedMap( new HashMap() );

	private FixedPoolSerialExecutor fixedPoolSerialExecutor;

	private BaseTransactionalBeanInterceptor interceptor;
	private MetricsCoreService metricsService;
	private int corePoolSize;
	private int scheduledCorePoolSize;
	private int fixedPoolSerialSize;
	private int keepAliveTime;
	private int parallelKeepAliveTime;

	public void init()
	{
		taskExecutor = new ThreadPoolExecutor( corePoolSize, corePoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue() );

		scheduledTaskExecutor = new ScheduledThreadPoolExecutor( scheduledCorePoolSize );
		scheduledTaskExecutor.setKeepAliveTime( keepAliveTime, TimeUnit.SECONDS );
		scheduledTaskExecutor.allowCoreThreadTimeOut( true );

		serialTaskExecutorsMap.put( "generic", Executors.newSingleThreadExecutor() );

		if ( fixedPoolSerialSize > 0 )
		{
			fixedPoolSerialExecutor = new FixedPoolSerialExecutor( fixedPoolSerialSize, keepAliveTime, metricsService );
		}
	}

	public void destroy()
	{
		LOG.info( "Task scheduler is going to shutdown." );
		taskExecutor.shutdownNow();
		LOG.debug( "Task Executor has been shutdown." );

		scheduledTaskExecutor.shutdownNow();
		LOG.debug( "Scheduled Task Executor has been shutdown." );

		for ( ExecutorService deviceExecutor : serialTaskExecutorsMap.values() )
		{
			deviceExecutor.shutdownNow();
		}
		LOG.debug( "Serial Task Executors have been shutdown." );

		for ( ExecutorService parallelExecutor : parallelTaskExecutorsMap.values() )
		{
			parallelExecutor.shutdownNow();
		}

		for ( ExecutorService scheduledExecutor : scheduledExecutorsMap.values() )
		{
			scheduledExecutor.shutdownNow();
		}

		if ( fixedPoolSerialExecutor != null )
		{
			fixedPoolSerialExecutor.shutdownNow();
		}
		LOG.debug( "Parallel Task Executors have been shutdown." );
	}

	public void executeSerial( Runnable task, String executorId )
	{
		ExecutorService serialExecutor = null;

		if ( executorId != null )
		{
			serialExecutor = ( ExecutorService ) serialTaskExecutorsMap.get( executorId );
			if ( serialExecutor == null )
			{
				serialExecutor = setupSerialExecutor( executorId );
			}
		}
		else
		{
			serialExecutor = ( ExecutorService ) serialTaskExecutorsMap.get( "generic" );
		}

		serialExecutor.execute( task );
	}

	public void executeFixedPoolSerial( Runnable task, String executorId )
	{
		fixedPoolSerialExecutor.submit( executorId, task );
	}

	public void executeSerial( Runnable task )
	{
		executeSerial( task, "generic" );
	}

	public void executeFixedPool( Runnable task, String executorId, int poolSize, int keepAliveTime )
	{
		ExecutorService parallelExecutor = null;

		parallelExecutor = ( ExecutorService ) parallelTaskExecutorsMap.get( executorId );
		if ( parallelExecutor == null )
		{
			parallelExecutor = setupParallelExecutor( executorId, poolSize, keepAliveTime );
		}

		parallelExecutor.execute( task );
	}

	public void executeFixedPool( Runnable task, String executorId, int poolSize )
	{
		executeFixedPool( task, executorId, poolSize, parallelKeepAliveTime );
	}

	public void executeNow( Runnable task )
	{
		taskExecutor.execute( task );
	}

	public ScheduledFuture<?> schedule( Runnable task, long delay, TimeUnit timeUnit )
	{
		if ( delay == 0L )
		{
			executeNow( task );
			return null;
		}
		return scheduledTaskExecutor.schedule( task, delay, timeUnit );
	}

	public boolean cancelSchedule( ScheduledFuture<?> scheduledTask )
	{
		boolean cancelled = true;
		if ( scheduledTask != null )
		{
			cancelled = scheduledTask.cancel( true );
			scheduledTaskExecutor.purge();
		}
		return cancelled;
	}

	public ScheduledFuture<?> scheduleWithFixedDelay( Runnable task, long initialDelay, long delay, TimeUnit timeUnit )
	{
		LOG.debug( "Scheduling delay task {}", task.getClass().getSimpleName() );
		return scheduledTaskExecutor.scheduleWithFixedDelay( task, initialDelay, delay, timeUnit );
	}

	public ScheduledFuture<?> scheduleAtFixedRate( Runnable task, long initialDelay, long period, TimeUnit unit )
	{
		LOG.debug( "Scheduling periodic task {}", task.getClass().getSimpleName() );
		return scheduledTaskExecutor.scheduleAtFixedRate( task, initialDelay, period, unit );
	}

	public ScheduledFuture<?> scheduleFixedPool( Runnable task, String executorId, int poolSize, long delay, TimeUnit timeUnit, int keepAliveTime )
	{
		ScheduledExecutorService scheduledParallelExecutor = ( ScheduledExecutorService ) scheduledExecutorsMap.get( executorId );
		if ( scheduledParallelExecutor == null )
		{
			scheduledParallelExecutor = setupScheduledExecutor( executorId, poolSize, keepAliveTime );
		}
		return scheduledParallelExecutor.schedule( task, delay, timeUnit );
	}

	public ScheduledFuture<?> scheduleFixedPoolAtFixedRate( Runnable task, String executorId, int poolSize, long initialDelay, long period, TimeUnit unit )
	{
		ScheduledExecutorService scheduledFixedRateExecutor = ( ScheduledExecutorService ) scheduledExecutorsMap.get( executorId );
		if ( scheduledFixedRateExecutor == null )
		{
			scheduledFixedRateExecutor = setupScheduledExecutor( executorId, poolSize, keepAliveTime );
		}
		return scheduledFixedRateExecutor.scheduleAtFixedRate( task, initialDelay, period, unit );
	}

	public boolean cancelFixedPoolSchedule( String executorId, ScheduledFuture<?> scheduledTask )
	{
		boolean cancelled = false;
		ScheduledThreadPoolExecutor scheduledParallelExecutor = ( ScheduledThreadPoolExecutor ) scheduledExecutorsMap.get( executorId );
		if ( ( scheduledParallelExecutor != null ) && ( scheduledTask != null ) )
		{
			cancelled = scheduledTask.cancel( true );
			scheduledParallelExecutor.purge();
		}
		return cancelled;
	}

	public int cancelFixedPool( String executorId )
	{
		ThreadPoolExecutor parallelExecutor = ( ThreadPoolExecutor ) parallelTaskExecutorsMap.get( executorId );
		if ( parallelExecutor != null )
		{
			parallelExecutor.purge();
			int size = parallelExecutor.getQueue().size();
			parallelExecutor.getQueue().clear();
			return size;
		}

		return 0;
	}

	public void executeAfterTransactionCommits( Task task )
	{
		interceptor.executeAfterTransactionCommits( task );
	}

	public void executeTasks( List<Task> tasksToExecute )
	{
		for ( Task task : tasksToExecute )
		{
			if ( ( task instanceof TaskFixedPoolSerial ) )
			{
				TaskSerial taskFixedPoolSerial = ( TaskSerial ) task;
				executeFixedPoolSerial( taskFixedPoolSerial.getTask(), taskFixedPoolSerial.getExecutorId() );
			}
			else if ( ( task instanceof TaskSerial ) )
			{
				TaskSerial serialTask = ( TaskSerial ) task;
				executeSerial( serialTask.getTask(), serialTask.getExecutorId() );
			}
			else if ( ( task instanceof TaskAsync ) )
			{
				executeNow( task.getTask() );
			}
			else if ( ( task instanceof TaskFixedPool ) )
			{
				TaskFixedPool taskFixedPool = ( TaskFixedPool ) task;
				executeFixedPool( taskFixedPool.getTask(), taskFixedPool.getExecutorId(), taskFixedPool.getPoolSize(), taskFixedPool.getKeepAliveTime() );
			}
		}
	}

	private ExecutorService setupSerialExecutor( String executorId )
	{
		ThreadPoolExecutor serialExecutor = new ThreadPoolExecutor( 1, 1, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue() );
		serialExecutor.setKeepAliveTime( keepAliveTime, TimeUnit.SECONDS );
		serialExecutor.allowCoreThreadTimeOut( true );

		LOG.debug( " Executor {} created with Id {} ", new Object[] {serialExecutor.toString(), executorId} );

		serialTaskExecutorsMap.put( executorId, serialExecutor );
		return serialExecutor;
	}

	private ExecutorService setupParallelExecutor( String executorId, int poolSize, int keepAliveTime )
	{
		ThreadPoolExecutor parallelExecutor = new ThreadPoolExecutor( poolSize, poolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue() );
		parallelExecutor.allowCoreThreadTimeOut( true );

		LOG.debug( " Executor {} created with Id {} Poolsize {}", new Object[] {parallelExecutor.toString(), executorId, Integer.valueOf( poolSize )} );

		parallelTaskExecutorsMap.put( executorId, parallelExecutor );
		return parallelExecutor;
	}

	private ScheduledExecutorService setupScheduledExecutor( String executorId, int poolSize, int keepAliveTime )
	{
		ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor( poolSize );
		scheduledExecutor.setKeepAliveTime( keepAliveTime, TimeUnit.SECONDS );
		scheduledExecutor.allowCoreThreadTimeOut( true );

		scheduledExecutorsMap.put( executorId, scheduledExecutor );

		return scheduledExecutor;
	}

	public void setKeepAliveTime( int keepAliveTime )
	{
		this.keepAliveTime = keepAliveTime;
	}

	public int getScheduledCorePoolSize()
	{
		return scheduledCorePoolSize;
	}

	public void setScheduledCorePoolSize( int scheduledCorePoolSize )
	{
		this.scheduledCorePoolSize = scheduledCorePoolSize;
	}

	public int getParallelKeepAliveTime()
	{
		return parallelKeepAliveTime;
	}

	public void setParallelKeepAliveTime( int parallelKeepAliveTime )
	{
		this.parallelKeepAliveTime = parallelKeepAliveTime;
	}

	public void setCorePoolSize( int corePoolSize )
	{
		this.corePoolSize = corePoolSize;
	}

	public void setFixedPoolSerialSize( int fixedPoolSerialSize )
	{
		this.fixedPoolSerialSize = fixedPoolSerialSize;
	}

	public void setInterceptor( BaseTransactionalBeanInterceptor interceptor )
	{
		this.interceptor = interceptor;
	}

	public void setMetricsService( MetricsCoreService metricsService )
	{
		this.metricsService = metricsService;
	}
}
