package com.marchnetworks.command.common.notification.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationContent
{
	private List<String> recipients;
	private String subject;
	private String message;
	private Map<String, byte[]> inlines = new HashMap();

	public List<String> getRecipients()
	{
		return recipients;
	}

	public void setRecipients( List<String> recipients )
	{
		this.recipients = recipients;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage( String message )
	{
		this.message = message;
	}

	public String getSubject()
	{
		return subject;
	}

	public void setSubject( String subject )
	{
		this.subject = subject;
	}

	public Map<String, byte[]> getInlines()
	{
		return inlines;
	}

	public void addAllInlines( Map<String, byte[]> c )
	{
		inlines.putAll( c );
	}

	public void addInlines( String key, byte[] c )
	{
		inlines.put( key, c );
	}
}
