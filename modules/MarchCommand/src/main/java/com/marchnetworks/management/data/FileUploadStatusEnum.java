package com.marchnetworks.management.data;

public enum FileUploadStatusEnum
{
	UNKNOWN,
	OK,
	ERROR,
	FILE_EXISTS,
	IMCOMPATIBLE_AGENT,
	INVALID_FILE;

	private FileUploadStatusEnum()
	{
	}
}

