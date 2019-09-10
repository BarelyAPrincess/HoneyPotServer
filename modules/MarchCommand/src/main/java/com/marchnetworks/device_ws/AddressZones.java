package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "AddressZones", propOrder = {"localZone", "userAddressZones"} )
public class AddressZones
{
	@XmlElement( required = true )
	protected LocalZone localZone;
	protected ArrayOfAddressZone userAddressZones;

	public LocalZone getLocalZone()
	{
		return localZone;
	}

	public void setLocalZone( LocalZone value )
	{
		localZone = value;
	}

	public ArrayOfAddressZone getUserAddressZones()
	{
		return userAddressZones;
	}

	public void setUserAddressZones( ArrayOfAddressZone value )
	{
		userAddressZones = value;
	}
}
