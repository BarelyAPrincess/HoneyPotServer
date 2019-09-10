package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"parameters"} )
@XmlRootElement( name = "SetParameters" )
public class SetParameters
{
	@XmlElement( required = true )
	protected ArrayOfGenericParameter parameters;

	public ArrayOfGenericParameter getParameters()
	{
		return parameters;
	}

	public void setParameters( ArrayOfGenericParameter value )
	{
		parameters = value;
	}
}
