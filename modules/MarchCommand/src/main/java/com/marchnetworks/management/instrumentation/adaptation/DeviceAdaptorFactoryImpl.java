package com.marchnetworks.management.instrumentation.adaptation;

import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceAdaptor;
import com.marchnetworks.management.instrumentation.DeviceAdaptorFactory;
import com.marchnetworks.management.instrumentation.RemoteDeviceOperations;

public class DeviceAdaptorFactoryImpl implements DeviceAdaptorFactory
{
	public <T extends RemoteDeviceOperations> T getDeviceAdaptor( DeviceResource deviceResource )
	{
		if ( deviceResource == null )
		{
			throw new RuntimeException( "getDeviceAdaptor called with null deviceResource" );
		}

		DeviceAdaptor adaptor = ( DeviceAdaptor ) ApplicationContextSupport.getBean( "compositeDeviceAdaptor" );

		adaptor.setDeviceResource( deviceResource );

		return ( T ) adaptor;
	}
}

