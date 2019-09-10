package com.marchnetworks.web.util.compression;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CompressedResponseWrapper extends HttpServletResponseWrapper
{
	protected HttpServletResponse originalResponse = null;
	protected GZIPServletOutputStream stream = null;

	public CompressedResponseWrapper( HttpServletResponse response )
	{
		super( response );
		originalResponse = response;
	}

	public void finishResponse()
	{
		try
		{
			if ( ( stream != null ) && ( !stream.closed() ) )
			{
				stream.close();
			}
		}
		catch ( IOException localIOException )
		{
		}
	}

	public void setContentLength( int length )
	{
	}

	public void flushBuffer() throws IOException
	{
		stream.flush();
	}

	public ServletOutputStream getOutputStream() throws IOException
	{
		if ( stream == null )
		{
			stream = new GZIPServletOutputStream( originalResponse );
		}
		return stream;
	}

	public PrintWriter getWriter() throws IOException
	{
		throw new IllegalStateException( "getWriter() is not implemented on CompressedResponseWrapper" );
	}
}
