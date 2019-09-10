package com.marchnetworks.common.diagnostics;

import java.util.Date;

public class WatchdogNotification
{
	private int dependenciesRestarted;
	private boolean processRestarted;
	private String reason;
	private long timestamp;

	public WatchdogNotification( String reason, boolean processRestarted, int dependenciesRestarted, long timestamp )
	{
		this.reason = reason;
		this.processRestarted = processRestarted;
		this.dependenciesRestarted = dependenciesRestarted;
		this.timestamp = timestamp;
	}

	public int getDependenciesRestarted()
	{
		return dependenciesRestarted;
	}

	public boolean getProcessRestarted()
	{
		return processRestarted;
	}

	public String getReason()
	{
		return reason;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public String toString()
	{
		Date date = new Date( timestamp * 1000L );
		return date + ", Reason: " + reason + ", Process Restarted: " + processRestarted + ", Num Dependencies Restarted: " + dependenciesRestarted;
	}
}
