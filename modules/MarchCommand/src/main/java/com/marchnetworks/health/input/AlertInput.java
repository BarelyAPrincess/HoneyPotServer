package com.marchnetworks.health.input;

import com.marchnetworks.command.api.alert.AlertCategoryEnum;
import com.marchnetworks.command.api.alert.AlertDefinitionEnum;
import com.marchnetworks.command.common.transport.data.Pair;

public abstract class AlertInput
{
	public static final String NO_VALUE = "";
	private String alertCode;
	private String sourceId;
	private long alertTime = 0L;
	private long resolvedTime = 0L;

	private long lastTime;

	private String info;

	private String value;

	private AlertCategoryEnum category;

	private boolean deviceState;

	public AlertInput( String alertCode, AlertCategoryEnum category, String sourceId, long alertTime, long lastTime, long resolvedTime, String info, String value, boolean deviceState )
	{
		this.alertCode = alertCode;
		this.sourceId = sourceId;
		this.alertTime = alertTime;
		this.lastTime = lastTime;
		this.resolvedTime = resolvedTime;
		this.info = info;
		this.value = value;
		this.category = category;
		this.deviceState = deviceState;
	}

	public AlertInput( AlertDefinitionEnum definition, String sourceId, long alertTime, long lastTime, long resolvedTime, String info, String value, boolean deviceState )
	{
		this( definition.getPath(), definition.getCategory(), sourceId, alertTime, lastTime, resolvedTime, info, value, deviceState );
	}

	public long getAlertTime()
	{
		return alertTime;
	}

	public void setAlertTime( long alertTime )
	{
		this.alertTime = alertTime;
	}

	public long getResolvedTime()
	{
		return resolvedTime;
	}

	public void setResolvedTime( long resolvedTime )
	{
		this.resolvedTime = resolvedTime;
	}

	public long getLastTime()
	{
		return lastTime;
	}

	public void setLastTime( long lastTime )
	{
		this.lastTime = lastTime;
	}

	public static String pairsToString( Pair[] pairs )
	{
		StringBuilder parsedPairs = new StringBuilder( "" );
		if ( pairs != null )
		{
			for ( int i = 0; i < pairs.length; i++ )
			{
				Pair pair = pairs[i];
				parsedPairs.append( pair.getName() + "=" + pair.getValue() );

				if ( i < pairs.length - 1 )
					parsedPairs.append( "|" );
			}
		}
		return parsedPairs.toString();
	}

	public void setAlertCode( String alertCode )
	{
		this.alertCode = alertCode;
	}

	public String getAlertCode()
	{
		return alertCode;
	}

	public void setSourceId( String sourceId )
	{
		this.sourceId = sourceId;
	}

	public String getSourceId()
	{
		return sourceId;
	}

	public void setInfo( String info )
	{
		this.info = info;
	}

	public String getInfo()
	{
		return info;
	}

	public void setValue( String value )
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public void setCategory( AlertCategoryEnum category )
	{
		this.category = category;
	}

	public AlertCategoryEnum getCategory()
	{
		return category;
	}

	public void setDeviceState( boolean deviceState )
	{
		this.deviceState = deviceState;
	}

	public boolean isDeviceState()
	{
		return deviceState;
	}
}
