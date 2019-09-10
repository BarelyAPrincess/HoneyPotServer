package com.marchnetworks.common.diagnostics.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.NotificationEmitter;

public class MemoryPoolWatcher
{
	private static final Logger LOG = LoggerFactory.getLogger( MemoryPoolWatcher.class );

	private Map<MemoryPool, Double> thresholds = new HashMap<MemoryPool, Double>( MemoryPool.values().length );
	private List<MemoryPoolListenerData> listeners = new ArrayList<MemoryPoolListenerData>( 1 );

	public void init()
	{
		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		NotificationEmitter emitter = ( NotificationEmitter ) mbean;
		MemoryThresholdListener listener = new MemoryThresholdListener( this );
		emitter.addNotificationListener( listener, null, null );

		for ( MemoryPool pool : MemoryPool.values() )
		{
			thresholds.put( pool, Double.valueOf( 0.0D ) );
		}
	}

	private boolean setThreshold( MemoryPool memPool, double threshold )
	{
		if ( ( threshold > 1.0D ) || ( threshold < 0.0D ) )
			return false;

		MemoryPoolMXBean pool = getPool( memPool );
		if ( pool != null )
		{
			long max = pool.getUsage().getMax();
			long limit = ( long ) ( max * threshold );

			if ( pool.isCollectionUsageThresholdSupported() )
			{
				pool.setCollectionUsageThreshold( limit );
			}
			else if ( pool.isUsageThresholdSupported() )
			{
				pool.setUsageThreshold( limit );
			}

			thresholds.put( memPool, Double.valueOf( threshold ) );
			return true;
		}
		return false;
	}

	private MemoryPoolMXBean getPool( MemoryPool memPool )
	{
		for ( MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans() )
		{
			String poolBeanName = pool.getName();
			for ( String poolName : memPool.getNames() )
			{
				if ( poolBeanName.contains( poolName ) )
				{
					return pool;
				}
			}
		}

		LOG.error( "Memory pool " + memPool + " not found" );

		return null;
	}

	public void addListener( MemoryPoolListener memListener, MemoryPool memPool, double[] memThresholds )
	{
		MemoryPoolListenerData listener = new MemoryPoolListenerData( memListener, memPool, memThresholds );
		listeners.add( listener );

		if ( ( ( Double ) thresholds.get( memPool ) ).doubleValue() == 0.0D )
		{
			setThreshold( memPool, listener.getMinimumThreshold() );
		}
	}

	public void resetThresholdsIfDisabled( MemoryPool memPool )
	{
		if ( ( ( Double ) thresholds.get( memPool ) ).doubleValue() == 0.0D )
		{

			double minimum = 1.0D;
			for ( MemoryPoolListenerData listener : listeners )
			{
				if ( listener.getPool() == memPool )
				{
					double threshold = listener.getMinimumThreshold();
					if ( threshold < minimum )
					{
						minimum = threshold;
					}
				}
			}
			if ( minimum < 1.0D )
			{
				setThreshold( memPool, minimum );
			}
		}
	}

	public void notifyListeners( MemoryNotificationInfo info )
	{
		String name = info.getPoolName();
		MemoryPool memPool = MemoryPool.fromValue( name );
		if ( memPool != null )
		{
			double currentThreshold = ( ( Double ) thresholds.get( memPool ) ).doubleValue();

			if ( currentThreshold != 0.0D )
			{
				for ( MemoryPoolListenerData listener : listeners )
				{
					if ( ( listener.getPool() == memPool ) && ( listener.containsThreshold( currentThreshold ) ) )
					{
						listener.getListener().handleMemoryThresholdExceeded( memPool, currentThreshold );
					}
				}

				double nextThreshold = getNextThreshold( memPool, currentThreshold );
				if ( nextThreshold < 1.0D )
				{
					setThreshold( memPool, nextThreshold );
				}
				else
				{
					LOG.info( "Last threshold crossed for " + memPool + ", stopping notifications" );
					setThreshold( memPool, 0.0D );
				}
			}
		}
	}

	private double getNextThreshold( MemoryPool memPool, double current )
	{
		double minimum = 1.0D;
		for ( MemoryPoolListenerData listener : listeners )
		{
			if ( listener.getPool() == memPool )
			{
				double nextThreshold = listener.getNextThreshold( current );
				if ( nextThreshold < minimum )
				{
					minimum = nextThreshold;
				}
			}
		}
		return minimum;
	}
}

