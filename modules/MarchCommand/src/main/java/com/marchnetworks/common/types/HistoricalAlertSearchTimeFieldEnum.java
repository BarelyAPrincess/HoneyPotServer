package com.marchnetworks.common.types;

public enum HistoricalAlertSearchTimeFieldEnum
{
	ALERT_TIME( "Problem Start" ),
	ALERT_RESOLVED_TIME( "Alert Resolved" ),
	USER_CLOSED_AT( "Closed At" );

	private final String m_description;

	private HistoricalAlertSearchTimeFieldEnum( String description )
	{
		m_description = description;
	}

	public String toString()
	{
		return m_description;
	}
}
