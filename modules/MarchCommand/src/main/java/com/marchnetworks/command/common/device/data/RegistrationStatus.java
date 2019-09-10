package com.marchnetworks.command.common.device.data;

public enum RegistrationStatus
{
	INITIAL,
	REGISTERED,
	ERROR_REGISTRATION,
	PENDING_REGISTRATION,
	UNREGISTERED,
	MARKED_FOR_REPLACEMENT,
	PENDING_REPLACEMENT,
	ERROR_REPLACEMENT,
	DEVICE_NOT_FOUND;

	private static RegistrationStatus[] REGISTERED_STATUSES = {REGISTERED, MARKED_FOR_REPLACEMENT, ERROR_REPLACEMENT, PENDING_REPLACEMENT};

	public static RegistrationStatus getRegistrationStatusFromString( String registrationStatus )
	{
		RegistrationStatus status = null;
		try
		{
			status = valueOf( registrationStatus );
		}
		catch ( IllegalArgumentException localIllegalArgumentException )
		{
		}

		return status;
	}

	public static boolean isRegisteredStatus( RegistrationStatus status )
	{
		return ( REGISTERED == status ) || ( MARKED_FOR_REPLACEMENT == status ) || ( ERROR_REPLACEMENT == status ) || ( PENDING_REPLACEMENT == status );
	}

	public static RegistrationStatus[] getRegisteredStatuses()
	{
		return REGISTERED_STATUSES;
	}
}
