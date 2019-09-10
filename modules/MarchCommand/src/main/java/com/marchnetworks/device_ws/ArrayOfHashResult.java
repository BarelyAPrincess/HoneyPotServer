package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfHashResult", propOrder = {"hashResult"} )
public class ArrayOfHashResult
{
	@XmlElement( name = "HashResult" )
	protected List<HashResult> hashResult;

	public List<HashResult> getHashResult()
	{
		if ( hashResult == null )
		{
			hashResult = new ArrayList();
		}
		return hashResult;
	}
}
