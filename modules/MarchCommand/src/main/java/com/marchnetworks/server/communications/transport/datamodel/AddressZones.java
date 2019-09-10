package com.marchnetworks.server.communications.transport.datamodel;

import com.marchnetworks.command.common.transport.data.AddressZone;
import com.marchnetworks.command.common.transport.data.LocalZone;

public class AddressZones
{
	private LocalZone localZone;
	private AddressZone[] userAddressZones;

	public LocalZone getLocalZone()
	{
		return localZone;
	}

	public void setLocalZone( LocalZone localZone )
	{
		this.localZone = localZone;
	}

	public AddressZone[] getUserAddressZones()
	{
		return userAddressZones;
	}

	public void setUserAddressZones( AddressZone[] userAddressZones )
	{
		this.userAddressZones = userAddressZones;
	}
}

