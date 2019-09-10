package com.marchnetworks.casemanagementservice.service;

import com.marchnetworks.casemanagementservice.data.LocalGroup;
import com.marchnetworks.casemanagementservice.data.LocalGroupException;

import java.util.List;

public abstract interface LocalGroupService
{
	public abstract LocalGroup create( LocalGroup paramLocalGroup ) throws LocalGroupException;

	public abstract List<LocalGroup> getAll();

	public abstract List<LocalGroup> getAllByIds( List<Long> paramList );

	public abstract List<LocalGroup> getAllByUser( String paramString );

	public abstract List<Long> getAllIdsByUser( String paramString );

	public abstract LocalGroup update( LocalGroup paramLocalGroup ) throws LocalGroupException;

	public abstract void delete( Long paramLong ) throws LocalGroupException;

	public abstract boolean isUserMemberOf( String paramString, List<Long> paramList );
}
