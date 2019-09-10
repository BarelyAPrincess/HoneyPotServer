package com.marchnetworks.casemanagementservice.common;

public class CaseManagementException extends Exception
{
	private CaseManagementExceptionTypeEnum error;

	public CaseManagementException( CaseManagementExceptionTypeEnum type, String message )
	{
		super( message );
	}

	public CaseManagementException( Throwable cause )
	{
		super( cause );
	}

	public CaseManagementException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public CaseManagementExceptionTypeEnum getError()
	{
		return error;
	}
}
