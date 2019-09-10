package com.marchnetworks.server.network.settings;

import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.common.configuration.ConfigSettings;
import com.marchnetworks.common.device.ServerServiceException;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.system.ServerParameterStoreServiceIF;
import com.marchnetworks.server.event.BandwidthSettingsChangedEvent;
import com.marchnetworks.server.event.EventRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class NetworkBandwidthServiceImpl implements NetworkBandwidthService, InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( NetworkBandwidthServiceImpl.class );
	private static final String NETWORK_CONFIG_SETTINGS = "network_config_settings";
	private static final int MEGABITS = 1000000;

	public NetworkBandwidthServiceImpl()
	{
		bucketMap = new HashMap();

		lock = new Object();
	}

	public void onAppInitialized()
	{
		String result = serverParameterStore.getParameterValue( "network_config_settings" );
		if ( result != null )
		{
			settings = ( ( ConfigSettings ) CoreJsonSerializer.fromJson( result, ConfigSettings.class ) );
		}
		else
		{
			settings = new ConfigSettings();
		}
		computeSizeCap();
		eventRegistry.send( new BandwidthSettingsChangedEvent( settings ) );
	}

	private Map<String, PermitBucket> bucketMap;
	private ConfigSettings settings;
	private ServerParameterStoreServiceIF serverParameterStore;

	public void register( String host )
	{
		synchronized ( bucketMap )
		{
			PermitBucket bucket = bucketMap.get( host );
			if ( bucket == null )
			{
				bucket = new PermitBucket( currentSizeCap );
				bucketMap.put( host, bucket );
				LOG.debug( "Created bucket {}", host );
			}
			else
			{
				bucket.addConsumer();
				LOG.debug( "Added consumer to bucket {}", host );
			}
		}
	}

	public void unregister( String host )
	{
		synchronized ( bucketMap )
		{
			PermitBucket bucket = ( PermitBucket ) bucketMap.get( host );
			if ( bucket != null )
			{
				bucket.removeConsumer();
				LOG.debug( "Removed consumer for bucket {}", host );
				if ( !bucket.hasConsumers() )
				{
					LOG.debug( "Removing bucket {}", host );
				}
				bucketMap.remove( host );

				bucket.refillBucket();
			}
		}
	}

	public void getPermit( String host, int amountInBytes )
	{
		PermitBucket bucket = ( PermitBucket ) bucketMap.get( host );
		if ( bucket == null )
		{
			synchronized ( bucketMap )
			{

				register( host );
				bucket = ( PermitBucket ) bucketMap.get( host );
			}
		}

		bucket.getPermit( amountInBytes );
		setRefillSchedule();
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "{} bytes left in bucket", Integer.valueOf( bucket.availablePermits() ) );
		}
	}

	public ConfigSettings getSettings()
	{
		return settings;
	}

	public void updateSettings( ConfigSettings settings )
	{
		String json = CoreJsonSerializer.toJson( settings );
		try
		{
			serverParameterStore.storeParameter( "network_config_settings", json );
			this.settings = settings;
		}
		catch ( ServerServiceException localServerServiceException )
		{
		}

		computeSizeCap();
		synchronized ( bucketMap )
		{
			for ( PermitBucket bucket : bucketMap.values() )
			{
				bucket.updateCapacity( currentSizeCap );
			}
		}
		eventRegistry.send( new BandwidthSettingsChangedEvent( this.settings ) );
	}

	private void computeSizeCap()
	{
		int maxSimUpdates = settings.isMaxSimultaneousUpdatesEnabled() ? settings.getMaxSimultaneousUpdates() : 1;
		int sizeCap = Math.min( settings.getMaxBandwidth() * 1000000 / maxSimUpdates, settings.getMaxDeviceBandwidth() * 1000000 );
		currentSizeCap = ( sizeCap / 8 );
	}

	private void setRefillSchedule()
	{
		if ( needsRefillTask )
		{
			return;
		}

		synchronized ( lock )
		{
			if ( needsRefillTask )
			{
				return;
			}

			taskScheduler.schedule( new BucketRefillTask(), 1L, TimeUnit.SECONDS );
			needsRefillTask = true;
		}
	}

	private TaskScheduler taskScheduler;
	private EventRegistry eventRegistry;
	private int currentSizeCap;
	private boolean needsRefillTask;
	private Object lock;

	private class PermitBucket extends Semaphore
	{
		private int refCount;
		private int sizeThreshold;

		public PermitBucket( int sizeThreshold )
		{
			super( sizeThreshold );

			refCount = 1;
			this.sizeThreshold = sizeThreshold;
		}

		public void getPermit( int size )
		{
			if ( size > this.sizeThreshold )
				throw new IllegalArgumentException( "Too many permits requested." );
			acquireUninterruptibly( size );
		}

		public void addConsumer()
		{
			refCount += 1;
		}

		public void removeConsumer()
		{
			refCount -= 1;
		}

		public boolean hasConsumers()
		{
			return refCount > 0;
		}

		public void refillBucket()
		{
			drainPermits();
			release( sizeThreshold );
		}

		public void updateCapacity( int newSize )
		{
			int delta = sizeThreshold - newSize;
			if ( delta < 0 )
			{
				release( sizeThreshold - availablePermits() + Math.abs( delta ) );
			}
			else
			{
				reducePermits( delta );
			}
			sizeThreshold = newSize;
			NetworkBandwidthServiceImpl.LOG.debug( "Bucket was resized to {} permits", Integer.valueOf( sizeThreshold ) );
		}
	}

	private class BucketRefillTask implements Runnable
	{
		private BucketRefillTask()
		{
		}

		public void run()
		{
			long start = System.nanoTime();
			synchronized ( lock )
			{
				needsRefillTask = false;
			}

			synchronized ( bucketMap )
			{
				for ( PermitBucket bucket : bucketMap.values() )
				{
					if ( currentSizeCap > bucket.availablePermits() )
					{
						bucket.refillBucket();
					}
				}
			}
			if ( NetworkBandwidthServiceImpl.LOG.isDebugEnabled() )
			{
				NetworkBandwidthServiceImpl.LOG.debug( "Bucket Refill task took {} ns to run", Long.valueOf( System.nanoTime() - start ) );
			}
		}
	}

	public void setServerParameterStore( ServerParameterStoreServiceIF serverParameterStore )
	{
		this.serverParameterStore = serverParameterStore;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}
}

