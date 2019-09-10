package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"paramPrefixes"} )
@XmlRootElement( name = "GetParameters" )
public class GetParameters
{
	@XmlElement( required = true )
	protected ArrayOfString paramPrefixes;

	public ArrayOfString getParamPrefixes()
	{
		return paramPrefixes;
	}

	public void setParamPrefixes( ArrayOfString value )
	{
		paramPrefixes = value;
	}
}
