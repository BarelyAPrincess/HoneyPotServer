package com.marchnetworks.management.data;

import com.marchnetworks.common.utils.CommonUtils;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class RelInfo
{
	private String name = null;
	private String desc = null;
	private String version = null;
	Map<String, Properties> modelVersions = new Hashtable();
	Map<String, String> dvrTargetVersionByModel = new Hashtable();
	Map<String, String> zzreleaseTargetVersionByModel = new Hashtable();

	public void setName( String name )
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setDesc( String desc )
	{
		this.desc = desc;
	}

	public String getDesc()
	{
		return desc;
	}

	public void setVersion( String version )
	{
		this.version = version;
	}

	public String getVersion()
	{
		return version;
	}

	public void setModelTargetVersion( String model, Properties targetVersions )
	{
		modelVersions.put( model, targetVersions );
	}

	public Properties getTargetVersionsByModel( String model )
	{
		return ( Properties ) modelVersions.get( model );
	}

	public Map<String, Properties> getModelTargetVersions()
	{
		return modelVersions;
	}

	public void setDvrTargetVersionByModel( String model, String targetVersion )
	{
		dvrTargetVersionByModel.put( model, CommonUtils.convertVersionFormat( targetVersion ) );
	}

	public String getDvrTargetVersionByModel( String model )
	{
		Set<String> set = dvrTargetVersionByModel.keySet();
		String[] modelPatterns = ( String[] ) set.toArray( new String[set.size()] );
		modelPatterns = ( String[] ) dvrTargetVersionByModel.keySet().toArray( new String[set.size()] );
		String targetVersion = null;
		for ( String pattern : modelPatterns )
		{
			if ( CommonUtils.matchStringPattern( model, pattern ) )
			{
				targetVersion = ( String ) dvrTargetVersionByModel.get( pattern );
				break;
			}
		}

		return targetVersion;
	}

	public Map<String, String> getDvrModelVersionList()
	{
		return dvrTargetVersionByModel;
	}

	public void setZzreleaseTargetVersionByModel( String model, String targetVersion )
	{
		zzreleaseTargetVersionByModel.put( model, CommonUtils.convertVersionFormat( targetVersion ) );
	}

	public String getZzreleaseTargetVersionByModel( String model )
	{
		Set<String> set = zzreleaseTargetVersionByModel.keySet();
		String[] modelPatterns = ( String[] ) set.toArray( new String[set.size()] );
		modelPatterns = ( String[] ) zzreleaseTargetVersionByModel.keySet().toArray( new String[set.size()] );
		String targetVersion = null;
		for ( String pattern : modelPatterns )
		{
			if ( CommonUtils.matchStringPattern( model, pattern ) )
			{
				targetVersion = ( String ) zzreleaseTargetVersionByModel.get( pattern );
				break;
			}
		}

		return targetVersion;
	}

	public Map<String, String> getZzreleaseModelVersionList()
	{
		return zzreleaseTargetVersionByModel;
	}
}

