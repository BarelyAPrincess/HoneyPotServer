package com.marchnetworks.notification.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.notification.model.NotificationEntity;

import java.util.List;

public abstract interface NotificationDAO extends GenericDAO<NotificationEntity, Long>
{
	public abstract List<NotificationEntity> findAllByGroupAndAppId( String paramString1, String paramString2 );

	public abstract boolean existsByGroupAppIdAndName( String paramString1, String paramString2, String paramString3 );

	public abstract void deleteByAppId( String paramString );

	public abstract List<NotificationEntity> findAllByUsername( String paramString );
}

