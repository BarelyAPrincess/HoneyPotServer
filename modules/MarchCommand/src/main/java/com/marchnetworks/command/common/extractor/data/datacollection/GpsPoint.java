package com.marchnetworks.command.common.extractor.data.datacollection;

import java.util.Objects;

public class GpsPoint
{
	private String RecorderId;
	private long Time;
	private int Lat;
	private int Lon;
	private short Speed;
	private boolean Gap;

	public String getRecorderId()
	{
		return RecorderId;
	}

	public void setRecorderId( String recorderId )
	{
		RecorderId = recorderId;
	}

	public long getTime()
	{
		return Time;
	}

	public void setTime( long time )
	{
		Time = time;
	}

	public int getLat()
	{
		return Lat;
	}

	public void setLat( int lat )
	{
		Lat = lat;
	}

	public int getLon()
	{
		return Lon;
	}

	public void setLon( int lon )
	{
		Lon = lon;
	}

	public short getSpeed()
	{
		return Speed;
	}

	public void setSpeed( short speed )
	{
		Speed = speed;
	}

	public boolean isGap()
	{
		return Gap;
	}

	public void setGap( boolean gap )
	{
		Gap = gap;
	}

	public int hashCode()
	{
		return Objects.hash( new Object[] {RecorderId, Long.valueOf( Time )} );
	}

	public boolean equals( Object other )
	{
		if ( this == other )
		{
			return true;
		}
		if ( ( other == null ) || ( !( other instanceof GpsPoint ) ) )
		{
			return false;
		}
		GpsPoint otherGps = ( GpsPoint ) other;
		return ( RecorderId.equals( otherGps.getRecorderId() ) ) && ( Time == otherGps.getTime() );
	}
}
