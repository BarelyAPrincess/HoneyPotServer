package com.marchnetworks.command.api.rest;

import java.io.IOException;
import java.io.InputStream;

public class CommandUpgradeInputStream extends InputStream
{
	private InputStream contentInputStream;
	private long contentSize;

	public CommandUpgradeInputStream( InputStream upgradeFileInputStream, long fileSize )
	{
		contentInputStream = upgradeFileInputStream;
		contentSize = fileSize;
	}

	public long getContentSize()
	{
		return contentSize;
	}

	public int read() throws IOException
	{
		return contentInputStream.read();
	}

	public int read( byte[] arg0 ) throws IOException
	{
		return contentInputStream.read( arg0 );
	}

	public int read( byte[] arg0, int arg1, int arg2 ) throws IOException
	{
		return contentInputStream.read( arg0, arg1, arg2 );
	}

	public long skip( long arg0 ) throws IOException
	{
		return contentInputStream.skip( arg0 );
	}

	public int available() throws IOException
	{
		return contentInputStream.available();
	}

	public void close() throws IOException
	{
		contentInputStream.close();
	}

	public synchronized void reset() throws IOException
	{
		contentInputStream.reset();
	}

	public boolean markSupported()
	{
		return contentInputStream.markSupported();
	}
}
