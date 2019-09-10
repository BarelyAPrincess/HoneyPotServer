package com.marchnetworks.management.file.service;

public enum FileStorageExceptionType
{
	NO_ERROR,
	FILE_NOT_FOUND,
	CREATE_ERROR,
	FILE_IN_USE;

	private FileStorageExceptionType()
	{
	}
}

