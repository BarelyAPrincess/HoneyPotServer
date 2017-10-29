package io.amelia.android;

import com.sun.istack.internal.Nullable;

import io.amelia.synchronize.Message;

public class Looper
{
	final MessageQueue mQueue;
	final Thread mThread;

	public Looper( boolean quitAllowed )
	{
		mQueue = new MessageQueue( quitAllowed );
		mThread = Thread.currentThread();
	}

	/**
	 * Gets this looper's message queue.
	 *
	 * @return The looper's message queue.
	 */
	public MessageQueue getQueue()
	{
		return mQueue;
	}

	/**
	 * Gets the Thread associated with this Looper.
	 *
	 * @return The looper's thread.
	 */
	public Thread getThread()
	{
		return mThread;
	}

	/**
	 * Returns true if the current thread is this looper's thread.
	 */
	public boolean isCurrentThread()
	{
		return Thread.currentThread() == mThread;
	}

	/**
	 * Quits the looper.
	 * <p>
	 * Causes the {@link #loop} method to terminate without processing any
	 * more messages in the message queue.
	 * </p><p>
	 * Any attempt to post messages to the queue after the looper is asked to quit will fail.
	 * For example, the {@link Handler#sendMessage(Message)} method will return false.
	 * </p><p class="note">
	 * Using this method may be unsafe because some messages may not be delivered
	 * before the looper terminates.  Consider using {@link #quitSafely} instead to ensure
	 * that all pending work is completed in an orderly manner.
	 * </p>
	 *
	 * @see #quitSafely
	 */
	public void quit()
	{
		mQueue.quit( false );
	}

	/**
	 * Quits the looper safely.
	 * <p>
	 * Causes the {@link #loop} method to terminate as soon as all remaining messages
	 * in the message queue that are already due to be delivered have been handled.
	 * However pending delayed messages with due times in the future will not be
	 * delivered before the loop terminates.
	 * </p><p>
	 * Any attempt to post messages to the queue after the looper is asked to quit will fail.
	 * For example, the {@link Handler#sendMessage(Message)} method will return false.
	 * </p>
	 */
	public void quitSafely()
	{
		mQueue.quit( true );
	}

	/**
	 * Control logging of messages as they are processed by this Looper.  If
	 * enabled, a log message will be written to <var>printer</var>
	 * at the beginning and ending of each message dispatch, identifying the
	 * target Handler and message contents.
	 *
	 * @param printer A Printer object that will receive log messages, or
	 *                null to disable message logging.
	 */
	public void setMessageLogging( @Nullable Printer printer )
	{
		mLogging = printer;
	}

	/**
	 * {@hide}
	 */
	public void setTraceTag( long traceTag )
	{
		mTraceTag = traceTag;
	}

	@Override
	public String toString()
	{
		return "Looper (" + mThread.getName() + ", tid " + mThread.getId() + ") {" + Integer.toHexString( System.identityHashCode( this ) ) + "}";
	}
}
