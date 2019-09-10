package com.marchnetworks.health.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.health.alerts.AlertConfigEntity;

public interface AlertConfigDAO extends GenericDAO<AlertConfigEntity, Long>
{
	AlertConfigEntity getAlertConfig();
}
