package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "TimeZoneInfo", propOrder = {"autoAdjust", "zoneBias", "standardName", "standardDate", "standardBias", "daylightName", "daylightDate", "daylightBias"} )
public class TimeZoneInfo
{
	protected boolean autoAdjust;
	protected int zoneBias;
	@XmlElement( required = true )
	protected String standardName;
	@XmlElement( required = true )
	protected String standardDate;
	protected int standardBias;
	@XmlElement( required = true )
	protected String daylightName;
	@XmlElement( required = true )
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
