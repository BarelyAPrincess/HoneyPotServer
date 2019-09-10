package com.marchnetworks.management.file.model;

public abstract interface FileStorageMBean
{
	public abstract Long getId();

	public abstract String getName();

	public abstract String getFileRepositoryPath();

	public abstract String getProperty( String paramString );
}

