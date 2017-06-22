package io.amelia.foundation.binding;

import com.sun.istack.internal.NotNull;
import io.amelia.helpers.Objs;
import io.amelia.helpers.Strs;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves classes and keys to final instances
 * <p>
 * class -> class
 * class -> key
 * key -> key
 * key -> class
 */
public abstract class ServiceResolver
{
	private Map<Class<?>, String> classToKey = new ConcurrentHashMap<>();
	private String defKey = null;
	private Map<String, Object> instigated = new ConcurrentHashMap<>();
	private Map<String, String> keyToKey = new ConcurrentHashMap<>();
	private Map<String, BindingSupplier<?>> suppliers = new ConcurrentHashMap<>();

	/**
	 * Returns the service resolver key
	 *
	 * @return String
	 */
	public abstract String key();

	public void mapClass( @NotNull Class<?> cls, @NotNull String alias )
	{
		Objects.requireNonNull( cls );
		Objects.requireNonNull( alias );

		classToKey.put( cls, alias.toLowerCase() );
	}

	public void mapKey( @NotNull String key, @NotNull String alias )
	{
		Objects.requireNonNull( key );
		Objects.requireNonNull( alias );

		keyToKey.put( key.toLowerCase(), alias.toLowerCase() );
	}

	public void mapSupplier( @NotNull String key, @NotNull BindingSupplier<?> supplier )
	{
		Objects.requireNonNull( key );
		Objects.requireNonNull( supplier );

		suppliers.put( key.toLowerCase(), supplier );
	}

	public void mapSupplier( @NotNull String key, @NotNull ServiceBinding instance )
	{
		Objects.requireNonNull( key );
		Objects.requireNonNull( instance );

		mapSupplier( key, () -> instance );
	}

	public void mapSupplier( @NotNull String key, @NotNull Method method ) throws BindingException
	{
		Objects.requireNonNull( key );
		Objects.requireNonNull( method );

		if ( !method.getReturnType().isAssignableFrom( ServiceBinding.class ) )
			throw new BindingException( "Method \"" + method.getDeclaringClass().getSimpleName() + "::" + method.getName() + "\" for key \"" + key + "\" MUST return a ServiceBinding instance." );
		if ( !Modifier.isStatic( method.getModifiers() ) )
			throw new BindingException( "Method \"" + method.getDeclaringClass().getSimpleName() + "::" + method.getName() + "\" for key \"" + key + "\" MUST be static to be invoked in this manner." );
		method.setAccessible( true );
		try
		{
			mapSupplier( key, () -> ( ServiceBinding ) ( method.getParameterCount() > 0 ? method.invoke( null, BindingRegistry.getDependencies( method.getParameters() ) ) : method.invoke( null ) ) );
		}
		catch ( Exception e )
		{
			throw new BindingException( "Method \"" + method.getDeclaringClass().getSimpleName() + "::" + method.getName() + "\" for key \"" + key + "\" throw an unexpected exception.", e );
		}
	}

	public void mapSupplier( @NotNull String key, @NotNull Object obj, @NotNull Method method ) throws BindingException
	{
		Objects.requireNonNull( key );
		Objects.requireNonNull( obj );
		Objects.requireNonNull( method );

		if ( !method.getReturnType().isAssignableFrom( ServiceBinding.class ) )
			throw new BindingException( "Method \"" + method.getDeclaringClass().getSimpleName() + "::" + method.getName() + "\" for key \"" + key + "\" MUST return a ServiceBinding instance." );
		if ( Modifier.isStatic( method.getModifiers() ) )
			throw new BindingException( "Method \"" + method.getDeclaringClass().getSimpleName() + "::" + method.getName() + "\" for key \"" + key + "\" MUST NOT be static to be invoked in this manner." );
		method.setAccessible( true );
		try
		{
			mapSupplier( key, () -> ( ServiceBinding ) ( method.getParameterCount() > 0 ? method.invoke( obj, BindingRegistry.getDependencies( method.getParameters() ) ) : method.invoke( obj ) ) );
		}
		catch ( Exception e )
		{
			throw new BindingException( "Method \"" + method.getDeclaringClass().getSimpleName() + "::" + method.getName() + "\" for key \"" + key + "\" throw an unexpected exception.", e );
		}
	}

	@SuppressWarnings( "unchecked" )
	public <T> T resolve( @NotNull String key ) throws BindingException
	{
		if ( Objs.isEmpty( key ) )
		{
			if ( Objs.isEmpty( defKey ) )
				throw new BindingException( "The default key was never set for Service Resolver [" + getClass().getSimpleName() + "]" );
			return resolve( defKey );
		}

		key = key.toLowerCase();

		// Check if a key has been diverted. Keys that contain periods are considered a root divert and sent back to the binding registry for a recheck.
		if ( keyToKey.containsKey( key ) )
			return keyToKey.get( key ).contains( "." ) ? ( T ) BindingRegistry.resolve( keyToKey.get( key ) ) : resolve( keyToKey.get( key ) );

		// Check for binding supplier
		if ( suppliers.containsKey( key ) )
			try
			{
				return ( T ) suppliers.get( key ).get();
			}
			catch ( ClassCastException e )
			{
				throw e;
			}
			catch ( Exception e )
			{
				throw new BindingException( "The binding supplier for key \"" + key + "\" throw an unexpected exception.", e );
			}

		// Check for an instigated class
		if ( instigated.containsKey( key ) )
			return ( T ) instigated.get( key );

		// Convert key into something a bit more resolvable.
		key = Strs.toCamelCase( key.replaceAll( "[-_./\\\\]", " " ) );
		Class reflection = getClass();

		// Check for a local field of the same name
		try
		{
			Field field = reflection.getField( key );
			field.setAccessible( true );
			return ( T ) field.get( this );
		}
		catch ( ClassCastException | NoSuchFieldException | IllegalAccessException e )
		{
			// Ignore
		}

		// Check for a local method of the same name
		try
		{
			for ( Method method : reflection.getMethods() )
				if ( key.equals( method.getName() ) )
				{
					method.setAccessible( true );
					if ( method.getReturnType() != null )
						return ( T ) ( method.getParameterCount() > 0 ? method.invoke( this, BindingRegistry.getDependencies( method.getParameters() ) ) : method.invoke( this ) );
				}
		}
		catch ( IllegalAccessException e )
		{
			// Ignore
		}
		catch ( ClassCastException e )
		{
			throw e;
		}
		catch ( Exception e )
		{
			throw new BindingException( "The binding method for key \"" + key + "\" throw an unexpected exception.", e );
		}

		// Fail with null as we can't find the requested key in the resolver
		return null;
	}

	/**
	 * Called when a class needs resolving.
	 * Each registered resolver will be called for this purpose, the first to return non-false will succeed.
	 *
	 * @param cls
	 */
	@SuppressWarnings( "unchecked" )
	public <T> T resolveClass( Class<T> cls ) throws BindingException
	{
		// Check if a class has been mapped to a key
		if ( classToKey.containsKey( cls ) )
			return resolve( classToKey.get( cls ) );

		// Check if the class has been initialized
		for ( Object instance : instigated.values() )
			if ( instance.getClass().isAssignableFrom( cls ) )
				return ( T ) instance;

		try
		{
			// Check if the class implements a static key() method, which might work to resolve a class
			Method method = cls.getMethod( "key" );
			if ( Modifier.isStatic( method.getModifiers() ) && method.getReturnType().isAssignableFrom( String.class ) )
				return resolve( ( String ) method.invoke( null ) );
		}
		catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException e )
		{
			// Ignore
		}

		try
		{
			// A last resort!
			return resolve( cls.getSimpleName() );
		}
		catch ( Exception e )
		{
			// Ignore failure since it's a long shot.
			return null;
		}
	}

	/**
	 * Sets the default key name for when the root is requested.
	 *
	 * @param defKey The Default Key
	 */
	public void setDefault( String defKey )
	{
		this.defKey = defKey;
	}

	public enum Priority
	{
		HIGHEST,
		HIGH,
		NORMAL,
		LOW,
		LOWEST
	}
}
