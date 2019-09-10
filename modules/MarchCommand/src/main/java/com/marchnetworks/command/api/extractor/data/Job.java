package com.marchnetworks.command.api.extractor.data;

public abstract class Job
{
	protected String jobId;

	protected String referenceId;

	protected Long extractorId;

	protected Integer jobTimeout;

	public String getJobId()
	{
		return jobId;
	}

	public void setJobId( String jobId )
	{
		this.jobId = jobId;
	}

	public String getReferenceId()
	{
		return referenceId;
	}

	public void setReferenceId( String referenceId )
	{
		this.referenceId = referenceId;
	}

	public Long getExtractorId()
	{
		return extractorId;
	}

	public void setExtractorId( Long extractorId )
	{
		this.extractorId = extractorId;
	}

	public Integer getJobTimeout()
	{
		return jobTimeout;
	}

	public void setJobTimeout( Integer jobTimeout )
	{
		this.jobTimeout = jobTimeout;
	}
}
