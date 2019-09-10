package com.marchnetworks.command.common.timezones.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType( XmlAccessType.FIELD )
public class MapZone
{
	@XmlAttribute( name = "other" )
	private String window;
	@XmlAttribute
	private String territory;
	@XmlAttribute( name = "type" )
	private String olson;

	public String getWindow()
	{
		return window;
	}

	public void setWindow( String other )
	{
		window = other;
	}

	public String getTerritory()
	{
		return territory;
	}

	public void setTerritory( String territory )
	{
		this.territory = territory;
	}

	public String getOlson()
	{
		return olson;
	}

	public int hashCode()
	{
		return window.hashCode() + olson.hashCode() + territory.hashCode();
	}

	public boolean equals( Object obj )
	{
		if ( ( obj instanceof MapZone ) )
		{
			MapZone theOther = ( MapZone ) obj;
			return ( window.equals( theOther.getWindow() ) ) && ( territory.equals( theOther.getTerritory() ) ) && ( olson.equals( theOther.getOlson() ) );
		}
		return false;
	}

	public void setOlson( String type )
	{
		olson = type;
	}

	public String toString()
	{
		return String.format( "MapZone [window=%s, territory=%s, olson=%s]", new Object[] {window, territory, olson} );
	}
}
