package io.amelia.foundation.binding;

public class SharedNamespace implements ReadableBinding
{
	private final String baseNamespace;

	public SharedNamespace( String baseNamespace )
	{
		this.baseNamespace = baseNamespace;
	}

	@Override
	public String getBaseNamespace()
	{
		return baseNamespace;
	}
}
