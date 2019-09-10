package com.marchnetworks.common.id;

import com.marchnetworks.command.common.dao.GenericDAO;

public abstract interface IdDAO extends GenericDAO<IdEntity, Long>
{
	public abstract IdEntity findByTableName( String paramString );
}
