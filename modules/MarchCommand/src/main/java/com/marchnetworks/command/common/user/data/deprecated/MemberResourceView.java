package com.marchnetworks.command.common.user.data.deprecated;

import javax.xml.bind.annotation.XmlElement;

@Deprecated
public class MemberResourceView
{
	private MemberResourceTypeEnum resourceType;
	private Long resourceId;

	public MemberResourceView()
	{
	}

	public MemberResourceView( MemberResourceTypeEnum aType, Long aResourceId )
	{
		resourceType = aType;
		resourceId = aResourceId;
	}

	@XmlElement( required = true )
	public MemberResourceTypeEnum getResourceType()
	{
		return resourceType;
	}

	public void setResourceType( MemberResourceTypeEnum resourceType )
	{
		this.resourceType = resourceType;
	}

	@XmlElement( required = true )
	public Long getResourceId()
	{
		return resourceId;
	}

	public void setResourceId( Long resourceId )
	{
		this.resourceId = resourceId;
	}
}
