package com.marchnetworks.command.common.timezones.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "supplementalData" )
@XmlAccessorType( XmlAccessType.FIELD )
public class SupplementalData
{
	@XmlElement
	private Generation generation;
	@XmlElement
	private Version version;
	@XmlElement
	private WindowsZones windowsZones;

	public Generation getGeneration()
	{
		return generation;
	}

	public Version getVersion()
	{
		return version;
	}

	public void setGeneration( Generation generation )
	{
		this.generation = generation;
	}

	public void setVersion( Version version )
	{
		this.version = version;
	}

	public WindowsZones getWindowsZones()
	{
		return windowsZones;
	}

	public void setWindowsZones( WindowsZones windowsZones )
	{
		this.windowsZones = windowsZones;
	}

	public String toString()
	{
		return String.format( "SupplementalData [generation=%s, version=%s, windowsZones=%s]", new Object[] {generation, version, windowsZones} );
	}
}
