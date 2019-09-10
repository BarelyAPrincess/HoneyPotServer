package com.marchnetworks.management.instrumentation;

import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.server.communications.transport.datamodel.ChannelDetails;
import com.marchnetworks.server.communications.transport.datamodel.ConfigurationEnvelope;
import com.marchnetworks.server.communications.transport.datamodel.DeviceDetails;

import java.io.InputStream;

public abstract interface RemoteDeviceOperations
{
	public abstract DeviceDetails retrieveInfo() throws DeviceException;

	public abstract String upgrade( String paramString1, InputStream paramInputStream, String paramString2 ) throws DeviceException;

	public abstract String configure( byte[] paramArrayOfByte ) throws DeviceException;

	public abstract ConfigurationEnvelope retrieveConfiguration() throws DeviceException;

	public abstract ChannelDetails retrieveChannelDetails( String paramString ) throws DeviceException;

	public abstract String retrieveConfigurationHash() throws DeviceException;
}

