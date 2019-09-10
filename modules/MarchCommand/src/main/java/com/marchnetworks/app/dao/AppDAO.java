package com.marchnetworks.app.dao;

import com.marchnetworks.app.model.AppEntity;
import com.marchnetworks.command.common.dao.GenericDAO;

public abstract interface AppDAO extends GenericDAO<AppEntity, Long>
{
	public abstract AppEntity findByGuid( String paramString );
}
