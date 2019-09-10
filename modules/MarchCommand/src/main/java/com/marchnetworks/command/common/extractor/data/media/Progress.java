package com.marchnetworks.command.common.extractor.data.media;

public class Progress
{
	private int Percent;

	private long Bytes;

	public int getPercent()
	{
		return Percent;
	}

	public void setPercent( int percent )
	{
		Percent = percent;
	}

	public long getBytes()
	{
		return Bytes;
	}

	public void setBytes( long bytes )
	{
		Bytes = bytes;
	}
}
