package com.marchnetworks.common.file;

public class FileProperties
{
	public static final String PROPERTY_FILENAME = "FIRMWARE_FILENAME";

	public static final String PROPERTY_VERSION = "FIRMWARE_VERSION";

	public static final String PROPERTY_MINVERSION = "FIRMWARE_MINVERSION";

	public static final String PROPERTY_MAXVERSION = "FIRMWARE_MAXVERSION";

	public static final String PROPERTY_FAMILY = "FIRMWARE_MODEL";
	public static final String PROPERTY_MODEL = "FIRMWARE_TYPE";
	public static final String PROPERTY_FILETYPE = "FIRMWARE_FILETYPE";
	public static final String PROPERTY_DISPLAYVERSION = "FIRMWARE_DISPLAYVERSION";
	public static final String PROPERTY_VERSIONLIST = "FIRMWARE_VERSIONLIST";
	public static final String PROPERTY_AGENTVERSION = "FIRMWARE_AGENTVERSION";
	public static final String PROPERTY_UPGRADELIST = "FIRMWARE_UPGRADELIST";
	public static final String PROPERTY_RESTART = "FIRMWARE_RESTART";
	public static final String PROPERTY_MANUFACTURERID = "FIRMWARE_MANUFACTURERID";
	public static final String PROPERTY_CCMDEVICEMODELS = "FIRMWARE_CCMDEVICEMODELS";
	public static final String UPGRADE_CONFIGURATION_FILE_EXTENSION = ".xml";
	public static final String CERTIFICATE_ID = "CERTIFICATE_ID";
	public static final String CERTIFICATE_ISSUER = "CERTIFICATE_ISSUER";
	public static final String CERTIFICATE_SUBJECT = "CERTIFICATE_SUBJECT";

	public static final boolean isDeviceUpgradeMetadataFile( String input )
	{
		return input.toLowerCase().endsWith( ".xml" );
	}
}
