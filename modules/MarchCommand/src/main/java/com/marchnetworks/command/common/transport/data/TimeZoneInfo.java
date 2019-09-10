package com.marchnetworks.command.common.transport.data;

public class TimeZoneInfo
{
	protected boolean autoAdjust;

	protected int zoneBias;

	protected String standardName;

	protected String standardDate;

	protected int standardBias;

	protected String daylightName;

	protected String daylightDate;

	protected int daylightBias;

	public boolean isAutoAdjust()
	{
		return autoAdjust;
	}

	public void setAutoAdjust( boolean value )
	{
		autoAdjust = value;
	}

	public int getZoneBias()
	{
		return zoneBias;
	}

	public void setZoneBias( int value )
	{
		zoneBias = value;
	}

	public String getStandardName()
	{
		return standardName;
	}

	public void setStandardName( String value )
	{
		standardName = value;
	}

	public String getStandardDate()
	{
		return standardDate;
	}

	public void setStandardDate( String value )
	{
		standardDate = value;
	}

	public int getStandardBias()
	{
		return standardBias;
	}

	public void setStandardBias( int value )
	{
		standardBias = value;
	}

	public String getDaylightName()
	{
		return daylightName;
	}

	public void setDaylightName( String value )
	{
		daylightName = value;
	}

	public String getDaylightDate()
	{
		return daylightDate;
	}

	public void setDaylightDate( String value )
	{
		daylightDate = value;
	}

	public int getDaylightBias()
	{
		return daylightBias;
	}

	public void setDaylightBias( int value )
	{
		daylightBias = value;
	}
}
