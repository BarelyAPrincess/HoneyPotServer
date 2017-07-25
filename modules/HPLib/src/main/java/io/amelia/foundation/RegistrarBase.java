package io.amelia.foundation;

/**
 * Used to track event and task registration.
 */
public abstract class RegistrarBase
{
	public static final RegistrarBase INTERNAL = new RegistrarBase()
	{
		@Override
		public String getName()
		{
			return "HoneyPotServer";
		}
	};

	private Class<?> cls;

	public RegistrarBase()
	{

	}

	public RegistrarBase( Class<?> cls )
	{
		this.cls = cls;
	}

	/**
	 * Returns the name of the creator.
	 * <p>
	 * This should return the bare name of the creator and should be used for comparison.
	 *
	 * @return name of the creator
	 */
	public String getName()
	{
		return cls == null ? null : cls.getSimpleName();
	}

	/**
	 * Returns a value indicating whether or not this creator is currently enabled
	 *
	 * @return true if this creator is enabled, otherwise false
	 */
	public boolean isEnabled()
	{
		return true;
	}

	protected void setClass( Class<?> cls )
	{
		this.cls = cls;
	}
}
