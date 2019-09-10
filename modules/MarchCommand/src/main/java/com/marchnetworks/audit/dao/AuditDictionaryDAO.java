package com.marchnetworks.audit.dao;

import com.marchnetworks.audit.model.AuditDictionaryEntity;
import com.marchnetworks.command.common.dao.GenericDAO;

import java.util.Map;
import java.util.Set;

public abstract interface AuditDictionaryDAO extends GenericDAO<AuditDictionaryEntity, Integer>
{
	public abstract Set<Integer> findMissingKeys( Set<Integer> paramSet );

	public abstract Map<Integer, AuditDictionaryEntity> findValues( Set<Integer> paramSet );

	public abstract void deleteUnreferencedKeys();
}
