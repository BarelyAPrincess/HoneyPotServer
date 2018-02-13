/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import io.amelia.foundation.parcel.ParcelCarrier;
import io.amelia.foundation.parcel.ParcelReceiver;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.support.Objs;

public abstract class AsyncTask<Params, Progress, Result>
{
	private static final Logger L = LogBuilder.get( AsyncTask.class );
	private static final int MESSAGE_POST_PROGRESS = 0x2;
	private static final int MESSAGE_POST_RESULT = 0x1;
	private static ParcelReceiver receiver;

	private static ParcelReceiver getReceiver()
	{
		synchronized ( AsyncTask.class )
		{
			if ( receiver == null )
				receiver = new ParcelReceiver()
				{
					@Override
					public void handleParcel( ParcelCarrier msg )
					{
						AsyncTaskResult<?> result = ( AsyncTaskResult<?> ) msg.getPayloadObject();
						switch ( msg.getCode() )
						{
							case MESSAGE_POST_RESULT:
								// There is only one activeState
								result.mTask.finish( result.mData[0] );
								break;
							case MESSAGE_POST_PROGRESS:
								result.mTask.onProgressUpdate( result.mData );
								break;
						}
					}
				};

			return receiver;
		}
	}

	private final AtomicBoolean mCancelled = new AtomicBoolean();
	private final FutureTask<Result> mFuture;
	private final AtomicBoolean mTaskInvoked = new AtomicBoolean();
	private final WorkerCallable<Params, Result> mWorker;
	private volatile Status mStatus = Status.PENDING;

	/**
	 * Creates a new asynchronous task. This constructor must be invoked on the UI thread.
	 */
	public AsyncTask()
	{
		mWorker = new WorkerCallable<Params, Result>()
		{
			public Result call() throws Exception
			{
				mTaskInvoked.set( true );
				Result result = null;
				try
				{
					Thread.currentThread().setPriority( Thread.MAX_PRIORITY );
					//noinspection unchecked
					result = doInBackground( mParams );
					// Binder.flushPendingCommands();
				}
				catch ( Throwable tr )
				{
					mCancelled.set( true );
					throw tr;
				}
				finally
				{
					postResult( result );
				}
				return result;
			}
		};

		mFuture = new FutureTask<Result>( mWorker )
		{
			@Override
			protected void done()
			{
				try
				{
					postResultIfNotInvoked( get() );
				}
				catch ( InterruptedException e )
				{
					L.warning( e );
				}
				catch ( ExecutionException e )
				{
					throw new RuntimeException( "An error occurred while executing doInBackground()", e.getCause() );
				}
				catch ( CancellationException e )
				{
					postResultIfNotInvoked( null );
				}
			}
		};
	}

	/**
	 * <p>Attempts to cancel execution of this task.  This attempt will
	 * fail if the task has already completed, already been cancelled,
	 * or could not be cancelled for some other reason. If successful,
	 * and this task has not started when <tt>cancel</tt> is called,
	 * this task should never run. If the task has already started,
	 * then the <tt>mayInterruptIfRunning</tt> parameter determines
	 * whether the thread executing this task should be interrupted in
	 * an attempt to stop the task.</p>
	 * <p>
	 * <p>Calling this method will activeState in {@link #onCancelled(Object)} being
	 * invoked on the UI thread after {@link #doInBackground(Object[])}
	 * returns. Calling this method guarantees that {@link #onPostExecute(Object)}
	 * is never invoked. After invoking this method, you should check the
	 * value returned by {@link #isCancelled()} periodically from
	 * {@link #doInBackground(Object[])} to finish the task as early as
	 * possible.</p>
	 *
	 * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
	 *                              task should be interrupted; otherwise, in-progress tasks are allowed
	 *                              to complete.
	 *
	 * @return <tt>false</tt> if the task could not be cancelled,
	 * typically because it has already completed normally;
	 * <tt>true</tt> otherwise
	 *
	 * @see #isCancelled()
	 * @see #onCancelled(Object)
	 */
	public final boolean cancel( boolean mayInterruptIfRunning )
	{
		mCancelled.set( true );
		return mFuture.cancel( mayInterruptIfRunning );
	}

	/**
	 * Override this method to perform a computation on a background thread. The
	 * specified parameters are the parameters passed to {@link #execute}
	 * by the caller of this task.
	 * <p>
	 * This method can call {@link #publishProgress} to publish updates
	 * on the UI thread.
	 *
	 * @param params The parameters of the task.
	 *
	 * @return A activeState, defined by the subclass of this task.
	 *
	 * @see #onPreExecute()
	 * @see #onPostExecute
	 * @see #publishProgress
	 */
	protected abstract Result doInBackground( Params... params );

	/**
	 * Executes the task with the specified parameters. The task returns
	 * itself (this) so that the caller can keep a reference to it.
	 * <p>
	 * <p>This method is typically used with {@link Executor} to
	 * allow multiple tasks to run in parallel on a pool of threads.
	 * <p>
	 * <p><em>Warning:</em> Allowing multiple tasks to run in parallel from
	 * a thread pool is generally <em>not</em> what one wants, because the order
	 * of their operation is not defined.  For example, if these tasks are used
	 * to modify any state in common (such as writing a file),
	 * there are no guarantees on the order of the modifications.
	 * Without careful work it is possible in rare cases for the newer version
	 * of the data to be over-written by an older one, leading to obscure data
	 * loss and stability issues.  Such changes are best
	 * executed in serial; to guarantee such work is serialized regardless of
	 * platform version you can use {@link #executeSerial}
	 *
	 * @param executor The executor to use.
	 * @param params   The parameters of the task.
	 *
	 * @return This instance of AsyncTask.
	 *
	 * @throws IllegalStateException If {@link #getStatus()} returns either
	 *                               {@link AsyncTask.Status#RUNNING} or {@link AsyncTask.Status#FINISHED}.
	 * @see #executeSerial(Object[])
	 * @see #executeParallel(Object[])
	 */
	public final AsyncTask<Params, Progress, Result> execute( @Nonnull Executor executor, Params... params )
	{
		Objs.notNull( executor );

		if ( mStatus != Status.PENDING )
		{
			switch ( mStatus )
			{
				case RUNNING:
					throw new IllegalStateException( "Cannot execute task: the task is already running." );
				case FINISHED:
					throw new IllegalStateException( "Cannot execute task: the task has already been executed (a task can be executed only once)" );
			}
		}

		mStatus = Status.RUNNING;

		onPreExecute();

		mWorker.mParams = params;
		executor.execute( mFuture );

		return this;
	}

	/**
	 * Executes the task with the specified parameters. The task returns
	 * itself (this) so that the caller can keep a reference to it.
	 *
	 * @param params The parameters of the task.
	 *
	 * @return This instance of AsyncTask.
	 *
	 * @throws IllegalStateException If {@link #getStatus()} returns either
	 *                               {@link AsyncTask.Status#RUNNING} or {@link AsyncTask.Status#FINISHED}.
	 * @see #execute(java.util.concurrent.Executor, Object[])
	 */
	public final AsyncTask<Params, Progress, Result> executeParallel( Params... params )
	{
		return execute( Kernel.getExecutorParallel(), params );
	}

	/**
	 * Executes the task with the specified parameters. The task returns
	 * itself (this) so that the caller can keep a reference to it.
	 * <p>
	 * <p>Note: this function schedules the task on a queue for a single background
	 * thread or pool of threads depending on the platform version.
	 *
	 * @param params The parameters of the task.
	 *
	 * @return This instance of AsyncTask.
	 *
	 * @throws IllegalStateException If {@link #getStatus()} returns either
	 *                               {@link AsyncTask.Status#RUNNING} or {@link AsyncTask.Status#FINISHED}.
	 * @see #execute(java.util.concurrent.Executor, Object[])
	 */
	public final AsyncTask<Params, Progress, Result> executeSerial( Params... params )
	{
		return execute( Kernel.getExecutorSerial(), params );
	}

	private void finish( Result result )
	{
		if ( isCancelled() )
			onCancelled( result );
		else
			onPostExecute( result );
		mStatus = Status.FINISHED;
	}

	/**
	 * Waits if necessary for the computation to complete, and then
	 * retrieves its activeState.
	 *
	 * @return The computed activeState.
	 *
	 * @throws CancellationException If the computation was cancelled.
	 * @throws ExecutionException    If the computation threw an exception.
	 * @throws InterruptedException  If the current thread was interrupted while waiting.
	 */
	public final Result get() throws InterruptedException, ExecutionException
	{
		return mFuture.get();
	}

	/**
	 * Waits if necessary for at most the given time for the computation
	 * to complete, and then retrieves its activeState.
	 *
	 * @param timeout Time to wait before cancelling the operation.
	 * @param unit    The time unit for the timeout.
	 *
	 * @return The computed activeState.
	 *
	 * @throws CancellationException If the computation was cancelled.
	 * @throws ExecutionException    If the computation threw an exception.
	 * @throws InterruptedException  If the current thread was interrupted
	 *                               while waiting.
	 * @throws TimeoutException      If the wait timed out.
	 */
	public final Result get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
	{
		return mFuture.get( timeout, unit );
	}

	/**
	 * Returns the current status of this task.
	 *
	 * @return The current status.
	 */
	public final Status getStatus()
	{
		return mStatus;
	}

	/**
	 * Returns <tt>true</tt> if this task was cancelled before it completed
	 * normally. If you are calling {@link #cancel(boolean)} on the task,
	 * the value returned by this method should be checked periodically from
	 * {@link #doInBackground(Object[])} to end the task as soon as possible.
	 *
	 * @return <tt>true</tt> if task was cancelled before it completed
	 *
	 * @see #cancel(boolean)
	 */
	public final boolean isCancelled()
	{
		return mCancelled.get();
	}

	/**
	 * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
	 * {@link #doInBackground(Object[])} has finished.</p>
	 * <p>
	 * <p>The default implementation simply invokes {@link #onCancelled()} and
	 * ignores the activeState. If you write your own implementation, do not call
	 * <code>super.onCancelled(activeState)</code>.</p>
	 *
	 * @param result The activeState, if any, computed in
	 *               {@link #doInBackground(Object[])}, can be null
	 *
	 * @see #cancel(boolean)
	 * @see #isCancelled()
	 */
	@SuppressWarnings( {"UnusedParameters"} )
	protected void onCancelled( Result result )
	{
		onCancelled();
	}

	/**
	 * <p>Applications should preferably override {@link #onCancelled(Object)}.
	 * This method is invoked by the default implementation of
	 * {@link #onCancelled(Object)}.</p>
	 * <p>
	 * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
	 * {@link #doInBackground(Object[])} has finished.</p>
	 *
	 * @see #onCancelled(Object)
	 * @see #cancel(boolean)
	 * @see #isCancelled()
	 */
	protected void onCancelled()
	{
	}

	/**
	 * <p>Runs on the UI thread after {@link #doInBackground}. The
	 * specified activeState is the value returned by {@link #doInBackground}.</p>
	 * <p>
	 * <p>This method won't be invoked if the task was cancelled.</p>
	 *
	 * @param result The activeState of the operation computed by {@link #doInBackground}.
	 *
	 * @see #onPreExecute
	 * @see #doInBackground
	 * @see #onCancelled(Object)
	 */
	@SuppressWarnings( {"UnusedDeclaration"} )
	protected void onPostExecute( Result result )
	{
	}

	/**
	 * Runs on the UI thread before {@link #doInBackground}.
	 *
	 * @see #onPostExecute
	 * @see #doInBackground
	 */
	protected void onPreExecute()
	{
	}

	/**
	 * Runs on the UI thread after {@link #publishProgress} is invoked.
	 * The specified values are the values passed to {@link #publishProgress}.
	 *
	 * @param values The values indicating progress.
	 *
	 * @see #publishProgress
	 * @see #doInBackground
	 */
	@SuppressWarnings( {"UnusedDeclaration"} )
	protected void onProgressUpdate( Progress... values )
	{
	}

	private Result postResult( Result result )
	{
		@SuppressWarnings( "unchecked" )
		ParcelCarrier carrier = ParcelCarrier.obtain( MESSAGE_POST_RESULT, new AsyncTaskResult<>( this, result ) );
		Foundation.getRouter().sendParcel( carrier );
		return result;
	}

	private void postResultIfNotInvoked( Result result )
	{
		final boolean wasTaskInvoked = mTaskInvoked.get();
		if ( !wasTaskInvoked )
		{
			postResult( result );
		}
	}

	/**
	 * This method can be invoked from {@link #doInBackground} to
	 * publish updates on the UI thread while the background computation is
	 * still running. Each call to this method will trigger the execution of
	 * {@link #onProgressUpdate} on the UI thread.
	 * <p>
	 * {@link #onProgressUpdate} will not be called if the task has been
	 * canceled.
	 *
	 * @param values The progress values to update the UI with.
	 *
	 * @see #onProgressUpdate
	 * @see #doInBackground
	 */
	protected final void publishProgress( Progress... values )
	{
		if ( !isCancelled() )
			Foundation.getRouter().sendParcel( ParcelCarrier.obtain( MESSAGE_POST_PROGRESS, new AsyncTaskResult<Progress>( this, values ) ) );
	}

	/**
	 * Indicates the current status of the task. Each status will be set only once
	 * during the lifetime of a task.
	 */
	public enum Status
	{
		/**
		 * Indicates that the task has not been executed yet.
		 */
		PENDING,
		/**
		 * Indicates that the task is running.
		 */
		RUNNING,
		/**
		 * Indicates that {@link AsyncTask#onPostExecute} has finished.
		 */
		FINISHED,
	}

	@SuppressWarnings( {"RawUseOfParameterizedType"} )
	private static class AsyncTaskResult<Data>
	{
		final Data[] mData;
		final AsyncTask mTask;

		@SafeVarargs
		AsyncTaskResult( AsyncTask task, Data... data )
		{
			mTask = task;
			mData = data;
		}
	}

	private static abstract class WorkerCallable<Params, Result> implements Callable<Result>
	{
		Params[] mParams;
	}
}
