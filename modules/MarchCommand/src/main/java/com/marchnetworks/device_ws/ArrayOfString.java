package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfString", propOrder = {"string"} )
public class ArrayOfString
{
	@XmlElement( nillable = true )
	protected List<String> string;

	public List<String> getString()
	{
		if ( string == null )
		{
			string = new ArrayList();
		}
		return string;
	}
}
