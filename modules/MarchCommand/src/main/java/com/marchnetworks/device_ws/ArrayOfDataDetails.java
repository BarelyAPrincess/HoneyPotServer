package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfDataDetails", propOrder = {"dataDetails"} )
public class ArrayOfDataDetails
{
	@XmlElement( name = "DataDetails" )
	protected List<DataDetails> dataDetails;

	public List<DataDetails> getDataDetails()
	{
		if ( dataDetails == null )
		{
			dataDetails = new ArrayList();
		}
		return dataDetails;
	}
}
