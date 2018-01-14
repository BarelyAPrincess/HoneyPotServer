package io.amelia.foundation.binding;

import javax.annotation.Nonnull;

import io.amelia.foundation.facades.FacadeService;

interface ReadableBinding
{
	String getBaseNamespace();

	/**
	 * Looks for an instance that implements the {@link FacadeService}.
	 * <p>
	 * On top of checking the provided namespace, we also append `facade` and `instance`.
	 *
	 * @param namespace          The namespace to check
	 * @param facadeServiceClass The expected {@link FacadeService} subclass to be return.
	 * @param <T>                The {@link FacadeService} subclass.
	 *
	 * @return The FacadeService instance, null otherwise.
	 */
	@SuppressWarnings( "unchecked" )
	default <T extends FacadeService> T getFacade( String namespace, Class<T> facadeServiceClass )
	{
		Object obj = getObject( namespace, facadeServiceClass );

		if ( obj == null )
			obj = getObject( namespace + ".facade", facadeServiceClass );

		if ( obj == null )
			obj = getObject( namespace + ".instance", facadeServiceClass );

		return ( T ) obj;
	}

	/**
	 * @throws ClassCastException If the found instance can't be cast to the expected FacadeService.
	 */
	@SuppressWarnings( "unchecked" )
	default <T extends FacadeService> T getFacade( String namespace ) throws ClassCastException
	{
		return ( T ) getFacade( namespace, FacadeService.class );
	}

	default <T> T getObject( @Nonnull String namespace, @Nonnull Class<T> objectClass )
	{
		try
		{
			return getObjectWithException( namespace, objectClass );
		}
		catch ( BindingException.Error error )
		{
			return null;
		}
	}

	@SuppressWarnings( "unchecked" )
	default <T> T getObject( @Nonnull String namespace ) throws ClassCastException
	{
		// objectClass was not specified, so any subclass of Object is acceptable.
		return ( T ) getObject( namespace, Object.class );
	}

	@SuppressWarnings( "unchecked" )
	default <T> T getObjectWithException( String namespace, Class<T> objectClass ) throws BindingException.Error
	{
		String baseNamespace = getBaseNamespace();
		namespace = Bindings.normalizeNamespace( namespace );

		if ( !namespace.startsWith( baseNamespace ) )
			namespace = baseNamespace + "." + namespace;

		BindingReference ref = Bindings.getChild( namespace );

		Object obj = ref == null ? Bindings.resolveNamespace( namespace, objectClass ) : ref.getValue();

		if ( obj != null && !objectClass.isAssignableFrom( obj.getClass() ) )
			throw new BindingException.Error( "The object returned for namespace `" + namespace + "` wasn't assigned to class `" + objectClass.getSimpleName() + "`." );

		return ( T ) obj;
	}
}
