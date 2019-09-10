package com.marchnetworks.command.common.timezones.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "timezone" )
@XmlAccessorType( XmlAccessType.FIELD )
public class Timezone
{
	private String olson;
	private String window;
	private String delta;
	private String english;

	public void setOlson( String olson )
	{
		this.olson = olson;
	}

	public void setWindow( String window )
	{
		this.window = window;
	}

	public void setDelta( String delta )
	{
		this.delta = delta;
	}

	public void setEnglish( String english )
	{
		this.english = english;
	}

	public Timezone()
	{
	}

	public Timezone( String olson, String window, String delta, String english )
	{
		this.olson = olson;
		this.window = window;
		this.delta = delta;
		this.english = english;
	}

	public boolean equals( Object obj )
	{
		if ( ( obj instanceof Timezone ) )
		{
			Timezone other = ( Timezone ) obj;
			if ( ( other.getDelta().equals( delta ) ) && ( other.getEnglish().equals( english ) ) && ( other.getOlson().equals( olson ) ) && ( other.getWindow().equals( window ) ) )
			{
				return true;
			}
		}
		return false;
	}

	public String getDelta()
	{
		return delta;
	}

	public String getEnglish()
	{
		return english;
	}

	public String getOlson()
	{
		return olson;
	}

	public String getWindow()
	{
		return window;
	}

	public int hashCode()
	{
		return olson.hashCode() + window.hashCode() + delta.hashCode() + english.hashCode();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( "Timezone [olson=" ).append( olson ).append( ", window=" ).append( window ).append( ", delta=" ).append( delta ).append( ", english=" ).append( english ).append( "]" );
		return builder.toString();
	}
}
