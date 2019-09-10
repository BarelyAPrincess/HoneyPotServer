package com.marchnetworks.command.common.transport.data;

import javax.xml.bind.annotation.XmlElement;

public class AddressZone
{
	private String name;
	private String address;
	private Integer httpPort;
	private Integer httpsPort;

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress( String address )
	{
		this.address = address;
	}

	@XmlElement( required = true )
	public Integer getHttpPort()
	{
		return httpPort;
	}

	public void setHttpPort( Integer httpPort )
	{
		this.httpPort = httpPort;
	}

	@XmlElement( required = true )
	public Integer getHttpsPort()
	{
		return httpsPort;
	}

	public void setHttpsPort( Integer httpsPort )
	{
		this.httpsPort = httpsPort;
	}

	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + ( address == null ? 0 : address.hashCode() );
		result = 31 * result + ( httpPort == null ? 0 : httpPort.hashCode() );
		result = 31 * result + ( httpsPort == null ? 0 : httpsPort.hashCode() );
		result = 31 * result + ( name == null ? 0 : name.hashCode() );
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
		AddressZone other = ( AddressZone ) obj;
		if ( address == null )
		{
			if ( address != null )
				return false;
		}
		else if ( !address.equals( address ) )
			return false;
		if ( httpPort == null )
		{
			if ( httpPort != null )
				return false;
		}
		else if ( !httpPort.equals( httpPort ) )
			return false;
		if ( httpsPort == null )
		{
			if ( httpsPort != null )
				return false;
		}
		else if ( !httpsPort.equals( httpsPort ) )
			return false;
		if ( name == null )
		{
			if ( name != null )
				return false;
		}
		else if ( !name.equals( name ) )
			return false;
		return true;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer( "" );
		sb.append( "name:" );
		sb.append( name );
		sb.append( "; address:" );
		sb.append( address );
		sb.append( "; httpPort:" );
		sb.append( httpPort );
		sb.append( "; httpsPort:" );
		sb.append( httpsPort );
		return sb.toString();
	}
}
