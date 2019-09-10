package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfTextDetails", propOrder = {"textDetails"} )
public class ArrayOfTextDetails
{
	@XmlElement( name = "TextDetails" )
	protected List<TextDetails> textDetails;

	public List<TextDetails> getTextDetails()
	{
		if ( textDetails == null )
		{
			textDetails = new ArrayList();
		}
		return textDetails;
	}
}
