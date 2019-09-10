package com.marchnetworks.common.device;

import java.util.ArrayList;
import java.util.List;

public class DeviceModel
{
	private String model;
	private String modelName;
	private List<DeviceSubModel> deviceSubModelList;

	public DeviceModel()
	{
	}

	public DeviceModel( String model, String modelName )
	{
		this.model = model;
		this.modelName = modelName;
	}

	public String getModel()
	{
		return model;
	}

	public void setModel( String model )
	{
		this.model = model;
	}

	public String getModelName()
	{
		return modelName;
	}

	public void setModelName( String modelName )
	{
		this.modelName = modelName;
	}

	public List<DeviceSubModel> getDeviceSubModelList()
	{
		return deviceSubModelList;
	}

	public void setDeviceSubModelList( List<DeviceSubModel> deviceSubModelList )
	{
		this.deviceSubModelList = deviceSubModelList;
	}

	public void addDeviceSubModel( DeviceSubModel deviceSubModel )
	{
		if ( deviceSubModelList == null )
		{
			deviceSubModelList = new ArrayList();
		}
		deviceSubModelList.add( deviceSubModel );
	}
}
