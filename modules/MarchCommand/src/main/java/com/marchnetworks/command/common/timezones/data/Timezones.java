package com.marchnetworks.command.common.timezones.data;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "timezones" )
@XmlAccessorType( XmlAccessType.FIELD )
public class Timezones
{
	@XmlElement( name = "timezone" )
	private List<Timezone> timezones;

	public Timezones()
	{
	}

	public Timezones( List<Timezone> timezones )
	{
		this.timezones = timezones;
	}

	public List<Timezone> getTimezones()
	{
		return timezones;
	}

	public void setTimezones( List<Timezone> timezones )
	{
		this.timezones = timezones;
	}
}
