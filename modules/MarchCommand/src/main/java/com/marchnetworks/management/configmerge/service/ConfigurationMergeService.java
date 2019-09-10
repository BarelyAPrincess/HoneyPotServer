package com.marchnetworks.management.configmerge.service;

import com.marchnetworks.management.config.DeviceSnapshotState;

public interface ConfigurationMergeService
{
	DeviceSnapshotState compareConfig( String paramString1, String paramString2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2 );

	byte[] mergeConfig( String paramString1, String paramString2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2 );
}
