package com.marchnetworks.command.common.extractor.data.image;

public class Extension extends AdditionalTarget
{
	private String extension;

	public Extension( String extension )
	{
		this.extension = extension;
	}

	public String getExtension()
	{
		return extension;
	}

	public void setExtension( String extension )
	{
		this.extension = extension;
	}
}
