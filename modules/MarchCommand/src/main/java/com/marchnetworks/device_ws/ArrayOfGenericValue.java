package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfGenericValue", propOrder = {"genericValue"} )
public class ArrayOfGenericValue
{
	@XmlElement( name = "GenericValue" )
	protected List<GenericValue> genericValue;

	public List<GenericValue> getGenericValue()
	{
		if ( genericValue == null )
		{
			genericValue = new ArrayList();
		}
		return genericValue;
	}
}
