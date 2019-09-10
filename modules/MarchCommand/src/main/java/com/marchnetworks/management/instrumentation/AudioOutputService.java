package com.marchnetworks.management.instrumentation;

import com.marchnetworks.management.instrumentation.model.AudioOutputEntity;

import java.util.List;

public abstract interface AudioOutputService extends DeviceOutputService
{
	public abstract List<AudioOutputEntity> getAll();

	public abstract AudioOutputEntity getById( Long paramLong );

	public abstract void createDeviceOutput( Long paramLong, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5 );

	public abstract void delete( Long paramLong );

	public abstract void deleteAll();
}

