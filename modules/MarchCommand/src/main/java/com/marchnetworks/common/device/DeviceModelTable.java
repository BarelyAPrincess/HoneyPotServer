package com.marchnetworks.common.device;

import java.util.ArrayList;
import java.util.List;

public class DeviceModelTable
{
	private String defaultCategoryName;
	private String defaultModelName;
	private String defaultSubModelName;
	private List<DeviceModel> deviceModelList;

	public void addDeviceModel( DeviceModel deviceModel )
	{
		if ( deviceModelList == null )
		{
			deviceModelList = new ArrayList();
		}
		deviceModelList.add( deviceModel );
	}

	public List<DeviceModel> getDeviceModelList()
	{
		return deviceModelList;
	}

	public void setDeviceModelList( List<DeviceModel> aDeviceModelList )
	{
		deviceModelList = aDeviceModelList;
	}

	public String getDefaultCategoryName()
	{
		return defaultCategoryName;
	}

	public void setDefaultCategoryName( String defaultCategoryName )
	{
		this.defaultCategoryName = defaultCategoryName;
	}

	public String getDefaultModelName()
	{
		return defaultModelName;
	}

	public void setDefaultModelName( String defaultModelName )
	{
		this.defaultModelName = defaultModelName;
	}

	public String getDefaultSubModelName()
	{
		return defaultSubModelName;
	}

	public void setDefaultSubModelName( String defaultSubModelName )
	{
		this.defaultSubModelName = defaultSubModelName;
	}
}
