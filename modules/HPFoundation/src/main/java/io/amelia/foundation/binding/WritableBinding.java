package io.amelia.foundation.binding;

import io.amelia.foundation.facades.FacadePriority;
import io.amelia.foundation.facades.FacadeService;

public interface WritableBinding extends ReadableBinding
{
	default void registerFacade( String namespace, FacadeService facadeService, FacadePriority facadePriority ) throws BindingException.Error
	{
		String baseNamespace = getBaseNamespace();
		namespace = Bindings.normalizeNamespace( namespace );

		String fullNamespace = baseNamespace + "." + namespace;

		FacadeWrapper facadeWrapper = getObjectWithException( fullNamespace, FacadeWrapper.class );

		if ( facadeWrapper == null )
		{
			facadeWrapper = new FacadeWrapper();
			set( fullNamespace, facadeWrapper );
		}

		if ( facadeWrapper.registeredFacades.get( facadePriority ) != null )
			throw new BindingException.Error( "Facade already registered!" );

		facadeWrapper.registeredFacades.put( facadePriority, facadeService );
	}

	default void set( String namespace, Object obj )
	{
		String baseNamespace = getBaseNamespace();
		namespace = Bindings.normalizeNamespace( namespace );

		if ( !namespace.startsWith( baseNamespace ) )
			namespace = baseNamespace + "." + namespace;

		Bindings.getChildOrCreate( namespace ).setValue( obj );
		Bindings.getChildOrCreate( baseNamespace ).trimChildren();
	}
}
