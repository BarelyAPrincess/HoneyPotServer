package com.marchnetworks.command.api.metrics.input;

public class BucketCounterInput extends MetricInput
{
	private long value;

	private String bucket;

	public BucketCounterInput( String name, String bucket )
	{
		super( name );
		this.bucket = bucket;
		value = 1L;
	}

	public long getValue()
	{
		return value;
	}

	public String getBucket()
	{
		return bucket;
	}
}
