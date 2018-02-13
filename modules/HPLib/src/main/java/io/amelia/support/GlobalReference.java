/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class GlobalReference<ValueType>
{
	private final Lock lock = new ReentrantLock();
	private volatile ValueType delayedValue;
	private Thread lockedByThread = null;
	private volatile ValueType value;

	public GlobalReference( ValueType value )
	{
		this.value = value;
	}

	/**
	 * Attempts to acquire the lock for the value and returns what was the state.
	 * Returns immediately as it won't block.
	 *
	 * @return The result of attempting to lock value.
	 */
	private LockState acquireLock()
	{
		boolean result = lock.tryLock();
		if ( result )
			return LockState.NOT_LOCKED;
		else if ( lockedByThread != null )
		{
			if ( lockedByThread == Thread.currentThread() )
				return LockState.LOCAL_THREAD_LOCKED;
			else
				return LockState.THREAD_LOCKED;
		}
		else
			return LockState.IS_LOCKED;
	}

	public ValueType get()
	{
		LockState state = acquireLock();

		try
		{
			if ( state == LockState.IS_LOCKED || state == LockState.THREAD_LOCKED )
				lock.lockInterruptibly();
			try
			{
				ValueType result = value;
				if ( delayedValue != null )
					value = delayedValue = null;
				return result;
			}
			finally
			{
				if ( state == LockState.NOT_LOCKED )
					lock.unlock();
			}
		}
		catch ( InterruptedException e )
		{
			return null;
		}
	}

	public ValueType getLazy()
	{
		LockState state = acquireLock();

		if ( state == LockState.IS_LOCKED || state == LockState.THREAD_LOCKED )
			return null;
		try
		{
			return value;
		}
		finally
		{
			if ( state == LockState.NOT_LOCKED )
				lock.unlock();
		}
	}

	public void setLazy( ValueType value )
	{
		LockState state = acquireLock();

		if ( state == LockState.NOT_LOCKED || state == LockState.LOCAL_THREAD_LOCKED )
		{
			try
			{
				this.value = value;
				delayedValue = null;
			}
			finally
			{
				if ( state == LockState.NOT_LOCKED )
					lock.unlock();
			}
		}
		else
			delayedValue = value;
	}

	public LockState getLockState()
	{
		LockState state = acquireLock();
		if ( state == LockState.NOT_LOCKED )
			lock.unlock();
		return state;
	}

	public void lock() throws InterruptedException
	{
		LockState state = acquireLock();

		if ( state == LockState.LOCAL_THREAD_LOCKED || state == LockState.NOT_LOCKED )
			return;

		lock.lockInterruptibly();
		lockedByThread = Thread.currentThread();
	}

	public void set( ValueType value )
	{
		LockState state = acquireLock();

		if ( state == LockState.IS_LOCKED || state == LockState.THREAD_LOCKED )
			lock.lock();
		try
		{
			this.value = value;
			this.delayedValue = null;
		}
		finally
		{
			if ( state != LockState.LOCAL_THREAD_LOCKED )
				lock.unlock();
		}
	}

	/**
	 * Attempts to lock and execute the given function.
	 * A non-null return value is then set in place of the value.
	 * <p>
	 * Note: Any pending delayedValues from {@link #setLazy(ValueType)} are discarded for reasons.
	 *
	 * @param function The function to receive and return the value.
	 * @return Was it a success
	 */
	public boolean sync( NonnullFunction<ValueType, ValueType> function )
	{
		LockState state = acquireLock();

		try
		{
			if ( state == LockState.IS_LOCKED || state == LockState.THREAD_LOCKED )
				lock.lockInterruptibly();
			try
			{
				ValueType update = function.apply( value );
				if ( value != update )
				{
					delayedValue = null;
					value = update;
				}
			}
			finally
			{
				if ( state != LockState.LOCAL_THREAD_LOCKED )
					lock.unlock();
			}
			return true;
		}
		catch ( InterruptedException e )
		{
			return false;
		}
	}

	public boolean sync( Consumer<ValueType> consumer )
	{
		LockState state = acquireLock();

		try
		{
			if ( state == LockState.IS_LOCKED || state == LockState.THREAD_LOCKED )
				lock.lockInterruptibly();
			try
			{
				consumer.accept( value );

				if ( delayedValue != null )
					value = delayedValue = null;
			}
			finally
			{
				if ( state != LockState.LOCAL_THREAD_LOCKED )
					lock.unlock();
			}
			return true;
		}
		catch ( InterruptedException e )
		{
			return false;
		}
	}

	public boolean tryLock()
	{
		LockState state = acquireLock();
		if ( state == LockState.NOT_LOCKED || state == LockState.LOCAL_THREAD_LOCKED )
		{
			lockedByThread = Thread.currentThread();
			return true;
		}
		return false;
	}

	public boolean trySync( NonnullFunction<ValueType, ValueType> function )
	{
		LockState state = acquireLock();

		if ( state == LockState.IS_LOCKED || state == LockState.THREAD_LOCKED )
			return false;

		try
		{
			ValueType update = function.apply( value );
			if ( value != update )
			{
				delayedValue = null;
				value = update;
			}
		}
		finally
		{
			if ( state == LockState.NOT_LOCKED )
				lock.unlock();
			/* Do not unlock is LOCAL_THREAD_LOCKED as this indicates it was previously locked. */
		}
		return true;
	}

	public boolean trySync( Consumer<ValueType> consumer )
	{
		LockState state = acquireLock();

		if ( state == LockState.IS_LOCKED || state == LockState.THREAD_LOCKED )
			return false;

		try
		{
			consumer.accept( value );

			if ( delayedValue != null )
				value = delayedValue = null;
		}
		finally
		{
			if ( state == LockState.NOT_LOCKED )
				lock.unlock();
			/* Do not unlock is LOCAL_THREAD_LOCKED as this indicates it was previously locked. */
		}
		return true;
	}

	public void unlock()
	{
		if ( lockedByThread == null )
			throw new IllegalStateException( "[BUG] The GlobalReference was not locked by the user. Check the method call order." );
		if ( lockedByThread != Thread.currentThread() )
			throw new IllegalStateException( "[BUG] The GlobalReference is currently locked to Thread " + lockedByThread.getName() + " and #unlock() was called by Thread " + Thread.currentThread().getName() + ". Must be called from the same Thread." );
		if ( delayedValue != null )
			value = delayedValue = null;
		lock.unlock();
		lockedByThread = null;
	}

	public enum LockState
	{
		/**
		 * Locked, so lock wasn't acquired.
		 */
		IS_LOCKED,
		/**
		 * Lock already acquired.
		 */
		LOCAL_THREAD_LOCKED,
		/**
		 * Locked to another thread, lock wasn't acquired.
		 */
		THREAD_LOCKED,
		/**
		 * Lock wasn't currently present, so lock was acquired.
		 */
		NOT_LOCKED
	}
}
