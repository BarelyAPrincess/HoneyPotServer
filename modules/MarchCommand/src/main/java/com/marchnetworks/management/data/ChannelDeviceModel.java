package com.marchnetworks.management.data;

public class ChannelDeviceModel
{
	private String modelName;

	private String modelId;

	private String subModelId;

	public ChannelDeviceModel()
	{
	}

	public ChannelDeviceModel( String modelName, String modelId, String subModelId )
	{
		this.modelName = modelName;
		this.modelId = modelId;
		this.subModelId = subModelId;
	}

	public boolean matches( ChannelDeviceModel device )
	{
		if ( ( modelId != null ) && ( subModelId != null ) )
		{
			if ( ( modelId.equals( device.getModelId() ) ) && ( subModelId.equals( device.getSubmodelId() ) ) )
			{
				return true;
			}
		}
		else if ( modelName.equalsIgnoreCase( device.getModelName() ) )
		{
			return true;
		}

		return false;
	}

	public String getModelName()
	{
		return modelName;
	}

	public String getModelId()
	{
		return modelId;
	}

	public String getSubmodelId()
	{
		return subModelId;
	}

	public void setModelName( String modelName )
	{
		this.modelName = modelName;
	}

	public void setModelId( String modelId )
	{
		this.modelId = modelId;
	}

	public void setSubmodelId( String submodelId )
	{
		subModelId = submodelId;
	}
}
