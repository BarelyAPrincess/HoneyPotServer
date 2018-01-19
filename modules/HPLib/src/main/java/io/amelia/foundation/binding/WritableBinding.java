package io.amelia.foundation.binding;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.support.Objs;

public interface WritableBinding extends ReadableBinding
{
	default <T extends FacadeBinding> void registerFacadeBinding( @Nullable String namespace, @Nonnull Class<T> facadeService, @Nonnull Supplier<T> facadeSupplier ) throws BindingException.Error
	{
		registerFacadeBinding( namespace, facadeService, facadeSupplier, FacadePriority.NORMAL );
	}

	default <T extends FacadeBinding> void registerFacadeBinding( @Nonnull Class<T> facadeService, @Nonnull Supplier<T> facadeSupplier ) throws BindingException.Error
	{
		registerFacadeBinding( facadeService, facadeSupplier, FacadePriority.NORMAL );
	}

	default <T extends FacadeBinding> void registerFacadeBinding( @Nonnull Class<T> facadeService, @Nonnull Supplier<T> facadeSupplier, @Nonnull FacadePriority facadePriority ) throws BindingException.Error
	{
		registerFacadeBinding( null, facadeService, facadeSupplier, facadePriority );
	}

	default <T extends FacadeBinding> FacadeRegistration.Entry<T> registerFacadeBinding( @Nullable String namespace, @Nonnull Class<T> facadeService, @Nonnull Supplier<T> facadeSupplier, @Nonnull FacadePriority facadePriority ) throws BindingException.Error
	{
		String baseNamespace = getBaseNamespace();
		namespace = Bindings.normalizeNamespace( namespace );

		String fullNamespace = Objs.isEmpty( namespace ) ? baseNamespace + ".facade" : baseNamespace + "." + namespace;

		FacadeRegistration<T> facadeServiceList = getObjectWithException( fullNamespace, FacadeRegistration.class );

		if ( facadeServiceList == null )
		{
			facadeServiceList = new FacadeRegistration<T>( facadeService );
			set( fullNamespace, facadeServiceList );
		}
		else if ( !facadeServiceList.getBindingClass().equals( facadeService ) )
			throw new BindingException.Error( "The facade registered at namespace \"" + namespace + "\" does not match the facade already registered." );

		if ( facadeServiceList.isPriorityRegistered( facadePriority ) )
			throw new BindingException.Error( "There is already a facade registered for priority level " + facadePriority.name() + "at namespace \"" + namespace + "\"" );

		FacadeRegistration.Entry registration = new FacadeRegistration.Entry<T>( facadeSupplier, facadePriority );
		facadeServiceList.add( registration );
		return registration;
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
