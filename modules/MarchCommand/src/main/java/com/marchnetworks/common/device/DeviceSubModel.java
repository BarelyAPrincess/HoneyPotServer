package com.marchnetworks.common.device;

public class DeviceSubModel
{
	private String model;

	private String categoryName;

	private String subModel;

	private String subModelName;

	public DeviceSubModel()
	{
	}

	public DeviceSubModel( String model, String categoryName, String subModel, String subModelName )
	{
		this.model = model;
		this.categoryName = categoryName;
		this.subModel = subModel;
		this.subModelName = subModelName;
	}

	public String getCategoryName()
	{
		return categoryName;
	}

	public void setCategoryName( String categoryName )
	{
		this.categoryName = categoryName;
	}

	public String getModel()
	{
		return model;
	}

	public void setModel( String model )
	{
		this.model = model;
	}

	public String getSubModel()
	{
		return subModel;
	}

	public void setSubModel( String subModel )
	{
		this.subModel = subModel;
	}

	public String getSubModelName()
	{
		return subModelName;
	}

	public void setSubModelName( String subModelName )
	{
		this.subModelName = subModelName;
	}
}
