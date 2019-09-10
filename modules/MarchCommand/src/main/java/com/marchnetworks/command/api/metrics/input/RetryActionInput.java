package com.marchnetworks.command.api.metrics.input;

public class RetryActionInput extends MetricInput
{
	private long numRetry;

	private boolean isSuccess;

	private String source;

	private long value;

	public RetryActionInput( String name, long numRetry )
	{
		super( name );
		this.numRetry = numRetry;
	}

	public RetryActionInput( String name, boolean isSuccess, String source, long value )
	{
		super( name );
		this.isSuccess = isSuccess;
		this.source = source;
	}

	public boolean isRetry()
	{
		return numRetry > 0L;
	}

	public long getNumRetry()
	{
		return numRetry;
	}

	public boolean isSuccess()
	{
		return isSuccess;
	}

	public String getSource()
	{
		return source;
	}

	public long getValue()
	{
		return value;
	}
}
