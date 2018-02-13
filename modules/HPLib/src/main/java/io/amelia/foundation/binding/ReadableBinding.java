/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.binding;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.support.Objs;

public class ReadableBinding
{
	final String baseNamespace;

	public ReadableBinding( String baseNamespace )
	{
		this.baseNamespace = Bindings.normalizeNamespace( baseNamespace );
	}

	@Nonnull
	protected BindingMap getBinding( String namespace )
	{
		return Bindings.bindings.getChildOrCreate( namespace );
	}

	@Nonnull
	protected BindingMap getBinding()
	{
		return Bindings.bindings.getChildOrCreate( baseNamespace );
	}

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
	public <T extends FacadeBinding> T getFacadeBinding( @Nonnull String namespace )
	{
		try
		{
			FacadeRegistration<T> facadeRegistration = ( FacadeRegistration<T> ) getFacadeBindingRegistration( namespace );
			return facadeRegistration == null ? null : facadeRegistration.getHighestPriority();
		}
		catch ( ClassCastException e )
		{
			return null;
		}
	}

	public <T extends FacadeBinding> T getFacadeBinding( @Nonnull String namespace, @Nonnull Class<T> expectedClassBinding )
	{
		return Objs.ifPresentTest( getFacadeBinding( namespace ), binding -> expectedClassBinding.isAssignableFrom( binding.getClass() ) );
	}

	/**
	 * Queries for a facade registration. This will return if no facade has been registered.
	 *
	 * @param namespace The facade namespace appended to the base namespace.
	 *
	 * @return facade registration or null
	 */
	public FacadeRegistration<FacadeBinding> getFacadeBindingRegistration( @Nonnull String namespace )
	{
		FacadeRegistration facadeRegistration = getObject( namespace, FacadeRegistration.class );

		if ( facadeRegistration == null )
			facadeRegistration = getObject( namespace + ".facade", FacadeRegistration.class );

		if ( facadeRegistration == null )
			facadeRegistration = getObject( namespace + ".instance", FacadeRegistration.class );

		return facadeRegistration;
	}

	/**
	 * Get registrations for facade class.
	 *
	 * @param namespace The facade namespace appended to the base namespace.
	 *
	 * @return a stream of registrations
	 */
	public Stream<FacadeRegistration> getFacadeRegistrations( @Nonnull String namespace )
	{
		BindingMap map = getBinding().getChild( namespace );
		if ( map == null )
			return Stream.empty();
		return map.findValues( FacadeRegistration.class ).map( baseBinding -> ( FacadeRegistration ) baseBinding.getInstance() );
	}

	public <T extends FacadeBinding> Stream<T> getFacades( @Nonnull Class<T> facadeService )
	{
		return getFacadeRegistrations( "" ).filter( facadeRegistration -> facadeService.isAssignableFrom( facadeRegistration.getBindingClass() ) ).map( facadeRegistration -> ( T ) facadeRegistration.getHighestPriority() );
	}

	public <T> T getObject( @Nonnull String namespace, @Nonnull Class<T> objectClass )
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
	public <T> T getObject( @Nonnull String namespace ) throws ClassCastException
	{
		// objectClass was not specified, so any subclass of Object is acceptable.
		return ( T ) getObject( namespace, Object.class );
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getObjectWithException( @Nonnull String namespace, @Nonnull Class<T> objectClass ) throws BindingException.Error
	{
		return Bindings.Lock.callWithReadLock( ( namespace0, objectClass0 ) -> {
			namespace0 = Bindings.normalizeNamespace( baseNamespace, namespace0 );

			BindingMap ref = Bindings.getChild( namespace0 );

			Object obj = ref == null ? Bindings.resolveNamespace( namespace0, objectClass0 ) : ref.getValue();

			if ( obj != null && !objectClass0.isAssignableFrom( obj.getClass() ) )
				throw new BindingException.Error( "The object returned for namespace `" + namespace0 + "` wasn't assigned to class `" + objectClass0.getSimpleName() + "`." );

			return ( T ) obj;
		}, namespace, objectClass );
	}

	public ReadableBinding getSubNamespace( String namespace )
	{
		return Bindings.getNamespace( baseNamespace + "." + namespace );
	}

	/**
	 * <p>
	 * Setter Example:
	 * <pre>
	 * {
	 *      NamespaceBinding nb = Bindings.bindNamespace("com.google.somePlugin");
	 *      nb.setObject("facade", new SomeFacadeService());
	 *      nb.setObject("obj", new Object());
	 * }
	 * </pre>
	 * <p>
	 * Getter Example:
	 * <pre>
	 * {
	 *      // When getting a facade service; we look at the namespace, then the namespace plus "facade" for an object that extends the FacadeService interface.
	 *      Bindings.getFacade("com.google.somePlugin");
	 *      // You can also define the expected facade class to ensure not just any facade is returned. This ensures you receive null instead of a ClassCastException.
	 *      Bindings.getFacade("com.google.somePlugin", SomeFacadeService.class);
	 *      // Any object can be set and retrieved from the bindings.
	 *      Bindings.getObject("com.google.somePlugin.obj");
	 * }
	 * </pre>
	 */
	public WritableBinding writable()
	{
		// TODO Validate writing permission - one such being a check of the calling package

		return new WritableBinding( baseNamespace );
	}
}
