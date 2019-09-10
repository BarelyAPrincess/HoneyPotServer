package com.marchnetworks.management.instrumentation.jmx;

import com.marchnetworks.management.instrumentation.model.Device;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public final class DeviceObjectNameFactory
{
	private String domain = null;

	public static final String TYPE_KEY = "type";

	public static final String PARENT_KEY = "parent";

	private ObjectName objectNamePattern;

	public static final String INSTANCE_KEY = "id";

	public static final String INSTANCE_NAME = "name";

	public DeviceObjectNameFactory( String domain )
	{
		this.domain = domain;
		try
		{
			objectNamePattern = new ObjectName( domain + ":*" );
		}
		catch ( MalformedObjectNameException e )
		{
			throw new IllegalArgumentException( "Cannot construct ObjectName", e );
		}
	}

	public ObjectName getObjectName( Device device )
	{
		try
		{
			StringBuffer buffer = new StringBuffer();

			buffer.append( domain ).append( ":" );
			buffer.append( "type" ).append( "=" ).append( device.getClass().getSimpleName() ).append( "," );

			if ( device.getParentDeviceId() != null )
			{
				buffer.append( "parent" ).append( "=" ).append( device.getParentDeviceId() ).append( "," );
			}
			buffer.append( "id" ).append( "=" ).append( ObjectName.quote( device.getDeviceId() ) );

			if ( device.getName() != null )
			{
				buffer.append( "," ).append( "name" ).append( "=" ).append( ObjectName.quote( device.getName() ) );
			}

			return new ObjectName( buffer.toString() );
		}
		catch ( MalformedObjectNameException e )
		{
			throw new IllegalArgumentException( "Cannot construct ObjectName for " + device.getDeviceId(), e );
		}
	}

	public ObjectName getObjectNamePattern()
	{
		return objectNamePattern;
	}

	public String getInstanceId( ObjectName name )
	{
		if ( name.getDomain().startsWith( domain ) )
		{
			String quotedName = name.getKeyProperty( "id" );
			return ObjectName.unquote( quotedName );
		}
		throw new IllegalArgumentException( "Inappropriate ObjectName to get instance:" + name );
	}
}

