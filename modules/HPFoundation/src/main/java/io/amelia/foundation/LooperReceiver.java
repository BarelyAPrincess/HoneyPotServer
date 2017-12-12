package io.amelia.foundation;

import java.util.function.Consumer;

import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.support.DateAndTime;

/**
 * A Handler allows you to send and process {@link InternalMessage} and Runnable
 * objects associated with a thread's {@link Looper}.  Each Handler
 * instance is associated with a single {@link Looper}.
 * <p>
 * There are two main uses for a Handler: (1) to schedule messages and
 * runnables to be executed as some point in the future; and (2) to enqueue
 * an action to be performed on a different thread than your own.
 * <p>
 * Scheduling messages is accomplished with the
 * {@link #post}, {@link #postAtTime(Runnable, long)},
 * {@link #postDelayed}, {@link #sendEmptyInternalMessage},
 * {@link #sendInternalMessage}, {@link #sendInternalMessageAtTime}, and
 * {@link #sendInternalMessageDelayed} methods.  The <em>post</em> versions allow
 * you to enqueue Runnable objects to be called by the message queue when
 * they are received; the <em>sendInternalMessage</em> versions allow you to enqueue
 * a {@link InternalMessage} object containing a bundle of data that will be
 * processed by the Handler's {@link #handleInternalMessage} method (requiring that
 * you implement a subclass of Handler).
 * <p>
 * When posting or sending to a Handler, you can either
 * allow the item to be processed as soon as the message queue is ready
 * to do so, or specify a delay before it gets processed or absolute time for
 * it to be processed.  The latter two allow you to implement timeouts,
 * ticks, and other timing-based behavior.
 * <p>
 * When a process is created for your application, its main thread is dedicated to
 * running a message queue that takes care of managing the top-level
 * application tasks and messages.  You can create your own threads,
 * and communicate back with the main application thread through a Handler.
 */
public class LooperReceiver
{
	public static final Logger LOG = LogBuilder.get( LooperReceiver.class );

	final boolean async;
	final Consumer<InternalMessage> callback;
	final Looper looper;

	/**
	 * Default constructor associates this handler with the {@link Looper} for the
	 * current thread.
	 * <p>
	 * If this thread does not have a looper, this handler won't be able to receive messages
	 * so an exception is thrown.
	 */
	public LooperReceiver()
	{
		this( null, false );
	}

	/**
	 * Constructor associates this handler with the {@link Looper} for the
	 * current thread and takes a callback interface in which you can handle
	 * messages.
	 * <p>
	 * If this thread does not have a looper, this handler won't be able to receive messages
	 * so an exception is thrown.
	 *
	 * @param callback The callback interface in which to handle messages, or null.
	 */
	public LooperReceiver( Consumer<InternalMessage> callback )
	{
		this( callback, false );
	}

	/**
	 * Use the provided {@link Looper} instead of the default one.
	 *
	 * @param looper The looper, must not be null.
	 */
	public LooperReceiver( Looper looper )
	{
		this( looper, null, false );
	}

	/**
	 * Use the provided {@link Looper} instead of the default one and take a callback
	 * interface in which to handle messages.
	 *
	 * @param looper   The looper, must not be null.
	 * @param callback The callback interface in which to handle messages, or null.
	 */
	public LooperReceiver( Looper looper, Consumer<InternalMessage> callback )
	{
		this( looper, callback, false );
	}

	/**
	 * Use the {@link Looper} for the current thread
	 * and set whether the handler should be asynchronous.
	 * <p>
	 * Handlers are synchronous by default unless this constructor is used to make
	 * one that is strictly asynchronous.
	 * <p>
	 * Asynchronous messages represent interrupts or events that do not require global ordering
	 * with respect to synchronous messages.  Asynchronous messages are not subject to
	 * the synchronization barriers introduced by {@link LooperQueue#postTaskBarrier(long)}.
	 *
	 * @param async If true, the handler calls {@link InternalMessage#setAsync(boolean)} for
	 *              each {@link InternalMessage} that is sent to it or {@link Runnable} that is posted to it.
	 * @hide
	 */
	public LooperReceiver( boolean async )
	{
		this( null, async );
	}

	/**
	 * Use the {@link Looper} for the current thread with the specified callback interface
	 * and set whether the handler should be asynchronous.
	 * <p>
	 * Handlers are synchronous by default unless this constructor is used to make
	 * one that is strictly asynchronous.
	 * <p>
	 * Asynchronous messages represent interrupts or events that do not require global ordering
	 * with respect to synchronous messages.  Asynchronous messages are not subject to
	 * the synchronization barriers introduced by {@link LooperQueue#postTaskBarrier(long)}.
	 *
	 * @param callback The callback interface in which to handle messages, or null.
	 * @param async    If true, the handler calls {@link InternalMessage#setAsync(boolean)} for
	 *                 each {@link InternalMessage} that is sent to it or {@link Runnable} that is posted to it.
	 * @hide
	 */
	public LooperReceiver( Consumer<InternalMessage> callback, boolean async )
	{
		looper = Looper.Factory.obtain();
		if ( looper == null )
			throw new RuntimeException( "Can't create handler inside thread that has not called Looper.prepare()" );
		looper.queue = looper.getQueue();
		this.callback = callback;
		this.async = async;
	}

	/**
	 * Use the provided {@link Looper} instead of the default one and take a callback
	 * interface in which to handle messages.  Also set whether the handler
	 * should be asynchronous.
	 * <p>
	 * Handlers are synchronous by default unless this constructor is used to make
	 * one that is strictly asynchronous.
	 * <p>
	 * Asynchronous messages represent interrupts or events that do not require global ordering
	 * with respect to synchronous messages.  Asynchronous messages are not subject to
	 * the synchronization barriers introduced by {@link LooperQueue#postTaskBarrier(long)}.
	 *
	 * @param looper   The looper, must not be null.
	 * @param callback The callback interface in which to handle messages, or null.
	 * @param async    If true, the handler calls {@link InternalMessage#setAsync(boolean)} for
	 *                 each {@link InternalMessage} that is sent to it or {@link Runnable} that is posted to it.
	 * @hide
	 */
	public LooperReceiver( Looper looper, Consumer<InternalMessage> callback, boolean async )
	{
		this.looper = looper;
		looper.queue = looper.getQueue();
		this.callback = callback;
		this.async = async;
	}

	/**
	 * Handle system messages here.
	 */
	public void dispatchInternalMessage( InternalMessage msg )
	{
		if ( msg.callback != null )
		{
			msg.callback.accept( msg );
		}
		else
		{
			if ( callback != null )
			{
				if ( callback.handleMessage( msg ) )
				{
					return;
				}
			}
			handleInternalMessage( msg );
		}
	}

	private boolean enqueueInternalMessage( LooperQueue queue, InternalMessage msg, long uptimeMillis )
	{
		msg.target = this;
		if ( async )
			msg.setAsync( true );
		return queue.postMessage( msg, uptimeMillis );
	}

	/**
	 * Returns a string representing the name of the specified message.
	 * The default implementation will either return the class name of the
	 * message callback if any, or the hexadecimal representation of the
	 * message "what" field.
	 *
	 * @param message The message whose name is being queried
	 */
	public String getInternalMessageName( InternalMessage message )
	{
		if ( message.callback != null )
		{
			return message.callback.getClass().getName();
		}
		return "0x" + Integer.toHexString( message.what );
	}

	// if we can get rid of this method, the handler need not remember its loop
	// we could instead export a getLooperQueue() method...
	public final Looper getLooper()
	{
		return looper;
	}

	/**
	 * {@hide}
	 */
	public String getTraceName( InternalMessage message )
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( getClass().getName() ).append( ": " );
		if ( message.callback != null )
		{
			sb.append( message.callback.getClass().getName() );
		}
		else
		{
			sb.append( "#" ).append( message.what );
		}
		return sb.toString();
	}

	/**
	 * Subclasses must implement this to receive messages.
	 */
	public void handleInternalMessage( InternalMessage msg )
	{
	}

	/**
	 * Check if there are any pending posts of messages with callback r in
	 * the message queue.
	 *
	 * @hide
	 */
	public final boolean hasCallbacks( Runnable r )
	{
		return looper.queue.hasMessages( this, r, null );
	}

	/**
	 * Check if there are any pending posts of messages with code 'what' in
	 * the message queue.
	 */
	public final boolean hasMessages( int what )
	{
		return looper.queue.hasMessages( this, what, null );
	}

	/**
	 * Check if there are any pending posts of messages with code 'what' and
	 * whose obj is 'object' in the message queue.
	 */
	public final boolean hasMessages( int what, Object object )
	{
		return looper.queue.hasMessages( this, what, object );
	}

	/**
	 * Returns a new {@link InternalMessage} from the global message pool. More efficient than
	 * creating and allocating new instances. The retrieved message has its handler set to this instance (InternalMessage.target == this).
	 * If you don't want that facility, just call InternalMessage.obtain() instead.
	 */
	public final InternalMessage obtainInternalMessage()
	{
		return InternalMessage.obtain( this );
	}

	/**
	 * Same as {@link #obtainInternalMessage()}, except that it also sets the what member of the returned InternalMessage.
	 *
	 * @param what Value to assign to the returned InternalMessage.what field.
	 * @return A InternalMessage from the global message pool.
	 */
	public final InternalMessage obtainInternalMessage( int what )
	{
		return InternalMessage.obtain( this, what );
	}

	/**
	 * Same as {@link #obtainInternalMessage()}, except that it also sets the what and obj members
	 * of the returned InternalMessage.
	 *
	 * @param what Value to assign to the returned InternalMessage.what field.
	 * @param obj  Value to assign to the returned InternalMessage.obj field.
	 * @return A InternalMessage from the global message pool.
	 */
	public final InternalMessage obtainInternalMessage( int what, Object obj )
	{
		return InternalMessage.obtain( this, what, obj );
	}

	/**
	 * Same as {@link #obtainInternalMessage()}, except that it also sets the what, arg1 and arg2 members of the returned
	 * InternalMessage.
	 *
	 * @param what Value to assign to the returned InternalMessage.what field.
	 * @param arg1 Value to assign to the returned InternalMessage.arg1 field.
	 * @param arg2 Value to assign to the returned InternalMessage.arg2 field.
	 * @return A InternalMessage from the global message pool.
	 */
	public final InternalMessage obtainInternalMessage( int what, int arg1, int arg2 )
	{
		return InternalMessage.obtain( this, what, arg1, arg2 );
	}

	/**
	 * Same as {@link #obtainInternalMessage()}, except that it also sets the what, obj, arg1,and arg2 values on the
	 * returned InternalMessage.
	 *
	 * @param what Value to assign to the returned InternalMessage.what field.
	 * @param arg1 Value to assign to the returned InternalMessage.arg1 field.
	 * @param arg2 Value to assign to the returned InternalMessage.arg2 field.
	 * @param obj  Value to assign to the returned InternalMessage.obj field.
	 * @return A InternalMessage from the global message pool.
	 */
	public final InternalMessage obtainInternalMessage( int what, int arg1, int arg2, Object obj )
	{
		return InternalMessage.obtain( this, what, arg1, arg2, obj );
	}

	/**
	 * Causes the Runnable r to be added to the message queue.
	 * The runnable will be run on the thread to which this handler is
	 * attached.
	 *
	 * @param r The Runnable that will be executed.
	 * @return Returns true if the Runnable was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 */
	public final boolean post( Runnable r )
	{
		return sendInternalMessageDelayed( getPostInternalMessage( r ), 0 );
	}

	/**
	 * Posts a message to an object that implements Runnable.
	 * Causes the Runnable r to executed on the next iteration through the
	 * message queue. The runnable will be run on the thread to which this
	 * handler is attached.
	 * <b>This method is only for use in very special circumstances -- it
	 * can easily starve the message queue, cause ordering problems, or have
	 * other unexpected side-effects.</b>
	 *
	 * @param r The Runnable that will be executed.
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 */
	public final boolean postAtFrontOfQueue( Runnable r )
	{
		return sendInternalMessageAtFrontOfQueue( getPostInternalMessage( r ) );
	}

	/**
	 * Causes the Runnable r to be added to the message queue, to be run
	 * at a specific time given by <var>uptimeMillis</var>.
	 * <b>The time-base is {@link Kernel#uptime()}.</b>
	 * Time spent in deep sleep will add an additional delay to execution.
	 * The runnable will be run on the thread to which this handler is attached.
	 *
	 * @param r            The Runnable that will be executed.
	 * @param uptimeMillis The absolute time at which the callback should run,
	 *                     using the {@link Kernel#uptime()} time-base.
	 * @return Returns true if the Runnable was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.  Note that a
	 * result of true does not mean the Runnable will be processed -- if
	 * the looper is quit before the delivery time of the message
	 * occurs then the message will be dropped.
	 */
	public final boolean postAtTime( Runnable r, long uptimeMillis )
	{
		return sendInternalMessageAtTime( getPostInternalMessage( r ), uptimeMillis );
	}

	/**
	 * Causes the Runnable r to be added to the message queue, to be run
	 * at a specific time given by <var>uptimeMillis</var>.
	 * <b>The time-base is {@link Kernel#uptime()}.</b>
	 * Time spent in deep sleep will add an additional delay to execution.
	 * The runnable will be run on the thread to which this handler is attached.
	 *
	 * @param r            The Runnable that will be executed.
	 * @param uptimeMillis The absolute time at which the callback should run,
	 *                     using the {@link Kernel#uptime()} time-base.
	 * @return Returns true if the Runnable was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.  Note that a
	 * result of true does not mean the Runnable will be processed -- if
	 * the looper is quit before the delivery time of the message
	 * occurs then the message will be dropped.
	 * @see Kernel#uptime()
	 */
	public final boolean postAtTime( Runnable r, Object token, long uptimeMillis )
	{
		return sendInternalMessageAtTime( getPostInternalMessage( r, token ), uptimeMillis );
	}

	/**
	 * Causes the Runnable r to be added to the message queue, to be run
	 * after the specified amount of time elapses.
	 * The runnable will be run on the thread to which this handler
	 * is attached.
	 * <b>The time-base is {@link DateAndTime#epoch()}.</b>
	 * Time spent in deep sleep will add an additional delay to execution.
	 *
	 * @param r           The Runnable that will be executed.
	 * @param delayMillis The delay (in milliseconds) until the Runnable
	 *                    will be executed.
	 * @return Returns true if the Runnable was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.  Note that a
	 * result of true does not mean the Runnable will be processed --
	 * if the looper is quit before the delivery time of the message
	 * occurs then the message will be dropped.
	 */
	public final boolean postDelayed( Runnable r, long delayMillis )
	{
		return sendInternalMessageDelayed( getPostInternalMessage( r ), delayMillis );
	}

	/**
	 * Remove any pending posts of Runnable r that are in the message queue.
	 */
	public final void removeCallbacks( Runnable r )
	{
		looper.queue.removeInternalMessages( this, r, null );
	}

	/**
	 * Remove any pending posts of Runnable <var>r</var> with Object
	 * <var>token</var> that are in the message queue.  If <var>token</var> is null,
	 * all callbacks will be removed.
	 */
	public final void removeCallbacks( Runnable r, Object token )
	{
		looper.queue.removeInternalMessages( this, r, token );
	}

	/**
	 * Remove any pending posts of callbacks and sent messages whose
	 * <var>obj</var> is <var>token</var>.  If <var>token</var> is null,
	 * all callbacks and messages will be removed.
	 */
	public final void removeCallbacksAndInternalMessages( Object token )
	{
		looper.queue.removeCallbacksAndInternalMessages( this, token );
	}

	/**
	 * Remove any pending posts of messages with code 'what' that are in the
	 * message queue.
	 */
	public final void removeInternalMessages( int what )
	{
		looper.queue.removeInternalMessages( this, what, null );
	}

	/**
	 * Remove any pending posts of messages with code 'what' and whose obj is
	 * 'object' that are in the message queue.  If <var>object</var> is null,
	 * all messages will be removed.
	 */
	public final void removeInternalMessages( int what, Object object )
	{
		looper.queue.removeInternalMessages( this, what, object );
	}

	/**
	 * Runs the specified task synchronously.
	 * <p>
	 * If the current thread is the same as the handler thread, then the runnable
	 * runs immediately without being enqueued.  Otherwise, posts the runnable
	 * to the handler and waits for it to complete before returning.
	 * <p>
	 * This method is dangerous!  Improper use can result in deadlocks.
	 * Never call this method while any locks are held or use it in a
	 * possibly re-entrant manner.
	 * <p>
	 * This method is occasionally useful in situations where a background thread
	 * must synchronously await completion of a task that must run on the
	 * handler's thread.  However, this problem is often a symptom of bad design.
	 * Consider improving the design (if possible) before resorting to this method.
	 * <p>
	 * One example of where you might want to use this method is when you just
	 * set up a Handler thread and need to perform some initialization steps on
	 * it before continuing execution.
	 * <p>
	 * If timeout occurs then this method returns <code>false</code> but the runnable
	 * will remain posted on the handler and may already be in progress or
	 * complete at a later time.
	 * <p>
	 * When using this method, be sure to use {@link Looper#quitSafely} when
	 * quitting the looper.  Otherwise {@link #runUnsafe} may hang indefinitely.
	 * (TODO: We should fix this by making LooperQueue aware of blocking runnables.)
	 *
	 * @param r       The Runnable that will be executed synchronously.
	 * @param timeout The timeout in milliseconds, or 0 to wait indefinitely (makes this dangerous method, even more dangerous).
	 * @return Returns true if the Runnable was successfully executed.
	 * Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 */
	public final boolean runUnsafe( final Runnable r, long timeout )
	{
		if ( r == null )
			throw new IllegalArgumentException( "runnable must not be null" );

		if ( Looper.Factory.obtain() == looper )
		{
			r.run();
			return true;
		}

		BlockingRunnable br = new BlockingRunnable( r );
		if ( !post( br ) )
			return false;
		return br.postAndWait( timeout );
	}

	/**
	 * Sends a InternalMessage containing only the what value.
	 *
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 */
	public final boolean sendEmptyInternalMessage( int what )
	{
		return sendEmptyInternalMessageDelayed( what, 0 );
	}

	/**
	 * Sends a InternalMessage containing only the what value, to be delivered
	 * at a specific time.
	 *
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 * @see #sendInternalMessageAtTime(InternalMessage, long)
	 */

	public final boolean sendEmptyInternalMessageAtTime( int what, long uptimeMillis )
	{
		InternalMessage msg = InternalMessage.obtain();
		msg.what = what;
		return sendInternalMessageAtTime( msg, uptimeMillis );
	}

	/**
	 * Sends a InternalMessage containing only the what value, to be delivered
	 * after the specified amount of time elapses.
	 *
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 * @see #sendInternalMessageDelayed(InternalMessage, long)
	 */
	public final boolean sendEmptyInternalMessageDelayed( int what, long delayMillis )
	{
		InternalMessage msg = InternalMessage.obtain();
		msg.what = what;
		return sendInternalMessageDelayed( msg, delayMillis );
	}

	/**
	 * Pushes a message onto the end of the message queue after all pending messages
	 * before the current time. It will be received in {@link #handleInternalMessage},
	 * in the thread attached to this handler.
	 *
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 */
	public final boolean sendInternalMessage( InternalMessage msg )
	{
		return sendInternalMessageDelayed( msg, 0 );
	}

	/**
	 * Enqueue a message at the front of the message queue, to be processed on
	 * the next iteration of the message loop.  You will receive it in
	 * {@link #handleInternalMessage}, in the thread attached to this handler.
	 * <b>This method is only for use in very special circumstances -- it
	 * can easily starve the message queue, cause ordering problems, or have
	 * other unexpected side-effects.</b>
	 *
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 */
	public final boolean sendInternalMessageAtFrontOfQueue( InternalMessage msg )
	{
		LooperQueue queue = looper.queue;
		if ( queue == null )
		{
			RuntimeException e = new RuntimeException( this + " sendInternalMessageAtTime() called with no looper.queue" );
			App.L.warning( e.getInternalMessage(), e );
			return false;
		}
		return enqueueInternalMessage( queue, msg, 0 );
	}

	/**
	 * Enqueue a message into the message queue after all pending messages
	 * before the absolute time (in milliseconds) <var>uptimeMillis</var>.
	 * <b>The time-base is {@link Kernel#uptime()}.</b>
	 * Time spent in deep sleep will add an additional delay to execution.
	 * You will receive it in {@link #handleInternalMessage}, in the thread attached
	 * to this handler.
	 *
	 * @param uptimeMillis The absolute time at which the message should be
	 *                     delivered, using the
	 *                     {@link Kernel#uptime()} time-base.
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.  Note that a
	 * result of true does not mean the message will be processed -- if
	 * the looper is quit before the delivery time of the message
	 * occurs then the message will be dropped.
	 */
	public boolean sendInternalMessageAtTime( InternalMessage msg, long uptimeMillis )
	{
		LooperQueue queue = looper.queue;
		if ( queue == null )
		{
			RuntimeException e = new RuntimeException( this + " sendInternalMessageAtTime() called with no looper.queue" );
			LOG.warning( "Looper", e );
			return false;
		}
		return enqueueInternalMessage( queue, msg, uptimeMillis );
	}

	/**
	 * Enqueue a message into the message queue after all pending messages
	 * before (current time + delayMillis). You will receive it in
	 * {@link #handleInternalMessage}, in the thread attached to this handler.
	 *
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.  Note that a
	 * result of true does not mean the message will be processed -- if
	 * the looper is quit before the delivery time of the message
	 * occurs then the message will be dropped.
	 */
	public final boolean sendInternalMessageDelayed( InternalMessage msg, long delayMillis )
	{
		if ( delayMillis < 0 )
			delayMillis = 0;
		return sendInternalMessageAtTime( msg, Kernel.uptime() + delayMillis );
	}

	@Override
	public String toString()
	{
		return "Handler (" + getClass().getName() + ") {" + Integer.toHexString( System.identityHashCode( this ) ) + "}";
	}

}