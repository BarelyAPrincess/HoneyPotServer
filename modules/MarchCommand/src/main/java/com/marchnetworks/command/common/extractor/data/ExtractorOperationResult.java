package com.marchnetworks.command.common.extractor.data;

public class ExtractorOperationResult
{
	private String JobId;

	private String ResultState;

	private String FailReason;

	public String getJobId()
	{
		return JobId;
	}

	public void setJobId( String jobId )
	{
		JobId = jobId;
	}

	public String getResultState()
	{
		return ResultState;
	}

	public void setResultState( String resultState )
	{
		ResultState = resultState;
	}

	public String getFailReason()
	{
		return FailReason;
	}

	public void setFailReason( String failReason )
	{
		FailReason = failReason;
	}

	public String toString()
	{
		return "JobId: " + JobId + ", ResultState: " + ResultState + ", FailReason: " + FailReason;
	}
}
