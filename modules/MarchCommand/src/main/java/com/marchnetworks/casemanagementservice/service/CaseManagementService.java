package com.marchnetworks.casemanagementservice.service;

import com.marchnetworks.casemanagementservice.common.CaseManagementException;
import com.marchnetworks.casemanagementservice.data.Case;

import java.util.List;

public abstract interface CaseManagementService
{
	public abstract Case createCase( Case paramCase, String paramString ) throws CaseManagementException;

	public abstract void removeCase( Long paramLong, String paramString ) throws CaseManagementException;

	public abstract Case getCase( Long paramLong, String paramString ) throws CaseManagementException;

	public abstract List<Case> getAllCases( String paramString );

	public abstract Case updateCase( Case paramCase, String paramString ) throws CaseManagementException;

	public abstract byte[] getCaseNodeAttachment( Long paramLong, String paramString ) throws CaseManagementException;

	public abstract String getAttachmentTag( Long paramLong );

	public abstract void removeGroupFromCases( Long paramLong );
}
