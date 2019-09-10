package com.marchnetworks.management.data;

import com.marchnetworks.common.event.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class FileStatusResult
{
	private List<Pair> properties;
	private FileUploadStatusEnum status;

	public void addProperty( Pair pair )
	{
		properties.add( pair );
	}

	public FileStatusResult( FileUploadStatusEnum aStatus )
	{
		status = aStatus;
		properties = new ArrayList();
	}

	public List<Pair> getProperties()
	{
		return properties;
	}

	public FileUploadStatusEnum getStatus()
	{
		return status;
	}

	public void setStatus( FileUploadStatusEnum status )
	{
		this.status = status;
	}
}
