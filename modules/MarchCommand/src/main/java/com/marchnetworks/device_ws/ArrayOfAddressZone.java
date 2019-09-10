package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfAddressZone", propOrder = {"addressZone"} )
public class ArrayOfAddressZone
{
	@XmlElement( nillable = true )
	protected List<AddressZone> addressZone;

	public List<AddressZone> getAddressZone()
	{
		if ( addressZone == null )
		{
			addressZone = new ArrayList();
		}
		return addressZone;
	}
}
