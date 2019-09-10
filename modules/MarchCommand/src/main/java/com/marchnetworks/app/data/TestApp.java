package com.marchnetworks.app.data;

public class TestApp
{
	private String filePath;

	private boolean installed;

	public TestApp( String filePath, boolean installed )
	{
		this.filePath = filePath;
		this.installed = installed;
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath( String filePath )
	{
		this.filePath = filePath;
	}

	public boolean isInstalled()
	{
		return installed;
	}

	public void setInstalled( boolean installed )
	{
		this.installed = installed;
	}
}
