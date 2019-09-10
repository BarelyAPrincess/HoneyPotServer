package com.marchnetworks.command.common.transaction;

import com.marchnetworks.command.api.metrics.ApiMetricsTypes;
import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.scheduling.task.Task;
import com.marchnetworks.command.common.topology.ExpectedException;

import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTransactionalBeanInterceptor implements MethodInterceptor
{
	protected static final Logger LOG = LoggerFactory.getLogger( BaseTransactionalBeanInterceptor.class );

	protected int maxRetries = 5;
	protected int waitingTimeForRetry = 50;
	protected TransactionMethodInvocator newTransactionInvocator;
	protected TransactionMethodInvocator continueTransactionInvocator;
	private TaskScheduler taskScheduler;
	private MetricsCoreService metricsService;

	public Object invoke( MethodInvocation invocation ) throws Throwable
	{
		String methodName = invocation.getMethod().getName();
		String className = invocation.getMethod().getDeclaringClass().getSimpleName();
		String name = className + "." + methodName;
		LOG.debug( "Executing {}.", invocation );

		if ( !invocation.getMethod().getDeclaringClass().isInterface() )
		{
			LOG.debug( "{} is not an interface method, not intercepting", name );
			return invocation.proceed();
		}

		TransactionContext<?> context = getTransactionContext();
		context.startTransaction();

		Object ret = null;
		boolean invocationSuccessful = false;
		try
		{
			for ( ; ; )
			{
				long start = 0L;
				try
				{
					if ( context.isRootLevelTransaction() )
					{
						start = System.currentTimeMillis();
						ret = newTransactionInvocator.proceed( invocation );

						if ( context.getNumberOfRetries() > 0 )
						{
							LOG.info( "Retried invocation successful." );
						}

						invocationSuccessful = true;
						metricsService.addRetryActionSuccess( ApiMetricsTypes.TRANSACTION.getName(), name, System.currentTimeMillis() - start );

						onTransactionSuccess();

						List<Task> tasks = context.getTasksToExecute();
						if ( !tasks.isEmpty() )
						{
							taskScheduler.executeTasks( tasks );
						}
					}
					else
					{
						ret = continueTransactionInvocator.proceed( invocation );
					}

				}
				catch ( Throwable ex )
				{
					if ( ( !context.isRootLevelTransaction() ) || ( invocationSuccessful ) )
					{
						throw ex;
					}
					context.clear();

					if ( !( ex instanceof ExpectedException ) )
					{
						metricsService.addRetryActionFailure( ApiMetricsTypes.TRANSACTION.getName(), name, System.currentTimeMillis() - start );
					}

					if ( ( ex instanceof RuntimeException ) )
					{

						Exception translated = TransactionExceptionTranslator.translateException( ( RuntimeException ) ex );
						if ( ( translated instanceof DatabaseRetryException ) )
						{
							context.manageLastException( ( RuntimeException ) ex );
							if ( context.shouldLog() )
							{
								LOG.warn( "Retrying transaction {} for exception: {}", new Object[] {name, translated.getMessage()} );
							}

							if ( context.getNumberOfRetries() < maxRetries )
							{
								context.incrementNumberOfRetries();

								metricsService.addRetryAction( ApiMetricsTypes.TRANSACTION.getName(), context.getNumberOfRetries() );

								Thread.sleep( waitingTimeForRetry );
								LOG.warn( "Retry for the {} time.", Integer.valueOf( context.getNumberOfRetries() ) );
							}
							else
							{
								LOG.error( "Retried " + context.getNumberOfRetries() + " times. Task failed.", translated );
								throw translated;
							}
						}
						else
						{
							if ( ( translated instanceof DatabaseFailureException ) )
							{
								metricsService.addCounter( ApiMetricsTypes.DATABASE_FAILURE.getName() );
								LOG.error( "Transaction failed with exception causing database failure", translated );
								onTransactionDatabaseFailure();
								throw translated;
							}
							if ( ( translated instanceof ExpectedException ) )
							{
								LOG.error( name + ": " + ex.getMessage() );
								throw ex;
							}

							LOG.error( "Transaction failed with unrecoverable runtime exception", ex );
							throw ex;
						}
					}
					else
					{
						if ( ( ex instanceof ExpectedException ) )
						{
							LOG.error( name + ": " + ex.getMessage() );
							throw ex;
						}
						LOG.error( "Transaction failed with unrecoverable exception", ex );

						throw ex;
					}
				}
			}
		}
		finally
		{
			if ( context.isRootLevelTransaction() )
			{
				if ( !invocationSuccessful )
				{
					removeTransactionContext();
				}
			}
			else
			{
				context.endTransaction();
			}
		}

		// return ret;
	}

	public void executeAfterTransactionCommits( Task task )
	{
		TransactionContext<?> context = getTransactionContext();
		if ( !context.isTransactionStarted() )
		{
			LOG.warn( "No transaction found, ignoring task to execute after commit {}", task );
			removeTransactionContext();
			return;
		}
		context.addTask( task );
	}

	public abstract void onTransactionSuccess();

	public abstract void onTransactionDatabaseFailure();

	public abstract TransactionContext<?> getTransactionContext();

	public abstract void removeTransactionContext();

	public int getMaxRetries()
	{
		return maxRetries;
	}

	public void setMaxRetries( int maxRetries )
	{
		this.maxRetries = maxRetries;
	}

	public int getWaitingTimeForRetry()
	{
		return waitingTimeForRetry;
	}

	public void setWaitingTimeForRetry( int waitingTimeForRetry )
	{
		this.waitingTimeForRetry = waitingTimeForRetry;
	}

	public void setNewTransactionInvocator( TransactionMethodInvocator newTransactionInvocator )
	{
		this.newTransactionInvocator = newTransactionInvocator;
	}

	public void setContinueTransactionInvocator( TransactionMethodInvocator continueTransactionInvocator )
	{
		this.continueTransactionInvocator = continueTransactionInvocator;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public void setMetricsService( MetricsCoreService metricsService )
	{
		this.metricsService = metricsService;
	}
}
