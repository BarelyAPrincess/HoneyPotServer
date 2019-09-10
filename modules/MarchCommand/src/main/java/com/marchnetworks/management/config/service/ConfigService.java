package com.marchnetworks.management.config.service;

import com.marchnetworks.common.configuration.ConfigSettings;
import com.marchnetworks.management.config.DeviceImageState;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;

public interface ConfigService
{
	DeviceImageDescriptor createImage( String paramString1, String paramString2, String paramString3 ) throws ConfigurationException;

	DeviceConfigDescriptor getDeviceConfig( String paramString ) throws ConfigurationException;

	boolean isDeviceConfigSnapShotExist( String paramString ) throws ConfigurationException;

	DeviceConfigDescriptor getDeviceConfigByDeviceId( String paramString ) throws ConfigurationException;

	DeviceConfigDescriptor[] getAllDeviceConfig() throws ConfigurationException;

	void updateConfigSnapShotSerial( CompositeDevice paramCompositeDevice ) throws ConfigurationException;

	DeviceImageDescriptor[] listImages() throws ConfigurationException;

	DeviceImageDescriptor getImage( String paramString ) throws ConfigurationException;

	void applyReplacement( String paramString ) throws ConfigurationException;

	void applyImage( String paramString1, String paramString2 );

	void applyImages( ConfigView[] paramArrayOfConfigView );

	DeviceImageState getDeviceImageStatus( String paramString );

	void unassignImage( String paramString ) throws ConfigurationException;

	DeviceImageDescriptor editImage( String paramString1, String paramString2, String paramString3 ) throws ConfigurationException;

	DeviceImageDescriptor deleteImage( String paramString ) throws ConfigurationException;

	void performApplyImageTask( String paramString1, String paramString2, int paramInt );

	ConfigSettings getConfigurationSettings();

	void setConfigurationSettings( ConfigSettings paramConfigSettings ) throws ConfigurationException;

	boolean isAssociatedDeviceConfigurationOnProgress( String paramString );
}
