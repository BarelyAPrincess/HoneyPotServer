package com.marchnetworks.command.common.extractor.data;

import java.util.concurrent.Future;

public class RecorderJob extends Job
{
	private String RecorderId;
	private RecorderAuth RecorderAuth;
	private String RecorderUrl;
	private String RecorderStatus;
	private transient Future<?> refreshTimer;

	public String getRecorderId()
	{
		return RecorderId;
	}

	public void setRecorderId( String recorderId )
	{
		RecorderId = recorderId;
	}

	public RecorderAuth getRecorderAuth()
	{
		return RecorderAuth;
	}

	public void setRecorderAuth( RecorderAuth recorderAuth )
	{
		RecorderAuth = recorderAuth;
	}

	public String getRecorderUrl()
	{
		return RecorderUrl;
	}

	public void setRecorderUrl( String recorderUrl )
	{
		RecorderUrl = recorderUrl;
	}

	public String getRecorderStatus()
	{
		return RecorderStatus;
	}

	public void setRecorderStatus( String recorderStatus )
	{
		RecorderStatus = recorderStatus;
	}

	public Future<?> getRefreshTimer()
	{
		return refreshTimer;
	}

	public void setRefreshTimer( Future<?> refreshTimer )
	{
		this.refreshTimer = refreshTimer;
	}
}
