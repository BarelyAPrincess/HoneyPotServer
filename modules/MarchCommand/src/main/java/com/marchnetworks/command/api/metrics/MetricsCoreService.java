package com.marchnetworks.command.api.metrics;

import java.util.List;

public abstract interface MetricsCoreService
{
	public abstract void addCounter( String paramString );

	public abstract void addCounter( String paramString, long paramLong );

	public abstract void addBucketCounter( String paramString1, String paramString2 );

	public abstract void addBucketCounterValue( String paramString1, String paramString2, long paramLong );

	public abstract void addBucketValue( String paramString1, String paramString2, long paramLong );

	public abstract void addBucketCounter( String paramString1, String paramString2, int paramInt );

	public abstract void addMinMaxAvg( String paramString, long paramLong );

	public abstract void addBucketMinMaxAvg( String paramString1, String paramString2, long paramLong );

	public abstract void addCurrentMaxAvg( String paramString, long paramLong );

	public abstract void addConcurrent( String paramString, long paramLong );

	public abstract void addRetryActionSuccess( String paramString1, String paramString2, long paramLong );

	public abstract void addRetryActionFailure( String paramString1, String paramString2, long paramLong );

	public abstract void addRetryActionSuccess( String paramString1, String paramString2, int paramInt, long paramLong );

	public abstract void addRetryActionFailure( String paramString1, String paramString2, int paramInt, long paramLong );

	public abstract void addRetryAction( String paramString, long paramLong );

	public abstract void addMax( String paramString, long paramLong );

	public abstract MetricSnapshot getCurrentMetrics();

	public abstract void clearCurrentMetrics();

	public abstract List<MetricSnapshot> getAllMetrics();

	public abstract void snapshotMetrics();

	public abstract void saveMetrics();

	public abstract void saveCurrentMetrics();

	public abstract List<MetricSnapshot> readMetricsFromLogString( String paramString );
}
