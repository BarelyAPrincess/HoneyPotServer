package io.amelia.foundation.binding;

import javax.annotation.Nonnull;

interface ReadableBinding
{
	String getBaseNamespace();

	/**
	 * Looks for an instance that implements the {@link FacadeBinding}.
	 * <p>
	 * On top of checking the provided namespace, we also append `facade` and `instance`.
	 *
	 * @param namespace The namespace to check
	 * @param <T>       The {@link FacadeBinding} subclass.
	 *
	 * @return The FacadeService instance, null otherwise.
	 */
	@SuppressWarnings( "unchecked" )
	default <T extends FacadeBinding> T getFacadeBinding( @Nonnull String namespace )
	{
		try
		{
			FacadeRegistration facadeRegistration = getFacadeBindingRegistration( namespace );
			return facadeRegistration == null ? null : facadeRegistration.getHighestPriority();
		}
		catch ( ClassCastException e )
		{
			return null;
		}
	}

	default FacadeRegistration<? extends FacadeBinding> getFacadeBindingRegistration( @Nonnull String namespace )
	{
		FacadeRegistration facadeRegistration = getObject( namespace, FacadeRegistration.class );

		if ( facadeRegistration == null )
			facadeRegistration = getObject( namespace + ".facade", FacadeRegistration.class );

		if ( facadeRegistration == null )
			facadeRegistration = getObject( namespace + ".instance", FacadeRegistration.class );

		return facadeRegistration;
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
	default <T> T getObjectWithException( @Nonnull String namespace, @Nonnull Class<T> objectClass ) throws BindingException.Error
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
