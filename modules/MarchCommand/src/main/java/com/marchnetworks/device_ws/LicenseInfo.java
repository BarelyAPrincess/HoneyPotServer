package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "LicenseInfo", propOrder = {"total", "inuse", "expiry", "start", "end"} )
public class LicenseInfo
{
	protected int total;
	protected int inuse;
	@XmlElement( required = true )
	protected ExpiryType expiry;
	@XmlElement( required = true )
	protected String start;
	@XmlElement( required = true )
	protected String end;

	public int getTotal()
	{
		return total;
	}

	public void setTotal( int value )
	{
		total = value;
	}

	public int getInuse()
	{
		return inuse;
	}

	public void setInuse( int value )
	{
		inuse = value;
	}

	public ExpiryType getExpiry()
	{
		return expiry;
	}

	public void setExpiry( ExpiryType value )
	{
		expiry = value;
	}

	public String getStart()
	{
		return start;
	}

	public void setStart( String value )
	{
		start = value;
	}

	public String getEnd()
	{
		return end;
	}

	public void setEnd( String value )
	{
		end = value;
	}
}
