package com.marchnetworks.health.data;

import com.marchnetworks.command.api.alert.AlertCategoryEnum;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.common.types.AlertSeverityEnum;
import com.marchnetworks.common.types.AlertUserStateEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.bind.annotation.XmlElement;

public class AlertData
{
	private long id;
	private String alertCode;
	private long alertTime;
	private long lastInstanceTime;
	private long count;
	private long alertResolvedTime;
	private boolean deviceState;
	private String sourceId;
	private String sourceDesc;
	private AlertSeverityEnum severity;
	private AlertCategoryEnum category;
	private List<Pair> info;
	private AlertUserStateEnum userState;
	private long closedTime;

	public AlertData()
	{
	}

	public AlertData( long id )
	{
		this.id = id;
	}

	public AlertData( String alertCode, long alertTime, long lastInstanceTime, long count, long alertResolvedTime, boolean deviceState, String sourceId, String sourceDesc, AlertSeverityEnum severity, AlertCategoryEnum category, String info, long id, AlertUserStateEnum userState, long closedTime )
	{
		this.alertCode = alertCode;
		this.alertTime = alertTime;
		this.lastInstanceTime = lastInstanceTime;
		this.count = count;
		this.alertResolvedTime = alertResolvedTime;
		this.deviceState = deviceState;
		this.sourceId = sourceId;
		this.sourceDesc = sourceDesc;
		this.severity = severity;
		this.category = category;

		generateInfo( info );

		this.id = id;
		this.userState = userState;
		this.closedTime = closedTime;
	}

	public List<Pair> getInfo()
	{
		return info;
	}

	public void setInfo( List<Pair> info )
	{
		this.info = info;
	}

	public void generateInfo( String infoString )
	{
		if ( info == null )
		{
			info = new ArrayList();
		}

		if ( infoString == null )
		{
			return;
		}

		if ( !"".equals( infoString ) )
		{
			StringTokenizer pairs = new StringTokenizer( infoString, "|" );

			while ( pairs.hasMoreTokens() )
			{
				StringTokenizer pair = new StringTokenizer( pairs.nextToken(), "=" );
				while ( pair.hasMoreTokens() )
				{
					String key = pair.nextToken();
					String value = "";

					if ( pair.hasMoreTokens() )
					{
						value = pair.nextToken();
					}
					info.add( new Pair( key, value ) );
				}
			}
		}
	}

	public String getAlertCode()
	{
		return alertCode;
	}

	public void setAlertCode( String a_AlertCode )
	{
		alertCode = a_AlertCode;
	}

	public long getAlertTime()
	{
		return alertTime;
	}

	public void setAlertTime( long a_AlertTime )
	{
		alertTime = a_AlertTime;
	}

	public long getLastInstanceTime()
	{
		return lastInstanceTime;
	}

	public void setLastInstanceTime( long lastInstanceTime )
	{
		this.lastInstanceTime = lastInstanceTime;
	}

	public long getCount()
	{
		return count;
	}

	public void setCount( long count )
	{
		this.count = count;
	}

	public long getAlertResolvedTime()
	{
		return alertResolvedTime;
	}

	public void setAlertResolvedTime( long a_AlertResolvedTime )
	{
		alertResolvedTime = a_AlertResolvedTime;
	}

	public String getSourceDesc()
	{
		return sourceDesc;
	}

	public void setSourceDesc( String a_SourceDesc )
	{
		sourceDesc = a_SourceDesc;
	}

	public String getSourceId()
	{
		return sourceId;
	}

	public void setSourceId( String a_SourceId )
	{
		sourceId = a_SourceId;
	}

	public boolean getDeviceState()
	{
		return deviceState;
	}

	public void setDeviceState( boolean a_DeviceState )
	{
		deviceState = a_DeviceState;
	}

	@XmlElement( required = true )
	public AlertCategoryEnum getCategory()
	{
		return category;
	}

	public void setCategory( AlertCategoryEnum a_Category )
	{
		category = a_Category;
	}

	@XmlElement( required = true )
	public AlertSeverityEnum getSeverity()
	{
		return severity;
	}

	public void setSeverity( AlertSeverityEnum a_Severity )
	{
		severity = a_Severity;
	}

	public long getId()
	{
		return id;
	}

	public void setId( long id )
	{
		this.id = id;
	}

	@XmlElement( required = true )
	public AlertUserStateEnum getUserState()
	{
		return userState;
	}

	public void setUserState( AlertUserStateEnum userState )
	{
		this.userState = userState;
	}

	public long getClosedTime()
	{
		return closedTime;
	}

	public void setClosedTime( long closedTime )
	{
		this.closedTime = closedTime;
	}

	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + ( int ) ( id ^ id >>> 32 );
		return result;
	}

	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		AlertData other = ( AlertData ) obj;
		if ( id != id )
			return false;
		return true;
	}
}
