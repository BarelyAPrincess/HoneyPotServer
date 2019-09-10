package com.marchnetworks.alarm.service;

public class AlarmEntryCloseRecord
{
	private String alarmEntryId;
	private String closingComment;

	public AlarmEntryCloseRecord()
	{
	}

	public AlarmEntryCloseRecord( String alarmEntryId, String closingComment )
	{
		this.alarmEntryId = alarmEntryId;
		this.closingComment = closingComment;
	}

	public String getAlarmEntryId()
	{
		return alarmEntryId;
	}

	public void setAlarmEntryId( String alarmEntryId )
	{
		this.alarmEntryId = alarmEntryId;
	}

	public String getClosingComment()
	{
		return closingComment;
	}

	public void setClosingComment( String closingComment )
	{
		this.closingComment = closingComment;
	}
}
