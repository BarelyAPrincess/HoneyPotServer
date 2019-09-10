package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfGenericParameter", propOrder = {"genericParameter"} )
public class ArrayOfGenericParameter
{
	@XmlElement( name = "GenericParameter" )
	protected List<GenericParameter> genericParameter;

	public List<GenericParameter> getGenericParameter()
	{
		if ( genericParameter == null )
		{
			genericParameter = new ArrayList();
		}
		return genericParameter;
	}
}
