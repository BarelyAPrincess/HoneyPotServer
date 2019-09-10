package com.marchnetworks.management.topology.util;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.types.DeviceExceptionTypes;

public class TopologyExceptionTranslator
{
	public static TopologyException translateDeviceException( DeviceException ex )
	{
		if ( ex.getDetailedErrorType().equals( DeviceExceptionTypes.DEVICE_NOT_FOUND ) )
			return new TopologyException( TopologyExceptionTypeEnum.DEVICE_NOT_FOUND );
		if ( ex.getDetailedErrorType().equals( DeviceExceptionTypes.INVALID_DEVICE_ADDRESS ) )
		{
			return new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST );
		}
		if ( ex.getDetailedErrorType().equals( DeviceExceptionTypes.DEVICE_ALREADY_REGISTERED_WITH_THIS_SERVER ) )
			return new TopologyException( TopologyExceptionTypeEnum.DEVICE_ALREADY_REGISTERED );
		if ( ex.getDetailedErrorType().equals( DeviceExceptionTypes.STATION_ID_ALREADY_EXISTS ) )
			return new TopologyException( TopologyExceptionTypeEnum.STATION_ID_ALREADY_EXISTS );
		if ( ex.getDetailedErrorType().equals( DeviceExceptionTypes.DEVICE_NOT_MARKED_FOR_REPLACEMENT ) )
			return new TopologyException( TopologyExceptionTypeEnum.DEVICE_NOT_MARKED_FOR_REPLACEMENT );
		if ( ex.getDetailedErrorType().equals( DeviceExceptionTypes.DEVICE_FIRMWARE_VERSION_TOO_LOW ) )
			return new TopologyException( TopologyExceptionTypeEnum.DEVICE_FIRMWARE_VERSION_TOO_LOW );
		if ( ex.getDetailedErrorType().equals( DeviceExceptionTypes.DEVICE_CONFIGURATION_SNAPSHOT_NOT_EXISTS ) )
		{
			return new TopologyException( TopologyExceptionTypeEnum.DEVICE_CONFIGURATION_SNAPSHOT_NOT_EXISTS );
		}

		return new TopologyException( TopologyExceptionTypeEnum.INTERNAL_ERROR );
	}
}

