package com.marchnetworks.management.file.service;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.common.event.util.Pair;
import com.marchnetworks.common.file.FileProperties;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.common.utils.ServerUtils;
import com.marchnetworks.management.config.service.ConfigService;
import com.marchnetworks.management.data.ChannelDeviceModel;
import com.marchnetworks.management.data.FileStatusResult;
import com.marchnetworks.management.data.FileStorageView;
import com.marchnetworks.management.data.FileUploadStatusEnum;
import com.marchnetworks.management.data.UpdFileInfo;
import com.marchnetworks.management.file.FirmwareConfiguration;
import com.marchnetworks.management.file.dao.FileStorageDAO;
import com.marchnetworks.management.file.events.FileStorageEvent;
import com.marchnetworks.management.file.model.FileStorageEntity;
import com.marchnetworks.management.file.model.FileStorageMBean;
import com.marchnetworks.management.file.model.FileStoragePropertyEntity;
import com.marchnetworks.management.file.model.FileStorageType;
import com.marchnetworks.security.smartcard.SmartCardCertificateService;
import com.marchnetworks.server.event.EventRegistry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStorageServiceImpl implements FileStorageService
{
	private static Logger LOG = LoggerFactory.getLogger( FileStorageServiceImpl.class );
	private String repositoryRoot;
	private String repositoryPath;
	private FileStorageDAO fileStorageDAO;
	private EventRegistry eventRegistry;
	private ConfigService configService;

	private void createFolder( String folderPath )
	{
		File folder = new File( folderPath );
		if ( !folder.exists() )
		{
			folder.mkdirs();
		}
	}

	private String getRepositoryPath( FileStorageType category )
	{
		String serviceRootPath = System.getProperty( "user.dir" );
		repositoryPath = ( serviceRootPath + File.separator + repositoryRoot + File.separator + category.toString() );
		createFolder( repositoryPath );

		return repositoryPath;
	}

	public FileStorageView addFileStorage( String name, File f, List<Pair> properties ) throws FileStorageException
	{
		FileStorageView fs = null;

		if ( !f.exists() )
		{
			throw new FileStorageException( "Cannot AddFileStorage when file " + f.toString() + " doesn't exist" );
		}

		FileStorageType category = calculateCategory( name );

		String fileRepositoryPath = getRepositoryPath( category ) + File.separator + name;

		FileStorageEntity fileStorageObject = new FileStorageEntity( name, category );

		if ( category.equals( FileStorageType.CERTIFICATE ) )
		{
			try
			{
				fileStorageObject.setTheFile( FileUtils.readFileToByteArray( f ) );
				fileRepositoryPath = null;
			}
			catch ( IOException e )
			{
				throw new FileStorageException( "Couldn't move file /" + name + " to database" );
			}
			f.delete();
		}
		else if ( !f.renameTo( new File( fileRepositoryPath ) ) )
		{
			throw new FileStorageException( "Couldn't move file /" + name + " to correct repository location" );
		}

		fileStorageObject.setFileRepositoryPath( fileRepositoryPath );

		fileStorageDAO.create( fileStorageObject );

		fs = new FileStorageView( name );
		fs.setFileId( fileStorageObject.getId().toString() );
		fs.setCategory( category );

		if ( properties != null )
		{
			for ( Pair aPair : properties )
			{
				setFileStorageProperty( fs, aPair.getName(), aPair.getValue() );
			}
		}

		eventRegistry.sendEventAfterTransactionCommits( FileStorageEvent.newFileStorageAddedEvent( fs.getFileId(), fs.getCategory() ) );

		return fs;
	}

	public FileStorageType calculateCategory( String name ) throws FileStorageException
	{
		if ( name.contains( "." ) )
		{
			String fileExtension = name.substring( name.lastIndexOf( "." ) + 1 );
			if ( ( fileExtension.equalsIgnoreCase( "pem" ) ) || ( fileExtension.equalsIgnoreCase( "cer" ) ) )
				return FileStorageType.CERTIFICATE;
			if ( fileExtension.equalsIgnoreCase( "zip" ) )
			{
				return FileStorageType.FIRMWARE;
			}
		}

		throw new FileStorageException( "Unknown file extension." );
	}

	public FileStorageView createFileStorage( String name, InputStream inputStream ) throws FileStorageException
	{
		FileStorageView fileStorage = null;
		FileStorageType category = calculateCategory( name );
		String fileRepositoryPath = getRepositoryPath( category ) + File.separator + name;
		createFolder( getRepositoryPath( category ) );
		try
		{
			writeFile( fileRepositoryPath, inputStream );

			FileStorageEntity fileStorageObject = new FileStorageEntity( name, category );
			fileStorageObject.setFileRepositoryPath( fileRepositoryPath );

			fileStorageDAO.create( fileStorageObject );

			fileStorage = new FileStorageView( name );
			fileStorage.setFileId( fileStorageObject.getId().toString() );
		}
		catch ( IOException e )
		{
			LOG.debug( "error: createFileStorage::name=" + name );
			throw new FileStorageException( FileStorageExceptionType.CREATE_ERROR, e.getMessage() );
		}
		return fileStorage;
	}

	public FileStorageView createFileStorage( String name, InputStream inputStream, Boolean overwrite ) throws FileStorageException
	{
		FileStorageView fileStorage = null;
		if ( overwrite.booleanValue() )
		{
			FileStorageEntity fileStorageObject = findFileStorageObject( name );
			if ( fileStorageObject != null )
			{
				FileStorageType category = calculateCategory( name );
				String fileRepositoryPath = getRepositoryPath( category ) + File.separator + name;
				createFolder( getRepositoryPath( category ) );
				try
				{
					writeFile( fileRepositoryPath, inputStream );
				}
				catch ( IOException e )
				{
					LOG.debug( "error: createFileStorage::name=" + name );
					throw new FileStorageException( FileStorageExceptionType.CREATE_ERROR, e.getMessage() );
				}
				try
				{
					fileStorage = getFileStorage( fileStorageObject );
				}
				catch ( FileStorageException e )
				{
					throw new FileStorageException( FileStorageExceptionType.FILE_NOT_FOUND, e.getMessage() );
				}
			}
			else
			{
				fileStorage = createFileStorage( name, inputStream );
			}
		}
		else
		{
			fileStorage = createFileStorage( name, inputStream );
		}
		FileStorageEntity fileStorageObject = findFileStorageObject( name );
		fileStorage.setFileId( fileStorageObject.getId().toString() );
		return fileStorage;
	}

	private void writeFile( String fileRepositoryPath, InputStream inputStream ) throws IOException
	{
		File outFile = new File( fileRepositoryPath );
		FileOutputStream fileOutputStream = new FileOutputStream( outFile );

		int bytesRead = 1;
		int bufSize = 1024;
		byte[] buf = new byte[bufSize];

		while ( bytesRead > 0 )
		{
			bytesRead = inputStream.read( buf );
			if ( bytesRead > 0 )
				fileOutputStream.write( buf, 0, bytesRead );
		}
		fileOutputStream.close();
	}

	protected FileStorageEntity findFileStorageObject( String name )
	{
		try
		{
			return fileStorageDAO.findByNameCategory( name, calculateCategory( name ) );
		}
		catch ( FileStorageException e )
		{
		}
		return null;
	}

	public boolean isFileStorageExist( String name )
	{
		FileStorageEntity fso = findFileStorageObject( name );
		return fso != null;
	}

	public FileStorageView getFileStorageByName( String name ) throws FileStorageException
	{
		FileStorageEntity fileStorageObject = findFileStorageObject( name );
		return getFileStorage( fileStorageObject );
	}

	public FileStorageView getFileStorage( String fileObjectId ) throws FileStorageException
	{
		FileStorageEntity fileStorageObject = null;
		if ( fileObjectId != null )
		{
			try
			{
				fileStorageObject = fileStorageDAO.findById( Long.valueOf( fileObjectId ) );
			}
			catch ( NumberFormatException e )
			{
				LOG.info( "Number Format Exception when convert fileObjectId in getFileStorage. fileObjectId: {}.", fileObjectId );
			}
		}
		return getFileStorage( fileStorageObject );
	}

	public FileStorageMBean getFileStorageObject( String fileObjectId )
	{
		FileStorageEntity fileStorageObject = null;
		if ( fileObjectId != null )
		{
			fileStorageObject = fileStorageDAO.findById( Long.valueOf( fileObjectId ) );
		}
		return fileStorageObject;
	}

	private FileStorageView getFileStorage( FileStorageEntity fileStorageObject ) throws FileStorageException
	{
		FileStorageView fileStorage = null;
		if ( fileStorageObject != null )
		{
			fileStorage = new FileStorageView( fileStorageObject.getName() );
			fileStorage.setFileId( fileStorageObject.getId().toString() );
			fileStorage.setCategory( fileStorageObject.getCategory() );
			Map<String, FileStoragePropertyEntity> propertList = fileStorageObject.getProperties();

			Set<String> keySet = propertList.keySet();
			Iterator<String> keyList = keySet.iterator();
			String keyName = null;
			while ( keyList.hasNext() )
			{
				keyName = ( String ) keyList.next();
				FileStoragePropertyEntity fileStorageProperty = ( FileStoragePropertyEntity ) propertList.get( keyName );
				fileStorage.setProperty( keyName, fileStorageProperty.getPropertyValue() );
			}
			String fileRepositoryPath = fileStorageObject.getFileRepositoryPath();
			if ( fileRepositoryPath != null )
				fileStorage.setFile( new File( fileRepositoryPath ) );
			fileStorage.setTheBytes( fileStorageObject.getTheFile() );
		}
		else
		{
			throw new FileStorageException( FileStorageExceptionType.FILE_NOT_FOUND, "File not found." );
		}
		return fileStorage;
	}

	public void deleteFileStorage( String fileStorageId ) throws FileStorageException
	{
		if ( fileStorageId == null )
		{
			return;
		}
		FileStorageEntity fileStorageObject = fileStorageDAO.findById( Long.valueOf( fileStorageId ) );
		if ( fileStorageObject == null )
		{
			LOG.info( "File not found on Server File Store with Id =" + fileStorageId );
			throw new FileStorageException( FileStorageExceptionType.FILE_NOT_FOUND, "File not found on Server File Store with Id =" + fileStorageId );
		}

		String fileRepositoryPath = fileStorageObject.getFileRepositoryPath();
		if ( fileRepositoryPath != null )
		{
			File file = new File( fileRepositoryPath );
			if ( file.exists() )
			{
				boolean deleteSuccess = file.delete();
				if ( !deleteSuccess )
					throw new FileStorageException( FileStorageExceptionType.FILE_IN_USE, "File in use and could not be deleted with Id =" + fileStorageId );
			}
		}
		fileStorageDAO.delete( fileStorageObject );
		eventRegistry.sendEventAfterTransactionCommits( FileStorageEvent.newFileStorageRemovedEvent( fileStorageId, fileStorageObject.getCategory() ) );
	}

	public File getFile( String name ) throws FileStorageException
	{
		File file = null;
		FileStorageEntity fileStorageObject = findFileStorageObject( name );
		if ( fileStorageObject != null )
		{
			String fileRepositoryPath = fileStorageObject.getFileRepositoryPath();
			file = new File( fileRepositoryPath );
		}
		else
		{
			throw new FileStorageException( FileStorageExceptionType.FILE_NOT_FOUND, "File not found." );
		}
		return file;
	}

	public List<FileStorageView> getFileStorageByProperties( Pair... pairs )
	{
		boolean isMatch = true;
		List<FileStorageView> resultList = new ArrayList();
		try
		{
			List<FileStorageView> fileStorageList = getFileStorageList();

			for ( FileStorageView fileStorage : fileStorageList )
			{
				isMatch = true;
				for ( Pair aPair : pairs )
				{
					String aValue = fileStorage.getProperty( aPair.getName() );
					if ( !aValue.equals( aPair.getValue() ) )
					{
						isMatch = false;
						break;
					}
				}
				if ( isMatch )
				{
					resultList.add( fileStorage );
				}
			}
		}
		catch ( FileStorageException e )
		{
			LOG.debug( "Error retreiving file storage by properties: {}", e.getMessage() );
		}
		return resultList;
	}

	public FileStorageView getFirstMatchFileStorage( String targetVersion, DeviceView deviceView )
	{
		String manufacturerId = deviceView.getManufacturer();

		if ( targetVersion != null )
		{
			try
			{
				List<FileStorageView> fileStorageList = getFileStorageList();

				for ( FileStorageView fileStorage : fileStorageList )
				{
					boolean isMatch = false;
					String version = fileStorage.getProperty( "FIRMWARE_VERSION" );
					if ( targetVersion.equals( version ) )
					{

						if ( !deviceView.isR5() )
						{
							ChannelDeviceModel channelDev = new ChannelDeviceModel( deviceView.getModelName(), deviceView.getFamily(), deviceView.getModel() );

							String models = fileStorage.getProperty( "FIRMWARE_CCMDEVICEMODELS" );
							if ( models != null )
							{
								if ( ( manufacturerId == null ) || ( !manufacturerId.equals( fileStorage.getProperty( "FIRMWARE_MANUFACTURERID" ) ) ) )
								{
									continue;
								}

								List<ChannelDeviceModel> modelList = ( List ) CoreJsonSerializer.collectionFromJson( models, new TypeToken()
								{
								} );
								for ( ChannelDeviceModel channelDevice : modelList )
								{
									if ( channelDevice.matches( channelDev ) )
									{
										isMatch = true;
										break;
									}
								}
							}
							else if ( ( fileStorage.getProperty( "FIRMWARE_MODEL" ).equals( channelDev.getModelId() ) ) && ( fileStorage.getProperty( "FIRMWARE_TYPE" ).equals( channelDev.getSubmodelId() ) ) )
							{
								isMatch = true;
							}

						}
						else if ( ( fileStorage.getProperty( "FIRMWARE_MODEL" ).equals( deviceView.getFamily() ) ) && ( CommonUtils.checkDeviceModel( deviceView.getModel(), fileStorage.getProperty( "FIRMWARE_TYPE" ) ) == 1 ) )
						{
							isMatch = true;
						}

						if ( isMatch )
						{
							LOG.info( "Found match firmware file: {}", fileStorage.getFilename() );
							return fileStorage;
						}
					}
				}
			}
			catch ( FileStorageException e )
			{
				LOG.debug( "Error during retreiving file storage: {}", e.getMessage() );
			}
		}
		return null;
	}

	public void setFileStorageProperty( FileStorageView fileStorage, String propertyName, String propertyValue )
	{
		FileStorageEntity fileStorageObject = findFileStorageObject( fileStorage.getName() );
		if ( fileStorageObject != null )
		{
			fileStorageObject.setProperty( propertyName, propertyValue );
		}
	}

	public String getFileStorageProperty( FileStorageView fileStorage, String propertyName )
	{
		FileStorageEntity fileStorageObject = findFileStorageObject( fileStorage.getName() );
		String property = null;
		if ( fileStorageObject != null )
		{
			property = fileStorageObject.getProperty( propertyName );
		}
		return property;
	}

	public List<FileStorageView> getFileStorageList( FileStorageType category ) throws FileStorageException
	{
		List<FileStorageEntity> fileStorageObjectList = fileStorageDAO.findByCategory( category );
		List<FileStorageView> fileStorageList = new ArrayList();
		for ( FileStorageEntity fileStorageObject : fileStorageObjectList )
		{
			fileStorageList.add( getFileStorage( fileStorageObject ) );
		}
		return fileStorageList;
	}

	public FileStorageView[] getFileStorageListAsArray( FileStorageType category ) throws FileStorageException
	{
		return ( FileStorageView[] ) getFileStorageList( category ).toArray( new FileStorageView[0] );
	}

	public List<FileStorageView> getFileStorageList() throws FileStorageException
	{
		List<FileStorageEntity> fileStorageObjectList = fileStorageDAO.findAll();
		List<FileStorageView> fileStorageList = new ArrayList();
		for ( FileStorageEntity fileStorageObject : fileStorageObjectList )
		{
			fileStorageList.add( getFileStorage( fileStorageObject ) );
		}
		return fileStorageList;
	}

	public List<FileStorageView> getFileStorageListByProperty( String propertyName, String propertyValue ) throws FileStorageException
	{
		List<FileStorageView> resultList = new ArrayList();

		List<FileStorageView> fileStorageList = getFileStorageList();
		String propValue = null;
		for ( FileStorageView fileStorage : fileStorageList )
		{
			propValue = fileStorage.getProperty( propertyName );
			if ( ( propValue != null ) && ( propValue.equals( propertyValue ) ) )
				resultList.add( fileStorage );
		}
		return resultList;
	}

	public FileStatusResult validateFile( String fileName, InputStream inputStream, File aFile )
	{
		if ( isFileStorageExist( fileName ) )
		{
			return new FileStatusResult( FileUploadStatusEnum.FILE_EXISTS );
		}

		try
		{
			FileStorageType category = calculateCategory( fileName );
			FileStatusResult status = new FileStatusResult( FileUploadStatusEnum.OK );

			if ( !aFile.exists() )
			{
				status.setStatus( FileUploadStatusEnum.FILE_EXISTS );
				return status;
			}

			if ( !aFile.canRead() )
			{
				status.setStatus( FileUploadStatusEnum.ERROR );
				return status;
			}

			if ( category.equals( FileStorageType.FIRMWARE ) )
			{
				status = parseUpgradeFile( aFile, status );

				return compareTargetAgentWithServer( status );
			}

			if ( category.equals( FileStorageType.CERTIFICATE ) )
			{
				return parseCertificateFile( aFile, status );
			}
		}
		catch ( FileStorageException e )
		{
			return new FileStatusResult( FileUploadStatusEnum.INVALID_FILE );
		}

		return new FileStatusResult( FileUploadStatusEnum.ERROR );
	}

	private FileStatusResult parseCertificateFile( File f, FileStatusResult status )
	{
		try
		{
			SmartCardCertificateService certGenerator = ( SmartCardCertificateService ) ApplicationContextSupport.getBean( "certStringGenerator" );
			X509Certificate aCert = certGenerator.loadCertificate( FileUtils.readFileToByteArray( f ) );

			status.addProperty( new Pair( "CERTIFICATE_ID", new String( certGenerator.convertCertId( aCert.getSerialNumber().toByteArray() ) ) ) );
			status.addProperty( new Pair( "CERTIFICATE_ISSUER", aCert.getIssuerDN().getName() ) );
			status.addProperty( new Pair( "CERTIFICATE_SUBJECT", aCert.getSubjectDN().getName() ) );
			return status;
		}
		catch ( Exception e )
		{
			status.setStatus( FileUploadStatusEnum.ERROR );
		}
		return status;
	}

	private FileStatusResult compareTargetAgentWithServer( FileStatusResult status )
	{
		String serverVersionMajor = ServerUtils.getServerMajorVersion();
		List<Pair> properties = status.getProperties();

		for ( Pair property : properties )
		{
			if ( property.getName() == "FIRMWARE_AGENTVERSION" )
			{
				String agentVersion = property.getValue();
				if ( CommonAppUtils.isNullOrEmptyString( agentVersion ) )
				{
					break;
				}
				String agentVersionMajor = CommonUtils.getVersionPart( agentVersion, 2 );
				if ( CommonUtils.compareVersions( agentVersionMajor, serverVersionMajor ) != 1 )
					break;
				LOG.warn( "The target agent version {} is higher than the server version {}, abort the firmware upload...", agentVersionMajor, serverVersionMajor );
				status.setStatus( FileUploadStatusEnum.IMCOMPATIBLE_AGENT );
				break;
			}
		}

		return status;
	}

	private FileStatusResult parseUpgradeFile( File f, FileStatusResult status )
	{
		ZipFile zipFile = null;

		try
		{
			zipFile = new ZipFile( f );

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while ( entries.hasMoreElements() )
			{
				ZipEntry ze = ( ZipEntry ) entries.nextElement();
				String entry = CommonUtils.zip_genFileName( ze.getName() );

				if ( FileProperties.isDeviceUpgradeMetadataFile( entry ) )
				{

					FirmwareConfiguration fc = new FirmwareConfiguration();
					try
					{
						if ( fc.readFirmwareConfiguration( zipFile.getInputStream( ze ) ) )
						{
							status.addProperty( new Pair( "FIRMWARE_FILENAME", fc.getFileName() ) );
							status.addProperty( new Pair( "FIRMWARE_VERSION", fc.getVersion() ) );
							status.addProperty( new Pair( "FIRMWARE_MINVERSION", fc.getMinVersion() ) );
							status.addProperty( new Pair( "FIRMWARE_MAXVERSION", fc.getMaxVersion() ) );
							status.addProperty( new Pair( "FIRMWARE_MODEL", fc.getFamily() ) );
							status.addProperty( new Pair( "FIRMWARE_TYPE", fc.getModel() ) );
							status.addProperty( new Pair( "FIRMWARE_FILETYPE", fc.getFiletype() ) );
							status.addProperty( new Pair( "FIRMWARE_DISPLAYVERSION", fc.getDisplayversion() ) );
							status.addProperty( new Pair( "FIRMWARE_VERSIONLIST", fc.getVersionlist() ) );
							status.addProperty( new Pair( "FIRMWARE_AGENTVERSION", fc.getAgentVersion() ) );
							status.addProperty( new Pair( "FIRMWARE_UPGRADELIST", fc.getUpgradeListString() ) );
							status.addProperty( new Pair( "FIRMWARE_RESTART", fc.getRestart() ) );

							status.addProperty( new Pair( "FIRMWARE_MANUFACTURERID", fc.getManufacturerId() ) );
							status.addProperty( new Pair( "FIRMWARE_CCMDEVICEMODELS", fc.getCCMDeviceModels() ) );
						}
						else
						{
							status.setStatus( FileUploadStatusEnum.INVALID_FILE );
						}
						return status;
					}
					catch ( Exception e )
					{
						status.setStatus( FileUploadStatusEnum.INVALID_FILE );
					}
				}
			}

			status.setStatus( FileUploadStatusEnum.INVALID_FILE );

			return status;
		}
		catch ( Exception e )
		{
			status.setStatus( FileUploadStatusEnum.INVALID_FILE );
		}
		finally
		{
			try
			{
				if ( zipFile != null )
				{
					zipFile.close();
				}
			}
			catch ( IOException localIOException3 )
			{
			}
		}

		return null;
	}

	public boolean fileInUseCheck( String fileStorageId )
	{
		return fileStorageDAO.usersHaveFile( fileStorageId );
	}

	public UpdFileInfo getUpdFileInfo( String fileProertyName, String fileRepositoryPath )
	{
		return new UpdFileInfo( fileProertyName, fileRepositoryPath );
	}

	public void setFileStorageDAO( FileStorageDAO fileStorageObjectDao )
	{
		fileStorageDAO = fileStorageObjectDao;
	}

	public void setRepositoryRoot( String repositoryRoot )
	{
		this.repositoryRoot = repositoryRoot;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public ConfigService getConfigurationService()
	{
		if ( configService == null )
		{
			configService = ( ( ConfigService ) ApplicationContextSupport.getBean( "configService_internal" ) );
		}
		return configService;
	}
}

