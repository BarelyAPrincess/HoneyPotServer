package com.marchnetworks.casemanagement.dao;

import com.marchnetworks.casemanagement.model.LocalGroupEntity;
import com.marchnetworks.command.common.dao.GenericDAO;

import java.util.List;

public abstract interface LocalGroupDAO extends GenericDAO<LocalGroupEntity, Long>
{
	public abstract List<LocalGroupEntity> findByUser( String paramString );

	public abstract List<Long> findIdsByUser( String paramString );

	public abstract LocalGroupEntity findLocalGroupByName( String paramString );
}
