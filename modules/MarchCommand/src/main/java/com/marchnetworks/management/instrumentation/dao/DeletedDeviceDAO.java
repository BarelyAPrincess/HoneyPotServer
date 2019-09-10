package com.marchnetworks.management.instrumentation.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.common.device.DeletedDevice;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;

public abstract interface DeletedDeviceDAO extends GenericDAO<DeletedDevice, Long>
{
	public abstract DeletedDevice findByPathAndDevice( String paramString, DeviceMBean paramDeviceMBean );
}

