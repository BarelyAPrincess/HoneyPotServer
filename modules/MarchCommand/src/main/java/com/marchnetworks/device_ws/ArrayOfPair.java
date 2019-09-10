package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfPair", propOrder = {"pair"} )
public class ArrayOfPair
{
	@XmlElement( name = "Pair" )
	protected List<Pair> pair;

	public List<Pair> getPair()
	{
		if ( pair == null )
		{
			pair = new ArrayList();
		}
		return pair;
	}
}
