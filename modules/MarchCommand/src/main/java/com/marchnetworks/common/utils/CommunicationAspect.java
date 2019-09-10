package com.marchnetworks.common.utils;

import com.marchnetworks.common.types.DeviceExceptionTypes;

public abstract interface CommunicationAspect
{
	public abstract void setCommunicationError( boolean paramBoolean );

	public abstract boolean isCommunicationError();

	public abstract void setDetailedErrorMessage( String paramString );

	public abstract void setDetailedErrorType( DeviceExceptionTypes paramDeviceExceptionTypes );
}
