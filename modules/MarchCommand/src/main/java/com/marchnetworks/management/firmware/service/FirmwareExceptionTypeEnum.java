package com.marchnetworks.management.firmware.service;

public enum FirmwareExceptionTypeEnum
{
	DEVICE_NOT_FOUND,
	DEVICE_OFFLINE,
	DEVICE_BUSY,
	FIRMWARE_FILE_NOT_FOUND,
	DEVICE_FIRMWARE_NOT_MATCH,
	FIRMWARE_SCHEDULE_NOT_FOUND,
	DEVICE_COMMUNICATION_FAILURE,
	GROUP_UPDATE_NOT_FOUND,
	CAMERA_UPDATE_NOT_SUPPORTED;

	private FirmwareExceptionTypeEnum()
	{
	}
}

