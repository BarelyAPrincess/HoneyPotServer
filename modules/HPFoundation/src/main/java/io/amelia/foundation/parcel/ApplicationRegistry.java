package io.amelia.foundation.parcel;

import java.util.HashMap;
import java.util.Map;

import io.amelia.foundation.Foundation;

public class ApplicationRegistry
{
	private final static Map<ParcelChannel, ApplicationRegistration> registrationMap = new HashMap<>();

	public static ApplicationRegistration getApplicationRegistration()
	{
		return registrationMap.computeIfAbsent( Foundation.getApplication(), k -> new ApplicationRegistration() );
	}

	public static ApplicationRegistration registerChannel( ParcelChannel applicationChannel )
	{
		return registrationMap.computeIfAbsent( applicationChannel, k -> new ApplicationRegistration() );
	}

	public static void unregisterChannel( ParcelChannel applicationChannel )
	{
		// TODO Mark registration as invalid!
		registrationMap.remove( applicationChannel );
	}

	private ApplicationRegistry()
	{
		// Static Access
	}
}
