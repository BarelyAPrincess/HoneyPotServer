package io.amelia.foundation;

import java.util.HashMap;
import java.util.Map;

public class ApplicationRegistry
{
	private final static Map<ApplicationChannel, ApplicationRegistration> registrationMap = new HashMap<>();

	public static ApplicationRegistration getApplicationRegistration()
	{
		return registrationMap.computeIfAbsent( Foundation.getApplication(), k -> new ApplicationRegistration() );
	}

	public static ApplicationRegistration registerChannel( ApplicationChannel applicationChannel )
	{
		return registrationMap.computeIfAbsent( applicationChannel, k -> new ApplicationRegistration() );
	}

	public static void unregisterChannel( ApplicationChannel applicationChannel )
	{
		// TODO Mark registration as invalid!
		registrationMap.remove( applicationChannel );
	}

	private ApplicationRegistry()
	{
		// Static Access
	}

	/**
	 * Represents a point of contact for each {@link ApplicationRegistration}.
	 * e.g., Network Connection (remote) or Application instance (local).
	 */
	public interface ApplicationChannel
	{
		/**
		 * Does this represent a channel that is remote from this JVM instance, e.g, over network?
		 *
		 * @return True if so, otherwise false.
		 */
		default boolean isRemote()
		{
			return true;
		}
	}

	/**
	 * Provides a complete registration of receivers and sending available at each {@link ApplicationChannel}.
	 */
	private static class ApplicationRegistration
	{
		private final Map<String, Object> registered = new HashMap<>();
	}
}
