package io.amelia.foundation;

/**
 * Used to track event and task registration.
 */
public interface RegistrarBase
{
	/**
	 * Returns the name of the creator.
	 * <p>
	 * This should return the bare name of the creator and should be used for comparison.
	 *
	 * @return name of the creator
	 */
	String getName();

	/**
	 * Returns a value indicating whether or not this creator is currently enabled
	 *
	 * @return true if this creator is enabled, otherwise false
	 */
	default boolean isEnabled()
	{
		return true;
	}
}
