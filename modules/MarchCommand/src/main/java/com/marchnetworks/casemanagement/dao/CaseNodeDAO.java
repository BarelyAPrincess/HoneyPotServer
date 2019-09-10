package com.marchnetworks.casemanagement.dao;

import com.marchnetworks.casemanagement.model.CaseNodeEntity;
import com.marchnetworks.command.common.dao.GenericDAO;

import java.util.List;

public abstract interface CaseNodeDAO extends GenericDAO<CaseNodeEntity, Long>
{
	public abstract List<CaseNodeEntity> findByResourceId( Long paramLong );

	public abstract CaseNodeEntity findCaseNode( Long paramLong, boolean paramBoolean );

	public abstract CaseNodeEntity findByGuid( String paramString );

	public abstract List<CaseNodeEntity> findAllWithGuid();

	public abstract List<CaseNodeEntity> findAllBySerial( String paramString );
}
