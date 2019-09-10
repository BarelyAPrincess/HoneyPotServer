package com.marchnetworks.management.data;

import com.marchnetworks.common.utils.CommonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdFileInfo
{
	private static final Logger LOG = LoggerFactory.getLogger( UpdFileInfo.class );
	private final String FILE_TYPE_UPDATE = "rel";
	private String name;
	private String repositoryPath;
	private List<UpgFileInfo> upgList = new ArrayList();
	private RelInfo relInfo = new RelInfo();
	private long firmwareId;
	private String fileType;
	private String targetVersion;
	private List<String> versionList = new ArrayList();

	static enum ReadRelState
	{
		Read_General,
		Read_Model,
		Read_TargetVersions;

		private ReadRelState()
		{
		}
	}

	static enum ReadUpgState
	{
		Read_Info,
		Read_Model,
		Read_Versions,
		Read_DvrVersions,
		Read_done;

		private ReadUpgState()
		{
		}
	}

	public UpdFileInfo( String name, String repositoryPath )
	{
		this.name = name;
		this.repositoryPath = repositoryPath;

		ZipFile zipFile = null;
		try
		{
			File f = new File( repositoryPath );
			zipFile = new ZipFile( f );

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while ( entries.hasMoreElements() )
			{
				ZipEntry ze = ( ZipEntry ) entries.nextElement();
				String eName = ze.getName();

				if ( eName.toLowerCase().endsWith( "rel" ) )
				{
					readRelFile( zipFile, ze );
				}
				else if ( eName.toLowerCase().endsWith( "upg" ) )
				{
					getUpgInfoList( zipFile, ze );
				}
			}
			return;
		}
		catch ( IOException e )
		{
			LOG.error( e.getMessage() );
		}
		finally
		{
			if ( zipFile != null )
			{
				try
				{
					zipFile.close();
				}
				catch ( IOException e )
				{
					LOG.error( e.getMessage() );
				}
			}
		}
	}

	private KeyValue getKeyValue( String line )
	{
		KeyValue keyValue = new KeyValue();

		String[] tokens = line.split( "=" );
		if ( tokens.length == 2 )
		{
			keyValue.key = tokens[0];
			keyValue.value = tokens[1].replace( "\"", "" );
		}

		return keyValue;
	}

	private void readRelFile( ZipFile zipFile, ZipEntry ze ) throws IOException
	{
		ReadRelState readState = ReadRelState.Read_General;
		String eName = ze.getName();
		if ( eName.toLowerCase().endsWith( "rel" ) )
		{
			InputStream is = zipFile.getInputStream( ze );
			InputStreamReader reader = new InputStreamReader( is );
			try
			{
				BufferedReader bin = new BufferedReader( reader );
				String line = bin.readLine();
				boolean readModel = false;
				String modelName = null;
				boolean bGetTargetVersions = false;
				int bracketLevel = 1;

				while ( line != null )
				{
					switch ( readState )
					{
						case Read_General:
							if ( line.contains( "name" ) )
							{
								KeyValue kv = getKeyValue( line );
								relInfo.setName( kv.value );
							}
							else if ( line.contains( "desc" ) )
							{
								KeyValue kv = getKeyValue( line );
								relInfo.setDesc( kv.value );
							}
							else if ( ( !readModel ) && ( line.contains( "versions" ) ) )
							{
								readState = ReadRelState.Read_Model;
							}
							else if ( ( !readModel ) && ( line.contains( "version" ) ) )
							{
								KeyValue kv = getKeyValue( line );
								relInfo.setVersion( kv.value );
							}
							break;

						case Read_Model:
							if ( bracketLevel == 1 )
							{
								String[] tokens = line.split( "=" );
								modelName = tokens[0].trim().replace( "\"", "" );
							}
							else if ( bracketLevel == 2 )
							{
								if ( line.contains( "versions" ) )
								{
									bGetTargetVersions = true;
								}
							}
							else if ( ( bracketLevel == 3 ) && ( bGetTargetVersions ) )
							{
								String[] tokens = line.split( "=" );
								if ( tokens.length == 2 )
								{
									if ( tokens[0].trim().equalsIgnoreCase( "dvr" ) )
									{
										relInfo.setDvrTargetVersionByModel( modelName, tokens[1].replace( "\"", "" ) );
									}
									else if ( tokens[0].trim().equalsIgnoreCase( "zzrelease" ) )
									{
										relInfo.setZzreleaseTargetVersionByModel( modelName, tokens[1].replace( "\"", "" ) );
									}
								}
							}

							if ( line.contains( "[" ) )
							{
								bracketLevel++;
							}
							else if ( line.contains( "]" ) )
							{
								bracketLevel--;
								if ( ( bracketLevel == 2 ) && ( bGetTargetVersions ) )
								{
									bGetTargetVersions = false;
								}
							}

							break;
					}

					line = bin.readLine();
				}
				if ( bin != null )
				{
					bin.close();
				}
			}
			finally
			{
				if ( reader != null )
				{
					reader.close();
				}
				is.close();
			}
		}
	}

	private void getUpgInfoList( ZipFile zipFile, ZipEntry ze ) throws IOException
	{
		ReadUpgState readState = ReadUpgState.Read_Info;
		String eName = ze.getName();
		if ( eName.toLowerCase().endsWith( "upg" ) )
		{
			InputStream is = zipFile.getInputStream( ze );

			UpgFileInfo upgInfo = new UpgFileInfo( eName );
			try
			{
				BufferedReader bin = new BufferedReader( new InputStreamReader( is ) );

				String line = bin.readLine();
				int maxLinesRead = 100;
				int itemsFound = 0;
				int lineCount = 0;

				while ( line != null )
				{
					switch ( readState )
					{
						case Read_Info:
							if ( line.toLowerCase().startsWith( "info" ) )
							{
								readState = ReadUpgState.Read_Model;
							}

							break;
						case Read_Model:
							if ( line.contains( "models" ) )
							{
								upgInfo.setSupportedModels( line );
							}
							else if ( line.contains( "versions" ) )
							{
								readState = ReadUpgState.Read_Versions;
							}

							break;
						case Read_Versions:
							if ( line.toLowerCase().contains( "dvr=" ) )
							{
								readState = ReadUpgState.Read_DvrVersions;
							}

							break;
						case Read_DvrVersions:
							if ( line.contains( "maxVersion" ) )
							{
								itemsFound++;
								upgInfo.setMaxVersion( line );
							}
							else if ( line.contains( "minVersion" ) )
							{
								itemsFound++;
								upgInfo.setMinVersion( line );
							}
							else if ( line.contains( "version" ) )
							{
								itemsFound++;
								upgInfo.setTarVersion( line );
							}

							if ( itemsFound == 3 )
							{
								readState = ReadUpgState.Read_done;
							}

							break;
					}

					lineCount++;
					if ( ( lineCount >= maxLinesRead ) || ( readState == ReadUpgState.Read_done ) )
						break;
					line = bin.readLine();
				}
			}
			finally
			{
				is.close();
			}

			upgList.add( upgInfo );
		}
	}

	public String getName()
	{
		return name;
	}

	public String getRepositoryPath()
	{
		return repositoryPath;
	}

	public List<UpgFileInfo> getUpgList()
	{
		return upgList;
	}

	private boolean isApplicableModel( UpgFileInfo upg, String deviceModel )
	{
		ArrayList<String> modelList = new ArrayList( Arrays.asList( upg.getSupportedModels() ) );
		if ( modelList.contains( deviceModel ) )
		{
			return true;
		}
		return false;
	}

	public UpgFileInfo getNextUpg( String deviceVersion, String deviceModel )
	{
		for ( UpgFileInfo upg : upgList )
		{
			if ( ( isApplicableModel( upg, deviceModel ) ) && ( CommonUtils.compareVersions( upg.getMinVersion(), deviceVersion ) <= 0 ) && ( CommonUtils.compareVersions( upg.getMaxVersion(), deviceVersion ) >= 0 ) )
			{

				return upg;
			}
		}

		return null;
	}

	public RelInfo getRelInfo()
	{
		return relInfo;
	}

	public void setFirmwareId( long firmwareId )
	{
		this.firmwareId = firmwareId;
	}

	public long getFirmwareId()
	{
		return firmwareId;
	}

	public String getFileType()
	{
		return fileType;
	}

	public void setFileType( String fileType )
	{
		this.fileType = fileType;
	}

	public List<String> getVersionListe()
	{
		return versionList;
	}

	public void setTargetVersion( String targetVersion )
	{
		this.targetVersion = targetVersion;
	}

	public String getTargetVersion( String deviceModel )
	{
		if ( fileType.equalsIgnoreCase( "rel" ) )
		{
			return relInfo.getZzreleaseTargetVersionByModel( deviceModel );
		}
		return targetVersion;
	}

	public void setVersionListe( String versions )
	{
		String[] versionTokens = versions.split( "," );
		for ( String version : versionTokens )
		{
			versionList.add( version );
		}
	}

	class KeyValue
	{
		String key;
		String value;

		KeyValue()
		{
		}
	}
}
