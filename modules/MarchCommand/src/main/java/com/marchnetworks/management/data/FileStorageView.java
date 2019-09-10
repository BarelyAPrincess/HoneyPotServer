package com.marchnetworks.management.data;

import com.marchnetworks.common.file.FileProperties;
import com.marchnetworks.management.file.model.FileStorageType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.annotation.XmlElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStorageView
{
	private static Logger LOG = LoggerFactory.getLogger( FileStorageView.class );
	private String name;
	private String fileId = null;
	private String status = null;
	private List<FileStorageProperty> propertyList = new ArrayList<FileStorageProperty>();
	private Map<String, FileStorageProperty> propertyMap = new HashMap<String, FileStorageProperty>();
	private long fileLength;
	private long checksum;
	private File file;
	private FileStorageType category;
	private byte[] theBytes;
	private String upgName = null;

	public FileStorageView()
	{

	}

	public FileStorageView( String name )
	{
		this.name = name;
	}

	@XmlElement
	public String getName()
	{
		return name;
	}

	public void setProperty( String propertyName, String propertyValue )
	{
		FileStorageProperty fileStorageProperty = null;
		if ( propertyMap.containsKey( propertyName ) )
		{
			fileStorageProperty = ( FileStorageProperty ) propertyMap.get( propertyName );
			fileStorageProperty.setValue( propertyValue );
		}
		else
		{
			fileStorageProperty = new FileStorageProperty( propertyName, propertyValue );
			propertyMap.put( propertyName, fileStorageProperty );
			propertyList.add( fileStorageProperty );
		}
	}

	public String getProperty( String propertyName )
	{
		String propertyValue = null;
		if ( propertyMap.containsKey( propertyName ) )
		{
			FileStorageProperty fileStorageProperty = ( FileStorageProperty ) propertyMap.get( propertyName );
			propertyValue = fileStorageProperty.getValue();
		}
		return propertyValue;
	}

	@XmlElement
	public List<FileStorageProperty> getPropertyList()
	{
		return propertyList;
	}

	@XmlElement
	public long getFileLength()
	{
		return fileLength;
	}

	public void setFile( File file )
	{
		this.file = file;
		checksum = 0L;
		if ( file.exists() )
		{
			fileLength = file.length();
		}
		else
		{
			fileLength = 0L;
		}
	}

	public File getFile()
	{
		return file;
	}

	public InputStream getInputStream() throws FileNotFoundException
	{
		InputStream inputStream = null;
		if ( file != null )
		{
			inputStream = new FileInputStream( file );
		}
		return inputStream;
	}

	@XmlElement
	@Deprecated
	public long getChecksum()
	{
		return checksum;
	}

	private byte[] createMD5Checksum( File file ) throws Exception
	{
		InputStream inputStream = new FileInputStream( file );
		byte[] buffer = new byte['Ѐ'];
		MessageDigest md5MessageDigest = MessageDigest.getInstance( "MD5" );
		int numberRead = 0;
		do
		{
			numberRead = inputStream.read( buffer );
			if ( numberRead > 0 )
			{
				md5MessageDigest.update( buffer, 0, numberRead );
			}
		}
		while ( numberRead != -1 );
		inputStream.close();
		return md5MessageDigest.digest();
	}

	public String getMD5Checksum() throws Exception
	{
		byte[] md5ChecksumBytes = createMD5Checksum( file );
		String result = "";
		for ( int i = 0; i < md5ChecksumBytes.length; i++ )
		{
			result = result + Integer.toString( ( md5ChecksumBytes[i] & 0xFF ) + 256, 16 ).substring( 1 );
		}

		return result;
	}

	@XmlElement
	public String getFileId()
	{
		return fileId;
	}

	public void setFileId( String fileId )
	{
		this.fileId = fileId;
	}

	public void setStatus( String status )
	{
		this.status = status;
	}

	public String getStatus()
	{
		return status;
	}

	public FileObject getFileObject()
	{
		File file = getFile();
		FileObject firmwareFile = null;
		try
		{
			ZipFile zipFile = new ZipFile( file );
			if ( zipFile != null )
			{
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while ( entries.hasMoreElements() )
				{
					ZipEntry zipEntry = ( ZipEntry ) entries.nextElement();
					String enrtyName = getFileName( zipEntry.getName() );
					if ( upgName != null )
					{
						if ( enrtyName.equalsIgnoreCase( upgName ) )
						{
							firmwareFile = new FileObject( enrtyName, zipEntry.getSize(), zipFile.getInputStream( zipEntry ) );
							break;
						}
					}
					else if ( !FileProperties.isDeviceUpgradeMetadataFile( enrtyName ) )
					{
						firmwareFile = new FileObject( enrtyName, zipEntry.getSize(), zipFile.getInputStream( zipEntry ) );
						break;
					}
				}
			}
		}
		catch ( IOException e )
		{
			LOG.error( e.getMessage() );
		}
		return firmwareFile;
	}

	public String getFilename()
	{
		File file = getFile();
		String firmwareFile = null;
		try
		{
			ZipFile zipFile = new ZipFile( file );
			if ( zipFile != null )
			{
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while ( entries.hasMoreElements() )
				{
					ZipEntry zipEntry = ( ZipEntry ) entries.nextElement();
					String enrtyName = getFileName( zipEntry.getName() );
					if ( !FileProperties.isDeviceUpgradeMetadataFile( enrtyName ) )
					{
						firmwareFile = enrtyName;
						break;
					}
				}
				zipFile.close();
			}
		}
		catch ( IOException e )
		{
			LOG.error( e.getMessage() );
		}
		return firmwareFile;
	}

	private String getFileName( String filePath )
	{
		String fileName = filePath;
		int pos = filePath.lastIndexOf( "/" );
		if ( ( pos > 0 ) && ( pos < filePath.length() ) )
		{
			fileName = filePath.substring( pos + 1 );
		}
		return fileName;
	}

	@XmlElement( required = true )
	public FileStorageType getCategory()
	{
		return category;
	}

	public void setCategory( FileStorageType category )
	{
		this.category = category;
	}

	public byte[] getTheBytes()
	{
		return theBytes;
	}

	public void setTheBytes( byte[] theBytes )
	{
		this.theBytes = theBytes;
	}

	public void setUpgName( String upgName )
	{
		this.upgName = upgName;
	}

	public String getUpgName()
	{
		return upgName;
	}
}
