package io.amelia.foundation.binding;

public class BoundNamespace implements ReadableBinding, WritableBinding
{
	public BindingResolver resolver;
	protected String baseNamespace;

	protected BoundNamespace( String baseNamespace )
	{
		this.baseNamespace = baseNamespace;
	}

	@Override
	public String getBaseNamespace()
	{
		return baseNamespace;
	}

	public BindingResolver getResolver()
	{
		if ( resolver != null )
			resolver.baseNamespace = baseNamespace;
		return resolver;
	}

	public boolean isBound()
	{
		return Bindings.isBound0( this );
	}

	public void unbind()
	{
		Bindings.unbind0( this );
	}
}
