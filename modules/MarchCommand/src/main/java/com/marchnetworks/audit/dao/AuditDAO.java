package com.marchnetworks.audit.dao;

import com.marchnetworks.audit.data.AuditSearchQuery;
import com.marchnetworks.audit.model.AuditEntity;
import com.marchnetworks.command.common.dao.GenericDAO;

import java.util.List;

public abstract interface AuditDAO<T extends AuditEntity> extends GenericDAO<T, Long>
{
	public abstract List<T> getAudits( AuditSearchQuery paramAuditSearchQuery );

	public abstract T findAuditByTag( String paramString );

	public abstract int deleteOldAudits( long paramLong );

	public abstract List<T> findByResourceId( Long paramLong );

	public abstract void deleteByAppId( String paramString );
}
