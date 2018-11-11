/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http;

import org.bouncycastle.util.Arrays;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.support.ContentTypes;
import io.amelia.support.Encrypt;
import io.amelia.support.IO;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;

/**
 * Acts as the in between for uploaded files and the script
 */
public class UploadedFile
{
	private FileUpload cachedFileUpload = null;
	@Nonnull
	private String fileName;
	@Nonnegative
	private long fileSize;
	@Nonnull
	private Path uploadedFile;

	public UploadedFile( @Nonnull Path uploadedFile, @Nonnull String fileName, @Nonnegative long fileSize ) throws FileNotFoundException
	{
		if ( !Files.isRegularFile( uploadedFile ) )
			throw new FileNotFoundException( "File must be a regular file and exist!" );

		this.uploadedFile = uploadedFile;
		this.fileName = fileName;
		this.fileSize = fileSize;
	}

	public UploadedFile( @Nonnull FileUpload fileUpload ) throws IOException
	{
		cachedFileUpload = fileUpload;

		fileName = fileUpload.getFilename();
		fileSize = fileUpload.length();
		if ( !fileUpload.isInMemory() )
			uploadedFile = fileUpload.getFile().toPath();
		else
			uploadedFile = Paths.get( fileName ).resolve( DiskFileUpload.baseDirectory );
	}

	public String getEncoding()
	{
		return cachedFileUpload == null ? Charset.defaultCharset().displayName() : cachedFileUpload.getContentTransferEncoding();
	}

	public String getFileExtension()
	{
		return IO.getFileExtension( fileName );
	}

	public String getFileName()
	{
		return fileName;
	}

	public Path getFilePath() throws IOException
	{
		return uploadedFile;
	}

	public long getFileSize()
	{
		return fileSize;
	}

	public String getMD5Hash() throws IOException
	{
		return Encrypt.md5Hex( readToBytes() );
	}

	public String getMimeType()
	{
		return cachedFileUpload == null ? ContentTypes.getContentTypes( fileName ).findFirst().orElse( "null" ) : cachedFileUpload.getContentType();
	}

	public boolean isInMemory()
	{
		return cachedFileUpload != null && cachedFileUpload.isInMemory();
	}

	public byte[] readToBytes() throws IOException
	{
		if ( isInMemory() )
			return Arrays.clone( cachedFileUpload.get() );
		else if ( Files.isRegularFile( uploadedFile ) )
		{
			InputStream in = null;
			try
			{
				in = Files.newInputStream( uploadedFile );
				return IO.readStreamToBytes( in );
			}
			finally
			{
				IO.closeQuietly( in );
			}
		}
		else
			throw new IOException( "The uploaded file " + IO.relPath( uploadedFile ) + " does not exist nor was the file in memory!" );
	}

	public String readToString() throws IOException
	{
		return Encrypt.base64Encode( readToBytes() );
	}

	/**
	 * Returns the uploaded file path.
	 * Note: the file could be deleted as soon as the request finishes, be sure to copy the file to as safe location.
	 *
	 * @return The temporary file.
	 */
	public Path toFilePath() throws IOException
	{
		if ( !Files.isRegularFile( uploadedFile ) && isInMemory() )
		{
			uploadedFile = Paths.get( fileName ).resolve( DiskFileUpload.baseDirectory );
			OutputStream out = Files.newOutputStream( uploadedFile );
			out.write( cachedFileUpload.get() );
			IO.closeQuietly( out );
		}
		else if ( !Files.isRegularFile( uploadedFile ) )
			throw new IOException( "The uploaded file " + IO.relPath( uploadedFile ) + " does not exist nor was the file in memory!" );

		return uploadedFile;
	}

	public void toFilePath( Path newFilePath ) throws IOException
	{
		if ( isInMemory() )
		{
			OutputStream out = Files.newOutputStream( newFilePath );
			out.write( cachedFileUpload.get() );
			IO.closeQuietly( out );
		}
		else if ( Files.isRegularFile( uploadedFile ) )
			Files.copy( uploadedFile, newFilePath );
		else
			throw new IOException( "The uploaded file " + IO.relPath( uploadedFile ) + " does not exist nor was the file in memory!" );
	}

	@Override
	public String toString()
	{
		return "UploadedFile(size=" + fileSize + ",tmpFile=" + uploadedFile + ",fileName=" + fileName + ",mimeType=" + getMimeType() + ")";
	}
}
