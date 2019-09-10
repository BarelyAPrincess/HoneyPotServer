package com.marchnetworks.health.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.health.alerts.DeviceAlertEntity;

import java.util.List;

public interface TestDeviceAlertDAO extends GenericDAO<DeviceAlertEntity, Long>
{
	void batchInsert( List<DeviceAlertEntity> paramList, int paramInt );
}
