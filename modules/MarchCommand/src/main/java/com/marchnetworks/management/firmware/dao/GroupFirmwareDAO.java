package com.marchnetworks.management.firmware.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.management.firmware.data.FirmwareGroupEnum;
import com.marchnetworks.management.firmware.model.GroupFirmwareEntity;

public abstract interface GroupFirmwareDAO extends GenericDAO<GroupFirmwareEntity, Long>
{
	public abstract GroupFirmwareEntity findByGroup( FirmwareGroupEnum paramFirmwareGroupEnum );

	public abstract boolean isFileAssociated( String paramString );
}

