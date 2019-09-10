package com.marchnetworks.command.common.timezones.data;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType( XmlAccessType.FIELD )
public class WindowsZones
{
	@XmlElementWrapper( name = "mapTimezones" )
	@XmlElement( name = "mapZone" )
	private List<MapZone> mapZones;

	public WindowsZones()
	{
	}

	public WindowsZones( List<MapZone> values )
	{
		mapZones = values;
	}

	public List<MapZone> getMapZones()
	{
		return mapZones;
	}

	public void setMapZones( List<MapZone> mapZones )
	{
		this.mapZones = mapZones;
	}

	public String toString()
	{
		return String.format( "WindowsZones [mapZones=%s]", new Object[] {mapZones} );
	}
}
