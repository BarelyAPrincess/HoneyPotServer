package com.marchnetworks.health.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.health.alerts.AlertEntity;
import com.marchnetworks.health.search.AlertSearchQuery;

import java.util.List;

public interface AlertDAO extends GenericDAO<AlertEntity, Long>
{
	List<AlertEntity> findClosedAlertsByQuery( List<String> paramList1, List<String> paramList2, AlertSearchQuery paramAlertSearchQuery, int paramInt );

	int deleteClosedAlertsByClosedTime( long paramLong );
}
