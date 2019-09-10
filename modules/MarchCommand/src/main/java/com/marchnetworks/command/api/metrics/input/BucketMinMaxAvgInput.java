package com.marchnetworks.command.api.metrics.input;

public class BucketMinMaxAvgInput extends MetricInput
{
	private String bucket;

	private long value;

	public BucketMinMaxAvgInput( String name, String bucket, long vlaue )
	{
		super( name );
		this.bucket = bucket;
		value = vlaue;
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
