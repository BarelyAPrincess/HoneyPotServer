package io.amelia.foundation;

public abstract class NaggableRegistrar extends VendorRegistrar
{
	private boolean isNaggable;

	public NaggableRegistrar( Class<?> cls )
	{
		super( cls );
	}

	public boolean isNaggable()
	{
		return isNaggable;
	}

	public void setNaggable( boolean isNaggable )
	{
		this.isNaggable = isNaggable;
	}
}
