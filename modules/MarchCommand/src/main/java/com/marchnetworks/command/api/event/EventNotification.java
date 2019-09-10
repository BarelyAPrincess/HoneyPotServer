package com.marchnetworks.command.api.event;

import com.marchnetworks.command.common.transport.data.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EventNotification
{
	protected long id;
	protected String path;
	protected String source;
	protected Object value;
	protected List<Pair> info = new ArrayList<>( 4 );

	protected long timestamp;

	protected transient String appId;

	public static class Builder
	{
		EventNotification notification;

		public Builder( String path )
		{
			notification = new EventNotification();
			notification.path = path;
		}

		public Builder( String path, String appId )
		{
			notification = new EventNotification();
			notification.path = path;
			notification.appId = appId;
		}

		public Builder source( String source )
		{
			notification.source = source;
			return this;
		}

		public Builder value( Object value )
		{
			notification.value = value;
			return this;
		}

		public Builder info( String key, String value )
		{
			notification.addInfo( key, value );
			return this;
		}

		public Builder eventId( long eventId )
		{
			notification.id = eventId;
			return this;
		}

		public Builder timestamp( long timestamp )
		{
			notification.timestamp = timestamp;
			return this;
		}

		public Builder id( long id )
		{
			notification.id = id;
			return this;
		}

		public EventNotification build()
		{
			return notification;
		}
	}

	public void addInfo( String name, String value )
	{
		Pair p = new Pair();
		p.setName( name );
		p.setValue( value );
		info.add( p );
	}

	public String getInfo( String name )
	{
		for ( Pair p : info )
		{
			if ( p.getName().equals( name ) )
			{
				return p.getValue();
			}
		}
		return null;
	}

	public List<String> getAllInfo( String name )
	{
		List<String> result = new ArrayList();
		for ( Pair p : info )
		{
			if ( p.getName().equals( name ) )
			{
				result.add( p.getValue() );
			}
		}
		return result;
	}

	public String getFirstInfo()
	{
		Iterator i$ = info.iterator();
		if ( i$.hasNext() )
		{
			Pair p = ( Pair ) i$.next();
			return p.getValue();
		}
		return null;
	}

	public long getId()
	{
		return id;
	}

	public void setId( long id )
	{
		this.id = id;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath( String path )
	{
		this.path = path;
	}

	public void setPath( String path, String appId )
	{
		this.path = path;
		this.appId = appId;
	}

	public String getSource()
	{
		return source;
	}

	public void setSource( String source )
	{
		this.source = source;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue( Object value )
	{
		this.value = value;
	}

	public List<Pair> getInfo()
	{
		return info;
	}

	public void setInfo( List<Pair> info )
	{
		this.info = info;
	}

	public void setInfo( Pair[] info )
	{
		this.info.clear();
		this.info.addAll( Arrays.asList( info ) );
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp( long timestamp )
	{
		this.timestamp = timestamp;
	}

	public String getAppId()
	{
		return appId;
	}
}
