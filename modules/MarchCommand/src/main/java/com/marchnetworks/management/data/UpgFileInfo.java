package com.marchnetworks.management.data;

import com.marchnetworks.common.utils.CommonUtils;

public class UpgFileInfo
{
	String fileName = null;
	String[] supportedModels = null;
	String minVersion = null;
	String maxVersion = null;
	String tarVersion = null;

	public UpgFileInfo( String fileName )
	{
		this.fileName = fileName;
	}

	public String getUpgFileName()
	{
		return fileName;
	}

	public void setSupportedModels( String strModels )
	{
		if ( strModels.contains( "models=" ) )
		{
			String[] tokens = strModels.split( "\"" );
			if ( tokens.length >= 2 )
			{
				supportedModels = tokens[1].split( "," );
			}
		}
	}

	public String[] getSupportedModels()
	{
		return supportedModels;
	}

	private String getFormattedVersion( String name, String rawValue )
	{
		if ( rawValue.contains( name ) )
		{
			String[] tokens = rawValue.split( "\"" );
			if ( tokens.length >= 2 )
			{
				return CommonUtils.convertVersionFormat( tokens[1] );
			}
		}
		return null;
	}

	public void setMinVersion( String rawMinVersion )
	{
		minVersion = getFormattedVersion( "minVersion", rawMinVersion );
	}

	public String getMinVersion()
	{
		return minVersion;
	}

	public void setMaxVersion( String rawMaxVersion )
	{
		maxVersion = getFormattedVersion( "maxVersion", rawMaxVersion );
	}

	public String getMaxVersion()
	{
		return maxVersion;
	}

	public void setTarVersion( String rawTarVersion )
	{
		tarVersion = getFormattedVersion( "version", rawTarVersion );
	}

	public String getTarVersion()
	{
		return tarVersion;
	}
}
