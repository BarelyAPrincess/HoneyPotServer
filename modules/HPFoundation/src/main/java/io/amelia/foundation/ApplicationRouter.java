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

import io.amelia.foundation.parcel.ParcelCarrier;
import io.amelia.foundation.parcel.ParcelReceiver;

/**
 * A {@link ApplicationRouter} allows you to send and receive parcels through the registered {@link ParcelReceiver} on this application.
 * <p>
 * The main use for this class is to enqueue an action to be performed on a different threads or even Applications over network or IPC.
 * <p>
 * Sending parcels is accomplished with the {@link #sendEmptyParcel}, {@link #sendParcel},
 * {@link #sendParcelAtTime}, and {@link #sendParcelDelayed} methods.
 * The <em>post</em> versions allow you to enqueue Runnable objects (or tasks).
 * The <em>send</em> versions allow you to enqueue a {@link ParcelCarrier} containing
 * a bundle of data, tag, and/or result code that will processed by a Receiver's
 * {@link ParcelReceiver#handleParcel(ParcelCarrier)} method.
 * <p>
 * When posting or sending, you can either allow the object to be processed as soon
 * as the looper queue is ready to do so, or specify a delay before it gets processed
 * or absolute time for it to be processed. The latter two allow you to implement
 * timeouts, ticks, and other timing-based behavior.
 * <p>
 * When a {@link ApplicationInterface} is instigated, its main thread is dedicated to
 * running a looper that takes care of managing the top-level application tasks and parcels.
 */
public class ApplicationRouter
{
	public void dispose()
	{
		// Nothing!
	}

	private boolean enqueueParcel( ParcelCarrier parcelCarrier, long uptimeMillis )
	{
		if ( parcelCarrier.getTargetReceiver() == null && parcelCarrier.getTargetChannel() == null )
			parcelCarrier.setTargetReceiver( Foundation.getApplication() );

		return Foundation.getLooper().enqueueParcel( parcelCarrier, uptimeMillis );
	}

	public void quit( boolean safely )
	{
		// TODO
	}

	/**
	 * Sends a InternalMessage containing only the what value.
	 *
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 */
	public final boolean sendEmptyParcel( int what )
	{
		return sendEmptyParcelDelayed( what, 0 );
	}

	/**
	 * Sends a ParcelCarrier containing only a result code, to be delivered at a specific time.
	 *
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 *
	 * @see #sendParcelAtTime(ParcelCarrier, long)
	 */

	public final boolean sendEmptyParcelAtTime( int code, long uptimeMillis )
	{
		return sendParcelAtTime( ParcelCarrier.obtain( code ), uptimeMillis );
	}

	/**
	 * Sends a InternalMessage containing only the what value, to be delivered
	 * after the specified amount of time elapses.
	 *
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 *
	 * @see #sendParcelDelayed(ParcelCarrier, long)
	 */
	public final boolean sendEmptyParcelDelayed( int code, long delayMillis )
	{
		return sendParcelDelayed( ParcelCarrier.obtain( code ), delayMillis );
	}

	/**
	 * Pushes a message onto the end of the message queue after all pending messages
	 * before the current time. It will be received in {@link ParcelReceiver#handleParcel(ParcelCarrier)},
	 * in the thread attached to this handler.
	 *
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 */
	public final boolean sendParcel( ParcelCarrier msg )
	{
		return sendParcelDelayed( msg, 0 );
	}

	/**
	 * Enqueue a message into the message queue after all pending messages
	 * before the absolute time (in milliseconds) <var>uptimeMillis</var>.
	 * <b>The time-base is {@link Kernel#uptime()}.</b>
	 * Time spent in deep sleep will add an additional delay to execution.
	 * You will receive it in {@link ParcelReceiver#handleParcel(ParcelCarrier)}, in the thread attached
	 * to this handler.
	 *
	 * @param uptimeMillis The absolute time at which the message should be
	 *                     delivered, using the
	 *                     {@link Kernel#uptime()} time-base.
	 *
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.  Note that a
	 * activeState of true does not mean the message will be processed -- if
	 * the looper is quit before the delivery time of the message
	 * occurs then the message will be dropped.
	 */
	public boolean sendParcelAtTime( ParcelCarrier parcelCarrier, long uptimeMillis )
	{
		return enqueueParcel( parcelCarrier, uptimeMillis );
	}

	/**
	 * Enqueue a message into the message queue after all pending messages
	 * before (current time + delayMillis). You will receive it in
	 * {@link ParcelReceiver#handleParcel(ParcelCarrier)}, in the thread attached to this handler.
	 *
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.  Note that a
	 * activeState of true does not mean the message will be processed -- if
	 * the looper is quit before the delivery time of the message
	 * occurs then the message will be dropped.
	 */
	public final boolean sendParcelDelayed( ParcelCarrier parcelCarrier, long delayMillis )
	{
		if ( delayMillis < 0 )
			delayMillis = 0;
		return sendParcelAtTime( parcelCarrier, Kernel.uptime() + delayMillis );
	}

	/**
	 * Enqueue a parcel at the front of the queue, to be processed on
	 * the next iteration of the looper.  You will receive it in
	 * {@link ParcelReceiver#handleParcel(ParcelCarrier)}, in the thread attached to this handler.
	 * <b>This method is only for use in very special circumstances -- it
	 * can easily starve the message queue, cause ordering problems, or have
	 * other unexpected side-effects.</b>
	 *
	 * @return Returns true if the message was successfully placed in to the
	 * message queue.  Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 */
	public final boolean sendParcelNow( ParcelCarrier parcelCarrier )
	{
		return enqueueParcel( parcelCarrier, 0 );
	}

	@Override
	public String toString()
	{
		return "Handler (" + getClass().getName() + ") {" + Integer.toHexString( System.identityHashCode( this ) ) + "}";
	}
}