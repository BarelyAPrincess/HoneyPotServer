package com.marchnetworks.management.data;

import com.marchnetworks.command.api.rest.CommandUpgradeInputStream;

import java.io.InputStream;

public class FileObject
{
	private String name;
	private long fileLength;
	private InputStream inputStream;

	public FileObject()
	{
	}

	public FileObject( String name, long fileLength, InputStream inputStream )
	{
		this.name = name;
		this.fileLength = fileLength;
		this.inputStream = new CommandUpgradeInputStream( inputStream, fileLength );
	}

	public long getFileLength()
	{
		return fileLength;
	}

	public InputStream getInputStream()
	{
		return inputStream;
	}

	public String getName()
	{
		return name;
	}
}
