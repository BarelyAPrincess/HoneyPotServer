package com.marchnetworks.license.exception;

import javax.xml.ws.WebFault;

@WebFault( name = "LicenseFault" )
public class LicenseException extends Exception
{
	private static final long serialVersionUID = 7529207529407296223L;
	private LicenseExceptionType faultCode;

	public LicenseException( String message, LicenseExceptionType faultCode )
	{
		super( message );
		this.faultCode = faultCode;
	}

	public LicenseException( String msg, LicenseExceptionType faultCode, Exception inner )
	{
		super( msg, inner );
		this.faultCode = faultCode;
	}

	public LicenseExceptionType getFaultCode()
	{
		return faultCode;
	}

	public void setFaultCode( LicenseExceptionType faultCode )
	{
		this.faultCode = faultCode;
	}
}
