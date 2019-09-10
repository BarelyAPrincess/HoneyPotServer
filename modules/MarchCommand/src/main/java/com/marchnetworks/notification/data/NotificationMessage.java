package com.marchnetworks.notification.data;

import com.sun.xml.ws.util.ByteArrayDataSource;

import org.springframework.mail.SimpleMailMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataSource;

public class NotificationMessage extends SimpleMailMessage
{
	private NotificationContentFormat format;
	private Map<String, DataSource> inlines = new HashMap<>();

	public NotificationMessage()
	{
		format = NotificationContentFormat.HTML;
	}

	public NotificationMessage( NotificationContentFormat format )
	{
		this.format = format;
	}

	public void setContentFormat( NotificationContentFormat format )
	{
		this.format = format;
	}

	public NotificationContentFormat getContentFormat()
	{
		return format;
	}

	public Map<String, DataSource> getInlines()
	{
		return inlines;
	}

	public void addAllInlines( Map<String, byte[]> c )
	{
		for ( Entry<String, byte[]> i : c.entrySet() )
		{
			inlines.put( i.getKey(), new ByteArrayDataSource( ( byte[] ) i.getValue(), "image/jpeg" ) );
		}
	}

	public void addInlines( String key, DataSource c )
	{
		inlines.put( key, c );
	}
}

