package com.marchnetworks.command.common.extractor.data;

public class RecorderUpdate
{
	private String RecorderId;

	private String State;

	private String Url;

	private RecorderAuth AuthToken;

	public RecorderUpdate( String recorderId, String state )
	{
		RecorderId = recorderId;
		State = state;
	}

	public RecorderUpdate( String recorderId, String url, RecorderAuth authToken, String state )
	{
		RecorderId = recorderId;
		Url = url;
		AuthToken = authToken;
		State = state;
	}

	public String getRecorderId()
	{
		return RecorderId;
	}

	public void setRecorderId( String recorderId )
	{
		RecorderId = recorderId;
	}

	public String getState()
	{
		return State;
	}

	public void setState( String state )
	{
		State = state;
	}

	public String getUrl()
	{
		return Url;
	}

	public void setUrl( String url )
	{
		Url = url;
	}

	public RecorderAuth getAuthToken()
	{
		return AuthToken;
	}

	public void setAuthToken( RecorderAuth authToken )
	{
		AuthToken = authToken;
	}
}
