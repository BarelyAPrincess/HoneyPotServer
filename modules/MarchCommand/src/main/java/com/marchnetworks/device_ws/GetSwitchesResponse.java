package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getSwitchesResult"} )
@XmlRootElement( name = "GetSwitchesResponse" )
public class GetSwitchesResponse
{
	@XmlElement( name = "GetSwitchesResult", required = true )
	protected ArrayOfSwitch getSwitchesResult;

	public ArrayOfSwitch getGetSwitchesResult()
	{
		return getSwitchesResult;
	}

	public void setGetSwitchesResult( ArrayOfSwitch value )
	{
		getSwitchesResult = value;
	}
}
