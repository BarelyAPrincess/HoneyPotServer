package com.marchnetworks.common.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CompressionUtils
{
	private static final Logger LOG = LoggerFactory.getLogger( CompressionUtils.class );

	public static byte[] unzipFileToBytes( String zipFilename, String filename )
	{
		try
		{
			InputStream zipInputStream = null;
			ZipFile zipFile = null;
			try
			{
				zipFile = new ZipFile( zipFilename );
				ZipEntry zipEntry = zipFile.getEntry( filename );

				if ( zipEntry == null )
				{
					return null;
				}
				zipInputStream = zipFile.getInputStream( zipEntry );

				byte[] result = IOUtils.toByteArray( zipInputStream );

				return result;
			}
			finally
			{
				if ( zipInputStream != null )
				{
					zipInputStream.close();
				}
				if ( zipFile != null )
				{
					zipFile.close();
				}
			}
		}
		catch ( IOException e )
		{
			LOG.error( "Error reading zip file " + zipFilename, e );
		}

		return null;
	}

	public static boolean unzipFilesToDisk( String zipFilename, String outputDirectory, String... filenames )
	{
		boolean result = true;
		try
		{
			File folder = new File( outputDirectory );
			if ( !folder.exists() )
			{
				folder.mkdir();
			}

			ZipFile zipFile = null;
			try
			{
				zipFile = new ZipFile( zipFilename );

				for ( int i = 0; i < filenames.length; i++ )
				{
					if ( filenames[i] != null )
					{

						ZipEntry zipEntry = zipFile.getEntry( filenames[i] );

						if ( zipEntry == null )
						{
							result = false;
						}
						else
						{
							File newFile = new File( outputDirectory + File.separator + zipEntry.getName() );
							new File( newFile.getParent() ).mkdirs();

							InputStream zipInputStream = zipFile.getInputStream( zipEntry );

							byte[] buffer = new byte[' '];

							FileOutputStream fos = new FileOutputStream( outputDirectory + File.separator + zipEntry.getName() );
							BufferedOutputStream outputStream = null;
							try
							{
								outputStream = new BufferedOutputStream( fos, buffer.length );
								int len;
								while ( ( len = zipInputStream.read( buffer ) ) != -1 )
								{
									outputStream.write( buffer, 0, len );
								}
							}
							finally
							{
							}
						}
					}
				}
			}
			finally
			{
				if ( zipFile != null )
				{
					zipFile.close();
				}
			}
		}
		catch ( IOException e )
		{
			result = false;
			LOG.error( "Error unzipping zip file " + zipFilename + " into " + outputDirectory, e );
		}
		return result;
	}

	public static byte[] compress( byte[] rawData )
	{
		if ( ( rawData == null ) || ( rawData.length == 0 ) )
		{
			return rawData;
		}

		int bufferSize = Math.min( rawData.length, 8192 );
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( bufferSize );
		DeflaterOutputStream deflaterOutStream = new DeflaterOutputStream( outputStream );

		byte[] buffer = new byte[bufferSize];
		ByteArrayInputStream inputStream = new ByteArrayInputStream( rawData );
		try
		{
			int len;
			while ( ( len = inputStream.read( buffer ) ) != -1 )
			{
				deflaterOutStream.write( buffer, 0, len );
			}
			deflaterOutStream.close();
			inputStream.close();
			return outputStream.toByteArray();
		}
		catch ( IOException e )
		{
			LOG.error( "Error while writing data to compression stream" );
		}
		return null;
	}

	public static byte[] decompress( byte[] compressedData )
	{
		if ( ( compressedData == null ) || ( compressedData.length == 0 ) )
		{
			return compressedData;
		}

		ByteArrayInputStream inputStream = new ByteArrayInputStream( compressedData );
		InflaterInputStream inflaterInputStream = new InflaterInputStream( inputStream );

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		byte[] buffer = new byte['Ѐ'];
		try
		{
			int len;
			while ( ( len = inflaterInputStream.read( buffer ) ) != -1 )
			{
				outputStream.write( buffer, 0, len );
			}
			outputStream.close();
			inflaterInputStream.close();
			return outputStream.toByteArray();
		}
		catch ( IOException e )
		{
			LOG.error( "Error while reading data from decompression stream" );
		}
		return null;
	}
}

