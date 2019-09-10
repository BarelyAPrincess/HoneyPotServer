package com.marchnetworks.management.firmware.model;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.management.firmware.data.FirmwareGroupEnum;
import com.marchnetworks.management.firmware.data.GroupFirmware;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "GROUPFIRMWARE" )
public class GroupFirmwareEntity implements Serializable
{
	private static final long serialVersionUID = 4462592576261715956L;
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Enumerated( EnumType.STRING )
	@Column( name = "GROUP_ID", nullable = false )
	private FirmwareGroupEnum groupId;
	@Column( name = "TARGET_FIRMWARE_ID" )
	private Long targetFirmwareId;

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public FirmwareGroupEnum getGroup()
	{
		return groupId;
	}

	public void setGroup( FirmwareGroupEnum group )
	{
		groupId = group;
	}

	public Long getTargetFirmwareId()
	{
		return targetFirmwareId;
	}

	public void setTargetFirmwareId( Long targetFirmwareId )
	{
		this.targetFirmwareId = targetFirmwareId;
	}

	public GroupFirmware toDataObject()
	{
		GroupFirmware groupFirmware = new GroupFirmware();
		groupFirmware.setGroup( groupId );
		if ( targetFirmwareId != null )
		{
			groupFirmware.setTargetFirmwareId( targetFirmwareId.toString() );
		}
		return groupFirmware;
	}

	public void fromDataObject( GroupFirmware groupFirmware )
	{
		groupId = groupFirmware.getGroup();
		if ( !CommonAppUtils.isNullOrEmptyString( groupFirmware.getTargetFirmwareId() ) )
		{
			targetFirmwareId = Long.valueOf( groupFirmware.getTargetFirmwareId() );
		}
		else
		{
			targetFirmwareId = null;
		}
	}
}

