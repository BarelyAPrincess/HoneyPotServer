package com.marchnetworks.command.common.extractor.data;

public class Job
{
	private String id;

	private String ReferenceId;

	private String State;

	private String CompletionState;

	private boolean Restart;

	private long TimeoutPeriod;
	private transient Long extractorId;

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public String getState()
	{
		return State;
	}

	public void setState( String state )
	{
		State = state;
	}

	public String getCompletionState()
	{
		return CompletionState;
	}

	public void setCompletionState( String completionState )
	{
		CompletionState = completionState;
	}

	public boolean isRestart()
	{
		return Restart;
	}

	public void setRestart( boolean restart )
	{
		Restart = restart;
	}

	public long getTimeoutPeriod()
	{
		return TimeoutPeriod;
	}

	public void setTimeoutPeriod( long timeoutPeriod )
	{
		TimeoutPeriod = timeoutPeriod;
	}

	public String getReferenceId()
	{
		return ReferenceId;
	}

	public void setReferenceId( String referenceId )
	{
		ReferenceId = referenceId;
	}

	public Long getExtractorId()
	{
		return extractorId;
	}

	public void setExtractorId( Long extractorId )
	{
		this.extractorId = extractorId;
	}
}
