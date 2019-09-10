package com.marchnetworks.audit.common;

public class AuditLogException extends Exception
{
	private AuditLogExceptionTypeEnum error;

	private String message;

	public AuditLogException( AuditLogExceptionTypeEnum error, String message )
	{
		this.error = error;
	}

	public AuditLogExceptionTypeEnum getError()
	{
		return error;
	}

	public String getMessage()
	{
		return message;
	}
}
