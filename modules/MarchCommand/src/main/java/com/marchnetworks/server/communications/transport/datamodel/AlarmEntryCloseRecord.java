package com.marchnetworks.server.communications.transport.datamodel;

public class AlarmEntryCloseRecord
{
	protected String entryId;

	protected String closingText;

	protected String closedUser;

	protected long closedTime;

	public String getEntryId()
	{
		return entryId;
	}

	public void setEntryId( String entryId )
	{
		this.entryId = entryId;
	}

	public String getClosingText()
	{
		return closingText;
	}

	public void setClosingText( String closingText )
	{
		this.closingText = closingText;
	}

	public String getClosedUser()
	{
		return closedUser;
	}

	public void setClosedUser( String closedUser )
	{
		this.closedUser = closedUser;
	}

	public long getClosedTime()
	{
		return closedTime;
	}

	public void setClosedTime( long closedTime )
	{
		this.closedTime = closedTime;
	}
}

