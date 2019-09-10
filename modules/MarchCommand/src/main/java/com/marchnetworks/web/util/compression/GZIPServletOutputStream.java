package com.marchnetworks.web.util.compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nonnull;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

public class GZIPServletOutputStream extends ServletOutputStream
{
	protected ByteArrayOutputStream byteOutputStream = null;
	protected GZIPOutputStream gzipOutputStream = null;
	protected boolean isClosed = false;
	protected ServletOutputStream servletOutputStream = null;
	protected HttpServletResponse servletResponse = null;
	private WriteListener writeListener;

	public GZIPServletOutputStream( HttpServletResponse response ) throws IOException
	{
		isClosed = false;
		servletResponse = response;
		servletOutputStream = response.getOutputStream();
		byteOutputStream = new ByteArrayOutputStream();
		gzipOutputStream = new GZIPOutputStream( byteOutputStream );
	}

	public void close() throws IOException
	{
		if ( isClosed )
		{
			return;
		}
		isClosed = true;

		gzipOutputStream.close();

		byte[] decompressedBytes = byteOutputStream.toByteArray();

		servletResponse.setContentLength( decompressedBytes.length );
		servletResponse.addHeader( "Content-Encoding", "gzip" );
		servletOutputStream.write( decompressedBytes );
		servletOutputStream.flush();
		servletOutputStream.close();
	}

	public boolean closed()
	{
		return isClosed;
	}

	public void flush() throws IOException
	{
		if ( isClosed )
		{
			IOException e = new IOException( "Write called on a closed GZIPServletOutputStream output stream" );
			writeListener.onError( e );
			throw e;
		}

		gzipOutputStream.flush();
	}

	@Override
	public boolean isReady()
	{
		return true;
	}

	public void reset()
	{
	}

	@Override
	public void setWriteListener( WriteListener writeListener )
	{
		this.writeListener = writeListener;
	}

	public void write( @Nonnull byte[] bytes, int offset, int length ) throws IOException
	{
		if ( isClosed )
		{
			IOException e = new IOException( "Write called on a closed GZIPServletOutputStream output stream" );
			writeListener.onError( e );
			throw e;
		}

		gzipOutputStream.write( bytes, offset, length );
	}

	public void write( int b ) throws IOException
	{
		if ( isClosed )
		{
			IOException e = new IOException( "Write called on a closed GZIPServletOutputStream output stream" );
			writeListener.onError( e );
			throw e;
		}

		gzipOutputStream.write( ( byte ) b );
	}

	public void write( @Nonnull byte[] bytes ) throws IOException
	{
		write( bytes, 0, bytes.length );
	}
}
