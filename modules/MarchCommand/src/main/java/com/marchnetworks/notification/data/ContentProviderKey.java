package com.marchnetworks.notification.data;

import com.marchnetworks.command.common.CommonAppUtils;

import java.util.Arrays;

public class ContentProviderKey
{
	private String groupId;
	private String appId;

	public ContentProviderKey( String groupId, String appId )
	{
		this.groupId = groupId;
		this.appId = appId;
	}

	public String getGroupId()
	{
		return groupId;
	}

	public void setGroupId( String groupId )
	{
		this.groupId = groupId;
	}

	public String getAppId()
	{
		return appId;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}

	public boolean equals( Object object )
	{
		if ( this == object )
		{
			return true;
		}
		if ( ( object instanceof ContentProviderKey ) )
		{
			ContentProviderKey other = ( ContentProviderKey ) object;
			return ( groupId.equals( other.getGroupId() ) ) && ( CommonAppUtils.equalsWithNull( appId, other.getAppId() ) );
		}
		return false;
	}

	public int hashCode()
	{
		Object[] values = {groupId, appId};
		return Arrays.hashCode( values );
	}

	public String toString()
	{
		return "groupId: " + groupId + ", appId:" + appId;
	}
}

