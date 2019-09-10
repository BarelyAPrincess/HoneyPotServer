package com.marchnetworks.management.firmware.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.management.firmware.data.UpdateStateEnum;
import com.marchnetworks.management.firmware.model.FirmwareEntity;

import java.util.List;

public abstract interface FirmwareDAO extends GenericDAO<FirmwareEntity, Long>
{
	public abstract FirmwareEntity findByDeviceId( String paramString );

	public abstract List<FirmwareEntity> findByScheduleId( Long paramLong );

	public abstract List<FirmwareEntity> findAllByState( UpdateStateEnum paramUpdateStateEnum );

	public abstract List<FirmwareEntity> findAllReadyUpgrades();

	public abstract List<FirmwareEntity> findAllUnfinishedUpgrade();

	public abstract boolean isFileAssociated( String paramString );

	public abstract boolean isScheduleInUse( Long paramLong );
}

