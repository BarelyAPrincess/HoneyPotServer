package com.marchnetworks.health.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.health.alerts.AlertConfigEntity;

import java.util.List;

public class AlertConfigDAOImpl extends GenericHibernateDAO<AlertConfigEntity, Long> implements AlertConfigDAO
{
	public AlertConfigEntity getAlertConfig()
	{
		List<AlertConfigEntity> result = findAll();
		return result.size() > 0 ? ( AlertConfigEntity ) result.get( 0 ) : null;
	}
}
