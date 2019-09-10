package com.marchnetworks.command.common.extractor.data.image;

public class ImageInfo
{
	private Long rts;

	private String tag;

	public Long getRts()
	{
		return rts;
	}

	public void setRts( Long rts )
	{
		this.rts = rts;
	}

	public String getTag()
	{
		return tag;
	}

	public void setTag( String tag )
	{
		this.tag = tag;
	}

	public String toString()
	{
		return "Info [rts=" + rts + ", tag=" + tag + "]";
	}
}
