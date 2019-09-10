package com.marchnetworks.casemanagement.dao;

import com.marchnetworks.casemanagement.model.CaseEntity;
import com.marchnetworks.command.common.dao.GenericDAO;

import java.util.List;

public abstract interface CaseDAO extends GenericDAO<CaseEntity, Long>
{
	public abstract List<CaseEntity> getAllCases( String paramString, boolean paramBoolean );

	public abstract List<CaseEntity> getAllCases( String paramString, List<Long> paramList );

	public abstract List<CaseEntity> getAllByGroupId( Long paramLong );

	public abstract List<CaseEntity> getAllOrphan();
}
