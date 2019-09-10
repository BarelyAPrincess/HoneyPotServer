package com.marchnetworks.management.communications;

import java.util.List;

public interface CommunicationsService
{
	List<DeviceDiscoverView> discoverDevices( int paramInt ) throws CommunicationsException;
}
