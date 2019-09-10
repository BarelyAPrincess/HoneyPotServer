package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "GetParametersResult", propOrder = {"lastEventId", "parameters"} )
public class GetParametersResult
{
	protected long lastEventId;
	@XmlElement( required = true )
	protected ArrayOfGenericParameter parameters;

	public long getLastEventId()
	{
		return lastEventId;
	}

	public void setLastEventId( long value )
	{
		lastEventId = value;
	}

	public ArrayOfGenericParameter getParameters()
	{
		return parameters;
	}

	public void setParameters( ArrayOfGenericParameter value )
	{
		parameters = value;
	}
}
