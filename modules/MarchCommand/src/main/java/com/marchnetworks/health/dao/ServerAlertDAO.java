package com.marchnetworks.health.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.health.alerts.ServerAlertEntity;

import java.util.List;

public interface ServerAlertDAO extends GenericDAO<ServerAlertEntity, Long>
{
	List<ServerAlertEntity> findAllUserOpenAlertsByServer( String paramString );

	ServerAlertEntity findUserOpenAlertByIdentifiers( String paramString1, String paramString2, String paramString3 );

	List<ServerAlertEntity> findUnresolvedAlertsByIdentifiers( String paramString1, String paramString2, String paramString3 );
}
