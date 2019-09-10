package com.marchnetworks.security.saml;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;

public class ConfigFactory
{
	private static Logger LOG = LoggerFactory.getLogger( ConfigFactory.class );
	private Resource configFile;

	public void init()
	{
		String fileName = configFile.getFilename();

		String serviceRootPath = System.getProperty( "user.dir" );
		String targetConfigPath = serviceRootPath + java.io.File.separator + fileName;

		try
		{
			FileSystemManager fsManager = VFS.getManager();
			FileObject configFileObject = fsManager.resolveFile( configFile.getURL().toString() );
			if ( configFileObject != null )
			{
				FileObject targetConfigFileObject = fsManager.resolveFile( "file:/" + targetConfigPath );

				if ( !targetConfigFileObject.exists() )
				{
					targetConfigFileObject.createFile();
					targetConfigFileObject.copyFrom( configFileObject, new AllFileSelector() );

					LOG.info( "Config file {} copied to file system.", fileName );
				}

				targetConfigFileObject.close();
			}
		}
		catch ( FileSystemException e )
		{
			throw new RuntimeException( e );
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
		}
		FileSystemManager fsManager;
	}

	public Resource getConfigFile()
	{
		return configFile;
	}

	public void setConfigFile( Resource configFile )
	{
		this.configFile = configFile;
	}
}

