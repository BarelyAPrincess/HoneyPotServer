package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getAlarmSourcesResult"} )
@XmlRootElement( name = "GetAlarmSourcesResponse" )
public class GetAlarmSourcesResponse
{
	@XmlElement( name = "GetAlarmSourcesResult", required = true )
	protected ArrayOfAlarmSource getAlarmSourcesResult;

	public ArrayOfAlarmSource getGetAlarmSourcesResult()
	{
		return getAlarmSourcesResult;
	}

	public void setGetAlarmSourcesResult( ArrayOfAlarmSource value )
	{
		getAlarmSourcesResult = value;
	}
}
