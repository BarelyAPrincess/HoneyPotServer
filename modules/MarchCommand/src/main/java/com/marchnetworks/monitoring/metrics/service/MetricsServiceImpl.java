package com.marchnetworks.monitoring.metrics.service;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.api.metrics.BucketCounter;
import com.marchnetworks.command.api.metrics.BucketMinMaxAvg;
import com.marchnetworks.command.api.metrics.BucketValue;
import com.marchnetworks.command.api.metrics.ConcurrentAction;
import com.marchnetworks.command.api.metrics.Counter;
import com.marchnetworks.command.api.metrics.CurrentMaxAvg;
import com.marchnetworks.command.api.metrics.MaxValue;
import com.marchnetworks.command.api.metrics.Metric;
import com.marchnetworks.command.api.metrics.MetricSnapshot;
import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.api.metrics.MinMaxAvg;
import com.marchnetworks.command.api.metrics.RetryAction;
import com.marchnetworks.command.api.metrics.SingleValueMetric;
import com.marchnetworks.command.api.metrics.input.BucketCounterInput;
import com.marchnetworks.command.api.metrics.input.BucketMinMaxAvgInput;
import com.marchnetworks.command.api.metrics.input.ConcurrentActionInput;
import com.marchnetworks.command.api.metrics.input.CounterInput;
import com.marchnetworks.command.api.metrics.input.CurrentMaxAvgInput;
import com.marchnetworks.command.api.metrics.input.MaxValueInput;
import com.marchnetworks.command.api.metrics.input.MetricInput;
import com.marchnetworks.command.api.metrics.input.MinMaxAvgInput;
import com.marchnetworks.command.api.metrics.input.RetryActionInput;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.DateUtils;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.monitoring.metrics.dao.MetricsDAO;
import com.marchnetworks.monitoring.metrics.event.MetricsEvent;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class MetricsServiceImpl implements MetricsCoreService, InitializationListener, EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( MetricsServiceImpl.class );

	private static final String LOG_FILE = "..\\logs\\metrics.json";
	private static final int DEFAULT_MAX = 25;
	private MetricsDAO metricsDAO;
	private Map<String, Metric> metrics = new ConcurrentSkipListMap();

	public void onAppInitialized()
	{
		MetricSnapshot existingMetrics = null;
		try
		{
			existingMetrics = metricsDAO.find();
		}
		catch ( Exception e )
		{
			LOG.error( "Error reading metrics file: " + e.getMessage() );
			metricsDAO.delete();
		}

		if ( ( existingMetrics == null ) || ( existingMetrics.getMetrics() == null ) )
		{
			return;
		}

		long todayLocal = DateUtils.getLocalTimeMillis( System.currentTimeMillis() );
		todayLocal = DateUtils.getRoundedTime( todayLocal, 86400000L );

		long existingLocal = DateUtils.getLocalTimeMillis( existingMetrics.getTime() );
		existingLocal = DateUtils.getRoundedTime( existingLocal, 86400000L );

		if ( existingLocal != todayLocal )
		{
			existingMetrics.onSerialization();
			writeMetricsToLogs( existingMetrics );
			metricsDAO.delete();
		}
		else
		{
			List<Metric> metricList = existingMetrics.getMetrics();

			for ( Metric metric : metricList )
			{
				metrics.put( metric.getName(), metric );
			}
		}
	}

	public void destroy()
	{
		snapshotMetrics();
	}

	public String getListenerName()
	{
		return MetricsServiceImpl.class.getSimpleName();
	}

	public void process( Event event )
	{
		if ( ( event instanceof MetricsEvent ) )
		{
			MetricsEvent metricsEvent = ( MetricsEvent ) event;
			MetricInput input = metricsEvent.getMetricInput();
			processMetricInput( input );
		}
	}

	public void saveMetrics()
	{
		MetricSnapshot current = getCurrentMetrics();
		current.onSerialization();
		writeMetricsToLogs( current );

		metrics.clear();
		metricsDAO.delete();
	}

	public void snapshotMetrics()
	{
		MetricSnapshot snapshot = getCurrentMetrics();
		metricsDAO.update( snapshot );
	}

	public void saveCurrentMetrics()
	{
		MetricSnapshot snapshot = getCurrentMetrics();
		metricsDAO.saveCurrent( snapshot );
	}

	public List<MetricSnapshot> readMetricsFromLogString( String string )
	{
		if ( CommonAppUtils.isNullOrEmptyString( string ) )
		{
			return Collections.emptyList();
		}
		String json = "[" + string.substring( 0, string.length() - 2 ) + "]";

		List<MetricSnapshot> result = ( List ) CoreJsonSerializer.collectionFromJson( json, new TypeToken()
		{
		} );
		return result;
	}

	public MetricSnapshot getCurrentMetrics()
	{
		List<Metric> values = new ArrayList( metrics.values() );
		MetricSnapshot snapshot = new MetricSnapshot( System.currentTimeMillis(), values );
		return snapshot;
	}

	public void clearCurrentMetrics()
	{
		metrics.clear();
		metricsDAO.delete();
	}

	public List<MetricSnapshot> getAllMetrics()
	{
		String logFile = CommonAppUtils.readFileToString( "..\\logs\\metrics.json" );

		return readMetricsFromLogString( logFile );
	}

	private void writeMetricsToLogs( MetricSnapshot snapshot )
	{
		String json = CoreJsonSerializer.toJsonIndented( snapshot );
		CommonAppUtils.appendStringToFile( "..\\logs\\metrics.json", json + "\n," );
	}

	public void addCounter( String name )
	{
		addSingleValue( Counter.class, name, 1L );
	}

	public void addCounter( String name, long value )
	{
		addSingleValue( Counter.class, name, value );
	}

	public void addBucketCounter( String name, String bucket )
	{
		addBucketCounter( name, bucket, 0 );
	}

	public void addBucketValue( String name, String bucket, long value )
	{
		BucketValue existing = ( BucketValue ) metrics.get( name );
		if ( existing == null )
		{
			existing = new BucketValue( name );
			metrics.put( name, existing );
		}
		existing.addValue( bucket, value );
	}

	public void addBucketCounterValue( String name, String bucket, long value )
	{
		BucketCounter existing = ( BucketCounter ) metrics.get( name );
		if ( existing == null )
		{
			existing = new BucketCounter( name, 0 );
			metrics.put( name, existing );
		}
		existing.addValue( bucket, value );
	}

	public void addBucketCounter( String name, String bucket, int itemsPerLine )
	{
		BucketCounter existing = ( BucketCounter ) metrics.get( name );
		if ( existing == null )
		{
			existing = new BucketCounter( name, itemsPerLine );
			metrics.put( name, existing );
		}
		existing.addValue( bucket, 1L );
	}

	public void addMinMaxAvg( String name, long value )
	{
		MinMaxAvg existing = ( MinMaxAvg ) metrics.get( name );

		if ( existing == null )
		{
			existing = new MinMaxAvg( name, value );
			metrics.put( name, existing );
		}
		else
		{
			existing.addValue( value );
		}
	}

	public void addBucketMinMaxAvg( String name, String bucket, long value )
	{
		BucketMinMaxAvg existing = ( BucketMinMaxAvg ) metrics.get( name );

		if ( existing == null )
		{
			existing = new BucketMinMaxAvg( name, bucket, value );
			metrics.put( name, existing );
		}
		else
		{
			existing.addValue( bucket, value );
		}
	}

	public void addCurrentMaxAvg( String name, long value )
	{
		CurrentMaxAvg existing = ( CurrentMaxAvg ) metrics.get( name );

		if ( existing == null )
		{
			existing = new CurrentMaxAvg( name, value );
			metrics.put( name, existing );
		}
		else
		{
			existing.addValue( value );
		}
	}

	public void addConcurrent( String name, long concurrent )
	{
		addSingleValue( ConcurrentAction.class, name, concurrent );
	}

	public void addMax( String name, long value )
	{
		addSingleValue( MaxValue.class, name, value );
	}

	public void addRetryActionSuccess( String name, String source, long value )
	{
		addRetryActionSuccess( name, source, 25, value );
	}

	public void addRetryActionSuccess( String name, String source, int maxSources, long value )
	{
		RetryAction existing = ( RetryAction ) metrics.get( name );

		if ( existing == null )
		{
			existing = new RetryAction( name, maxSources );
			metrics.put( name, existing );
		}
		existing.addSuccess( source, value );
	}

	public void addRetryActionFailure( String name, String source, long value )
	{
		addRetryActionFailure( name, source, 25, value );
	}

	public void addRetryActionFailure( String name, String source, int maxSources, long value )
	{
		RetryAction existing = ( RetryAction ) metrics.get( name );

		if ( existing == null )
		{
			existing = new RetryAction( name, maxSources );
			metrics.put( name, existing );
		}
		existing.addFailure( source, value );
	}

	public void addRetryAction( String name, long numRetry )
	{
		RetryAction existing = ( RetryAction ) metrics.get( name );

		if ( existing == null )
		{
			throw new IllegalStateException( "Success or failure must occur before retry" );
		}
		existing.addRetry( numRetry );
	}

	private void addSingleValue( Class<? extends SingleValueMetric> type, String name, long value )
	{
		SingleValueMetric existing = ( SingleValueMetric ) metrics.get( name );
		try
		{
			if ( existing == null )
			{
				Constructor<? extends SingleValueMetric> ctor = type.getConstructor( new Class[] {String.class} );
				existing = ( SingleValueMetric ) ctor.newInstance( new Object[] {name} );
				metrics.put( name, existing );
			}
			existing.addValue( value );
		}
		catch ( Exception e )
		{
			LOG.error( "Error during Metric initialization, Exception " + e.getMessage() );
		}
	}

	private void processMetricInput( MetricInput metricInput )
	{
		if ( ( metricInput instanceof CounterInput ) )
		{
			CounterInput input = ( CounterInput ) metricInput;
			addCounter( input.getName(), input.getValue() );
		}
		else if ( ( metricInput instanceof BucketCounterInput ) )
		{
			BucketCounterInput input = ( BucketCounterInput ) metricInput;
			addBucketCounter( input.getName(), input.getBucket() );
		}
		else if ( ( metricInput instanceof ConcurrentActionInput ) )
		{
			ConcurrentActionInput input = ( ConcurrentActionInput ) metricInput;
			addConcurrent( input.getName(), input.getMaxConcurrent() );
		}
		else if ( ( metricInput instanceof MaxValueInput ) )
		{
			MaxValueInput input = ( MaxValueInput ) metricInput;
			addMax( input.getName(), input.getValue() );
		}
		else if ( ( metricInput instanceof MinMaxAvgInput ) )
		{
			MinMaxAvgInput input = ( MinMaxAvgInput ) metricInput;
			addMinMaxAvg( input.getName(), input.getValue() );
		}
		else if ( ( metricInput instanceof CurrentMaxAvgInput ) )
		{
			CurrentMaxAvgInput input = ( CurrentMaxAvgInput ) metricInput;
			addCurrentMaxAvg( input.getName(), input.getValue() );
		}
		else if ( ( metricInput instanceof BucketMinMaxAvgInput ) )
		{
			BucketMinMaxAvgInput input = ( BucketMinMaxAvgInput ) metricInput;
			addBucketMinMaxAvg( input.getName(), input.getBucket(), input.getValue() );
		}
		else if ( ( metricInput instanceof RetryActionInput ) )
		{
			RetryActionInput input = ( RetryActionInput ) metricInput;

			if ( input.isRetry() )
			{
				addRetryAction( input.getName(), input.getNumRetry() );
			}
			else if ( input.isSuccess() )
			{
				addRetryActionSuccess( input.getName(), input.getSource(), input.getValue() );
			}
			else
			{
				addRetryActionFailure( input.getName(), input.getSource(), input.getValue() );
			}
		}
	}

	public void setMetricsDAO( MetricsDAO metricsDAO )
	{
		this.metricsDAO = metricsDAO;
	}
}

