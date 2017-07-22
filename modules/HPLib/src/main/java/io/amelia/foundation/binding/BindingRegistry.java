package io.amelia.foundation.binding;

import com.sun.istack.internal.NotNull;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.binding.resolvers.AppServiceResolver;
import io.amelia.foundation.binding.resolvers.ServiceResolverBase;
import io.amelia.foundation.binding.resolvers.ServiceResolverPriority;
import io.amelia.helpers.Arrs;
import io.amelia.helpers.Objs;
import io.amelia.helpers.Maps;
import io.amelia.lang.UncaughtException;
import io.amelia.lang.annotation.Default;
import io.amelia.lang.annotation.Null;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class BindingRegistry
{
	private static Class[] buildStack = new Class[0];
	private static Map<String, ServiceResolverBase> resolvers = new HashMap<>();
	private static Map<String, String> resolversAlias = new HashMap<>();

	static
	{
		registerResolver( ServiceResolverPriority.NORMAL, new AppServiceResolver() );
	}

	/**
	 * Attempts to construct a class
	 *
	 * @param string $class
	 * @param array  $parameters
	 * @return object
	 * @throws BindingException
	 */
	public static function buildClass( Class<?> cls, Object[] parameters ) throws BindingException
	{
		if ( building( cls ) )
			throw new BindingException( "The class [" + cls.getSimpleName() + "] is already being built." );
		Arrs.push( buildStack, cls );
		try
		{
			// If the type is not instantiable, the developer is attempting to resolve
			// an abstract type such as an Interface of Abstract Class and there is
			// no binding registered for the abstractions so we need to bail out.
			if ( Modifier.isAbstract( cls.getModifiers() ) || Modifier.isInterface( cls.getModifiers() ) )
				throw new BindingException( "Target [" + cls.getSimpleName() + "] is not instantiable." );
			Constructor constructor = cls.getConstructor( Arrays.stream( parameters ).map( Object::getClass ).toArray( Class[]::new ) );

			$constructor = $reflector -> getConstructor();
			// If there are no constructors, that means there are no dependencies then
			// we can just resolve the instances of the objects right away.
			if ( is_null( $constructor ) )
				return new $class; $dependencies = $constructor -> getParameters();
			// Once we have all the constructor's parameters we can create each of the
			// dependency instances and then use the reflection instances to make a
			// new instance of this class, injecting the created dependencies in.
			$parameters = static::keyParametersByArgument( $dependencies, $parameters );
			$instances = static::getDependencies( $dependencies, $parameters, $class );
			Arrs.pop( buildStack );

			return $reflector -> newInstanceArgs( $instances );
		}
		catch ( NoSuchMethodException e )
		{
			Arrs.pop( buildStack );
			throw new BindingException( "Failed to build [$class]: ".$e->getMessage(). " in ".$e->getFile(). " on line ".$e->getLine() );
		}
	}

	/**
	 * Are we building this class
	 *
	 * @param cls
	 * @return boolean
	 */
	public static boolean building( Class<?> cls )
	{
		for ( Class<?> c : buildStack )
			if ( c == cls )
				return true;
		return false;
	}

	/**
	 * Call the given Closure / class@method and inject its dependencies.
	 *
	 * @param callable|string $callback
	 * @param array           $parameters
	 * @param string|null     $defaultMethod
	 * @return mixed
	 */
	public static function call( $callback, array $parameters =[], $defaultMethod =null )
	{
		if ( is_string( $callback ) && strpos( $callback, '@' ) != = false || $defaultMethod )
		{
			$segments = explode( '@', $callback );
			$method = count( $segments ) == 2 ? $segments[1] : $defaultMethod;
			if ( is_null( $method ) )
				throw new \InvalidArgumentException( 'Method not provided.' );
			$callback = [static::resolve( $segments[0] ), $method];
		} $dependencies = static::getMethodDependencies( $callback, $parameters );
		return call_user_func_array( $callback, $dependencies );

		BindingRegistry.call( ( a, b ) -> null );
	}

	public static void call( BiFunction<Object, Object, Void> function )
	{

	}

	/**
	 * Get the proper reflection instance for the given callback.
	 *
	 * @param callable|string $callback
	 * @return \ReflectionFunctionAbstract
	 */
	protected static function getCallReflector( Callable callback )
	{
		if ( is_string( $callback ) && strpos( $callback, '::' ) != = false )
			$callback = explode( '::', $callback ); if ( is_array( $callback ) )
		return new \ReflectionMethod( $callback[0], $callback[1] );
		return new \ReflectionFunction( $callback );
	}

	public static Object[] getDependencies( Parameter[] parameters ) throws BindingException
	{
		return getDependencies( parameters, new HashMap<>(), null );
	}

	public static Object[] getDependencies( Parameter[] parameters, @NotNull Map<String, Object> primitives ) throws BindingException
	{
		return getDependencies( parameters, primitives, null );
	}

	/**
	 * Resolve all of the dependencies from the ReflectionParameters.
	 *
	 * @param parameters
	 * @param primitives
	 * @param classAndMethod
	 * @return array
	 */
	public static Object[] getDependencies( Parameter[] parameters, @NotNull Map<String, Object> primitives, String classAndMethod ) throws BindingException
	{
		List<Object> dependencies = new ArrayList<>();
		if ( primitives.containsKey( "app" ) )
			primitives.put( "app", Kernel.application() );
		for ( Parameter parameter : parameters )
		{
			try
			{
				/*
				 * If the class is null, it means the dependency is a string or some other
				 * primitive type which we can not resolve since it is not a class and
				 * we will just bomb out with an error since we have no-where to go.
				 */
				if ( primitives.containsKey( parameter.getName() ) )
					dependencies.add( primitives.get( parameter.getName() ) );
				else if ( parameter.getType() == Object.class )
					dependencies.add( resolve( parameter.getName() ) );
				else
				{
					Map<String, Object> depend = resolveClass( parameter.getType() );
					if ( depend.size() == 0 )
						throw new BindingException();
					if ( depend.containsKey( parameter.getName() ) )
						dependencies.add( depend.get( parameter.getName() ) );
					else
						dependencies.add( Maps.first( depend ) );
				}
			}
			catch ( BindingException e )
			{
				boolean failure = true;

				/*
				 * If we can not resolve the class instance, we will check to see if the value
				 * is optional, and if it is we will return the optional parameter value as
				 * the value of the dependency, similarly to how we do this with scalars.
				 */
				if ( parameter.isAnnotationPresent( Null.class ) )
				{
					dependencies.add( null );
					failure = false;
				}
				else if ( parameter.isAnnotationPresent( Default.class ) )
				{
					try
					{
						Default def = parameter.getAnnotation( Default.class );
						dependencies.add( Objs.initClass( def.value() ) );
						failure = false;
					}
					catch ( UncaughtException ue )
					{
						// Ignore
					}
				}

				if ( failure )
				{
					Class<?> parameterClass = parameter.getType();
					for ( Object primitive : primitives.values() )
						if ( primitive != null && primitive.getClass().isAssignableFrom( parameterClass ) )
						{
							failure = false;
							dependencies.add( primitive );
						}
				}

				if ( failure )
					throw new BindingException( "Dependency injection failed for " + parameter.getName() + " on " + classAndMethod + "( " + Arrays.stream( parameters ).map( p -> p.getType().getSimpleName() + " " + p.getName() ).collect( Collectors.joining( ", " ) ) + " )" );
			}
		}

		return dependencies.toArray();
	}

	/**
	 * Get all dependencies for a given method.
	 *
	 * @param callable|string $callback
	 * @param array           $parameters
	 * @return array
	 */
	public static function getMethodDependencies( $callback, array $parameters =[] )
	{
		$reflector = static::getCallReflector( $callback );
		return static::getDependencies( $reflector -> getParameters(), $parameters, $reflector -> getName() );
	}

	/**
	 * @param name
	 * @return ServiceResolver
	 */
	public static ServiceResolverBase getResolver( String name )
	{
		if ( resolvers.containsKey( name ) )
			return resolvers.get( name );
		if ( resolversAlias.containsKey( name ) )
			return resolvers.get( resolversAlias.get( name ) );
		return null;
	}

	/**
	 * If extra parameters are passed by numeric ID, rekey them by argument name.
	 *
	 * @param dependencies
	 * @param parameters
	 * @return array
	 */
	protected static function keyParametersByArgument( array $dependencies, array $parameters )
	{
		foreach( $parameters as $key = > $value )
		if ( is_numeric( $key ) )
		{
			unset( $parameters[$key] );
			$parameters[$dependencies[$key]->name] =$value;
		} return $parameters;
	}

	public static void registerResolver( ServiceResolverBase resolver ) throws BindingException
	{
		registerResolver( ServiceResolverPriority.NORMAL, resolver );
	}

	public static void registerResolver( ServiceResolverPriority priority, ServiceResolverBase resolver ) throws BindingException
	{
		if ( keys == null || keys.length == 0 )
			keys = Arrs.array( resolver.key() );

		String[] alias = resolver.getKeyAliases().toArray( new String[0] );

		alias = Arrs.merge( alias, Arrs.trimStart( keys, 1 ) );
		String key = keys[0];

		resolvers.put( key, resolver );
		for ( String keyAlias : alias )
			resolversAlias.put( keyAlias, key );
	}

	/**
	 * Attempts to locate a instance or value from the registered resolvers.
	 *
	 * @param key
	 * @return mixed|null|object
	 */
	public static Object resolve( @NotNull String key )
	{
		Objs.notEmpty( key );
		try
		{
			return resolveClass( Class.forName( key ) );
		}
		catch ( ClassNotFoundException e )
		{
			// Ignore
		}
		String[] keys = key.split( "." );
		ServiceResolverBase resolver = getResolver( keys[0] );
		if ( resolver != null )
			return resolver.resolve( keys[0], Arrs.trimStart( keys, 1 ) );
		return null;
	}

	/**
	 * Attempts to locate a class within the registered resolvers.
	 * Optionally will build the class on failure.
	 *
	 * @param string $class
	 * @param bool   $buildOnFailure
	 * @param array  $parameters
	 * @return mixed|null|object
	 */
	public static Object resolveClass( Class<?> cls, boolean buildOnFailure =false, parameters )
	{
		if ( cls == Application.class )
			return Kernel.application();
		if ( cls == Configuration.class )
			return Kernel.config();
		if ( cls == Logger.class )
			return Kernel.log();
		Object result;
		for ( resolver:
		      resolvers )
			if ( false != ( result = resolver.resolveClass( cls ) ) )
				return result; if ( buildOnFailure )
		return buildClass( cls, parameters );
		return null;
	}

}
