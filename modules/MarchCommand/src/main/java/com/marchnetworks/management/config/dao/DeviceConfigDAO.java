package com.marchnetworks.management.config.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.management.config.DeviceImageState;
import com.marchnetworks.management.config.model.DeviceConfig;
import com.marchnetworks.management.config.model.DeviceImage;

import java.util.List;

public interface DeviceConfigDAO extends GenericDAO<DeviceConfig, Long>
{
	DeviceConfig findByDeviceId( String paramString );

	List<DeviceConfig> findAllByDeviceId( String paramString );

	List<DeviceConfig> findByImage( DeviceImage paramDeviceImage );

	List<DeviceConfig> findAllByImage( DeviceImage paramDeviceImage );

	List<DeviceConfig> findAllByAssignState( DeviceImageState... paramVarArgs );

	List<DeviceConfig> findAllByUpdateState( DeviceImageState... paramVarArgs );
}
