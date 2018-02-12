package io.amelia.foundation.binding;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.support.Objs;
import io.amelia.support.Strs;

public class WritableBinding extends ReadableBinding
{
	private boolean destroyed;

	public WritableBinding( String namespace )
	{
		super( namespace );
	}

	public void destroy()
	{
		getBinding().unprivatize( this );
		this.destroyed = true;
	}

	@Override
	public WritableBinding getSubNamespace( String namespace )
	{
		return Bindings.getNamespace( baseNamespace + "." + namespace ).writable();
	}

	public boolean isDestroyed()
	{
		return destroyed;
	}

	public boolean isPrivatized()
	{
		return !isDestroyed() && getBinding().isPrivatized();
	}

	/**
	 * Attempts to privatize this writing binding, so only this reference can be allowed to make changes to the namespace.
	 * <p>
	 * This is done by making a weak reference of this binding from within the namespace. As long at this instance isn't
	 * destroyed by the JVM GC, it will remain private. However, remember that this will have no effect on writable instances
	 * requested prior to the construction of this method nor will it affect read access.
	 * <p>
	 * Also remember the only way to make the namespace public once again is to either dereference this instance or call the destroy method.
	 * It's also note worth that if a parent namespace is privatized, it will take precedence and destroy this WritableBinding.
	 */
	public void privatize() throws BindingException.Denied
	{
		if ( baseNamespace.startsWith( "io.amelia" ) )
			throw new BindingException.Denied( "Namespace \"io.amelia\" can't privatized as it's reserved for internal use." );
		if ( Strs.countInstances( baseNamespace, '.' ) < 2 )
			throw new BindingException.Denied( "Namespaces with less than 3 nodes can't be privatized." );

		Bindings.bindings.getChildOrCreate( baseNamespace ).privatize( this );
	}

	public <T extends FacadeBinding> void registerFacadeBinding( @Nonnull Class<T> facadeService, @Nonnull Supplier<T> facadeSupplier ) throws BindingException.Error
	{
		registerFacadeBinding( facadeService, facadeSupplier, FacadePriority.NORMAL );
	}

	public <T extends FacadeBinding> void registerFacadeBinding( @Nonnull Class<T> facadeService, @Nonnull Supplier<T> facadeSupplier, @Nonnull FacadePriority facadePriority ) throws BindingException.Error
	{
		registerFacadeBinding( null, facadeService, facadeSupplier, facadePriority );
	}

	public <T extends FacadeBinding> void registerFacadeBinding( @Nullable String namespace, @Nonnull Class<T> facadeService, @Nonnull Supplier<T> facadeSupplier ) throws BindingException.Error
	{
		registerFacadeBinding( namespace, facadeService, facadeSupplier, FacadePriority.NORMAL );
	}

	public <T extends FacadeBinding> FacadeRegistration.Entry<T> registerFacadeBinding( @Nullable String namespace, @Nonnull Class<T> facadeService, @Nonnull Supplier<T> facadeSupplier, @Nonnull FacadePriority facadePriority ) throws BindingException.Error
	{
		return Bindings.Lock.callWithWriteLock( ( namespace0, facadeService0, facadeSupplier0, facadePriority0 ) -> {
			namespace0 = Bindings.normalizeNamespace( namespace0 );

			String fullNamespace = Objs.isEmpty( namespace0 ) ? baseNamespace + ".facade" : baseNamespace + "." + namespace0;

			FacadeRegistration<T> facadeServiceList = getObjectWithException( fullNamespace, FacadeRegistration.class );

			if ( facadeServiceList == null )
			{
				facadeServiceList = new FacadeRegistration<T>( facadeService0 );
				set( fullNamespace, facadeServiceList );
			}
			else if ( !facadeServiceList.getBindingClass().equals( facadeService0 ) )
				throw new BindingException.Error( "The facade registered at namespace \"" + namespace0 + "\" does not match the facade already registered." );

			if ( facadeServiceList.isPriorityRegistered( facadePriority0 ) )
				throw new BindingException.Error( "There is already a facade registered for priority level " + facadePriority0.name() + "at namespace \"" + namespace0 + "\"" );

			FacadeRegistration.Entry registration = new FacadeRegistration.Entry<T>( facadeSupplier0, facadePriority0 );
			facadeServiceList.add( registration );
			return registration;
		}, namespace, facadeService, facadeSupplier, facadePriority );
	}

	public void set( String namespace, Object obj )
	{
		namespace = Bindings.normalizeNamespace( namespace );

		if ( !namespace.startsWith( baseNamespace ) )
			namespace = baseNamespace + "." + namespace;

		getBinding( namespace ).set( obj );
		getBinding().trimChildren();
	}
}
