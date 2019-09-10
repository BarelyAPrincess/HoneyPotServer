package com.marchnetworks.health.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.health.alerts.DeviceAlertEntity;
import com.marchnetworks.health.search.AlertSearchQuery;

import java.util.List;

public interface DeviceAlertDAO extends GenericDAO<DeviceAlertEntity, Long>
{
	DeviceAlertEntity findAlert( String paramString1, String paramString2 );

	List<DeviceAlertEntity> findClosedAlertsByDeviceIds( String paramString, List<String> paramList );

	DeviceAlertEntity findByIdentifiers( String paramString1, String paramString2, String paramString3, long paramLong );

	List<DeviceAlertEntity> findUnresolvedAlertsByIdentifiers( String paramString1, String paramString2, String paramString3 );

	DeviceAlertEntity findUserOpenAlertByIdentifiers( String paramString1, String paramString2, String paramString3 );

	List<DeviceAlertEntity> findAllUserOpenAlertsByDevices( List<String> paramList );

	List<DeviceAlertEntity> findAllAlertsByDevice( String paramString );

	List<DeviceAlertEntity> findAllAlertsByRootDeviceAndSourceIdList( String paramString, List<String> paramList );

	List<DeviceAlertEntity> findAllAlertsByRootDeviceAndSourceId( String paramString1, String paramString2 );

	List<Long> findAllDeletedDeviceIds();

	List<String> findAllClosedDeviceIds( AlertSearchQuery paramAlertSearchQuery );

	List<String> findClosedNotReconciledAlertIdsByDeviceId( String paramString );

	List<DeviceAlertEntity> findTimeRestrictedAlertsByDevices( List<String> paramList, long paramLong1, long paramLong2 );
}
