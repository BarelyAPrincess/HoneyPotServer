package com.marchnetworks.command.api.user.rightexport;

public class UserRightExportTemplate
{
	private String header;

	private String[] targetRights;

	private String appId;

	private DisplayType displayType;

	public static enum DisplayType
	{
		YES_NO,
		VIEW_EDIT,
		TWO_STATE;

		private DisplayType()
		{
		}
	}

	public UserRightExportTemplate( String header, String viewRight, String editRight, String appId )
	{
		this.header = header;
		this.appId = appId;
		displayType = DisplayType.VIEW_EDIT;

		targetRights = new String[2];
		targetRights[0] = viewRight;
		targetRights[1] = editRight;
	}

	public UserRightExportTemplate( String header, String targetRight, String appId )
	{
		this.header = header;
		this.appId = appId;
		displayType = DisplayType.YES_NO;

		targetRights = new String[1];
		targetRights[0] = targetRight;
	}

	public UserRightExportTemplate( String header, String targetRight, String assignedValue, String notAssignedValue, String appId )
	{
		this.header = header;
		this.appId = appId;
		displayType = DisplayType.TWO_STATE;

		targetRights = new String[3];
		targetRights[0] = targetRight;
		targetRights[1] = assignedValue;
		targetRights[2] = notAssignedValue;
	}

	public String getHeader()
	{
		return header;
	}

	public String getAppId()
	{
		return appId;
	}

	public DisplayType getDisplayType()
	{
		return displayType;
	}

	public String getTargetRight()
	{
		return targetRights[0];
	}

	public String getViewTargetRight()
	{
		return targetRights[0];
	}

	public String getEditTargetRight()
	{
		return targetRights[1];
	}

	public String getRightAssignedValue()
	{
		return targetRights[1];
	}

	public String getRightNotAssignedValue()
	{
		return targetRights[2];
	}
}

