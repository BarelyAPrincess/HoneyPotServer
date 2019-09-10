package com.marchnetworks.common.transport.datamodel;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.topology.ExpectedException;
import com.marchnetworks.common.types.DeviceExceptionTypes;
import com.marchnetworks.common.utils.CommunicationAspect;

import javax.net.ssl.SSLHandshakeException;

public class DeviceException extends Exception implements CommunicationAspect, ExpectedException
{
	private static final long serialVersionUID = 952216223880985510L;
	private boolean communicationError;
	private String detailedErrorMessage;
	private DeviceExceptionTypes detailedErrorType;

	public DeviceException()
	{
		detailedErrorType = DeviceExceptionTypes.UNKNOWN;
	}

	public DeviceException( String message )
	{
		super( message );
		detailedErrorType = resolveErrorType( message );
	}

	public DeviceException( String msg, DeviceExceptionTypes t )
	{
		super( msg );
		detailedErrorType = t;
	}

	public DeviceException( Throwable cause )
	{
		super( cause );
		detailedErrorType = resolveErrorType( cause );
		detailedErrorMessage = cause.getMessage();
	}

	public DeviceException( String message, Throwable cause )
	{
		super( message, cause );
		detailedErrorType = resolveErrorType( message );
		detailedErrorMessage = message;
	}

	public void setCommunicationError( boolean communicationError )
	{
		this.communicationError = communicationError;
	}

	public boolean isCommunicationError()
	{
		return communicationError;
	}

	public void setDetailedErrorMessage( String detailedErrorMessage )
	{
		this.detailedErrorMessage = detailedErrorMessage;
	}

	public String getDetailedErrorMessage()
	{
		return detailedErrorMessage;
	}

	public DeviceExceptionTypes getDetailedErrorType()
	{
		return detailedErrorType;
	}

	public void setDetailedErrorType( DeviceExceptionTypes detailedErrorType )
	{
		this.detailedErrorType = detailedErrorType;
	}

	private static DeviceExceptionTypes resolveErrorType( String message )
	{
		if ( CommonAppUtils.isNullOrEmptyString( message ) )
		{
			return DeviceExceptionTypes.UNKNOWN;
		}
		if ( message.equalsIgnoreCase( "not_found" ) )
			return DeviceExceptionTypes.INVALID_DEVICE_SUBSCRIPTION;
		if ( ( message.toLowerCase().contains( "method name or namespace not recognized" ) ) || ( message.equalsIgnoreCase( "not_implemented" ) ) )
		{
			return DeviceExceptionTypes.FUNCTION_NOT_IMPLEMENTED;
		}
		return DeviceExceptionTypes.UNKNOWN;
	}

	private static DeviceExceptionTypes resolveErrorType( Throwable exception )
	{
		if ( ( ( exception instanceof SSLHandshakeException ) ) || ( ( exception.getCause() instanceof SSLHandshakeException ) ) )
		{
			return DeviceExceptionTypes.DEVICE_CERTIFICATE_NOT_TRUSTED;
		}
		return DeviceExceptionTypes.UNKNOWN;
	}
}
