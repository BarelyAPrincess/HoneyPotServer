package com.marchnetworks.management.filestorage;

public enum ChunkUploadError
{
	START_UPLOAD_HEADERS,
	FILE_EXISTS,
	SESSION_GENERATION,
	CONTINUE_UPLOAD_HEADERS,
	SESSION_NOT_FOUND,
	CHUNK_ORDER,
	CHUNK_HASH,
	FILE_HASH,
	FILE_STORAGE_ERROR,
	IMCOMPATIBLE_AGENT,
	WRONG_PROTOCOL;

	private ChunkUploadError()
	{
	}
}
