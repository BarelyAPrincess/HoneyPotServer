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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.foundation.Kernel;
import io.amelia.support.BiFunctionWithException;
import io.amelia.support.FunctionWithException;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;
import io.amelia.support.QuadFunctionWithException;
import io.amelia.support.Strs;
import io.amelia.support.TriFunctionWithException;

public class Bindings
{
	public static final Kernel.Logger L = Kernel.getLogger( Bindings.class );
	protected static final BindingMap bindings = new BindingMap( "" );
	protected static final List<BindingResolver> resolvers = new ArrayList<>();
	protected static final WritableBinding root = new WritableBinding( "" );

	static BindingMap getChild( @Nonnull String namespace )
	{
		return bindings.getChild( namespace );
	}

	static BindingMap getChildOrCreate( @Nonnull String namespace )
	{
		return bindings.getChildOrCreate( namespace );
	}

	public static ReadableBinding getNamespace( String namespace )
	{
		return new ReadableBinding( namespace );
	}

	private static List<BindingResolver> getResolvers()
	{
		return getResolvers( null );
	}

	private static List<BindingResolver> getResolvers( String namespace )
	{
		return Lock.callWithReadLock( namespace0 -> {
			List<BindingResolver> list = new ArrayList<>();
			namespace0 = normalizeNamespace( namespace0 );

			/*for ( Map.Entry<String, WeakReference<BoundNamespace>> entry : boundNamespaces.entrySet() )
				if ( ( namespace == null || namespace.startsWith( entry.getKey() ) ) && entry.getValue().get() != null )
				{
					BindingResolver bindingResolver = entry.getValue().get().getResolver();
					if ( bindingResolver != null )
						list.add( bindingResolver );
				}*/

			resolvers.sort( new BindingResolver.Comparator() );

			for ( BindingResolver bindingResolver : resolvers )
				if ( namespace0 == null || namespace0.startsWith( bindingResolver.baseNamespace ) )
					list.add( bindingResolver );

			return list;
		}, namespace );
	}

	public static ReadableBinding getSystemNamespace()
	{
		return getNamespace( "io.amelia" );
	}

	/**
	 * Returns a {@link WritableBinding} for the provided system class.
	 *
	 * The concept is that any class that exists under the io.amelia namespace has a binding accessible to the entire system.
	 *
	 * @param aClass The class needing the binding.
	 *
	 * @return The namespace.
	 */
	public static WritableBinding getSystemNamespace( Class<?> aClass )
	{
		// For now we'll assign system namespaces based on the class package, however, in the future we might want to do some additional checking to make sure someone isn't trying to spoof the protected io.amelia package.

		Package pack = aClass.getPackage();
		if ( pack == null )
			throw new BindingException.Denied( "We had a problem obtaining the package from class \"" + aClass.getName() + "\"." );
		String packName = pack.getName();
		// if ( !packName.startsWith( "io.amelia." ) )
		// throw new BindingException.Denied( "Only internal class starting with \"io.amelia\" can be got through this method." );
		return new WritableBinding( packName );
	}

	public static void init()
	{
		// TODO Is this needed now or ever?

		// Load Builtin Facades First
		// registerFacadeBinding( FacadeService.class, FacadePriority.STRICT, FacadeService::new );
	}

	public static <T> T invokeFields( @Nonnull Object declaringObject, @Nonnull Predicate<Field> fieldPredicate )
	{
		return invokeFields( declaringObject, fieldPredicate, null );
	}

	protected static <T> T invokeFields( @Nonnull Object declaringObject, @Nonnull Predicate<Field> fieldPredicate, @Nullable String namespace )
	{
		for ( Field field : declaringObject.getClass().getDeclaredFields() )
			if ( fieldPredicate.test( field ) )
				try
				{
					field.setAccessible( true );
					T obj = ( T ) field.get( declaringObject );

					if ( !field.isAnnotationPresent( DynamicBinding.class ) && !Objs.isEmpty( namespace ) )
						bindings.getChildOrCreate( namespace ).set( obj );

					return obj;
				}
				catch ( IllegalAccessException e )
				{
					e.printStackTrace();
				}

		return null;
	}

	public static <T> T invokeMethods( @Nonnull Object declaringObject, @Nonnull Predicate<Method> methodPredicate )
	{
		return invokeMethods( declaringObject, methodPredicate, null );
	}

	protected static <T> T invokeMethods( @Nonnull Object declaringObject, @Nonnull Predicate<Method> methodPredicate, @Nullable String namespace )
	{
		Map<Integer, Method> possibleMethods = new TreeMap<>();

		for ( Method method : declaringObject.getClass().getDeclaredMethods() )
			if ( methodPredicate.test( method ) )
				possibleMethods.put( method.getParameterCount(), method );

		// Try each possible method, starting with the one with the least number of parameters.
		for ( Method method : possibleMethods.values() )
			try
			{
				method.setAccessible( true );
				T obj = ( T ) method.invoke( declaringObject, resolveParameters( method.getParameters() ) );

				if ( !method.isAnnotationPresent( DynamicBinding.class ) && !Objs.isEmpty( namespace ) )
					bindings.getChildOrCreate( namespace ).set( obj );

				return obj;
			}
			catch ( IllegalAccessException | InvocationTargetException | BindingException.Error e )
			{
				e.printStackTrace();
			}

		return null;
	}

	static String normalizeNamespace( @Nullable String namespace )
	{
		if ( namespace == null )
			return null;
		return Strs.toAscii( namespace.replaceAll( "[^a-z0-9_.]", "" ) );
	}

	public static String normalizeNamespace( @Nonnull String baseNamespace, @Nullable String namespace )
	{
		Objs.notNull( baseNamespace );

		baseNamespace = normalizeNamespace( baseNamespace );
		namespace = namespace == null ? "" : normalizeNamespace( namespace );

		if ( !namespace.startsWith( baseNamespace ) )
			namespace = baseNamespace + "." + namespace;

		return Strs.trimAll( namespace, '.' ).replaceAll( "\\.{2,}", "." );
	}

	/**
	 * TODO To be made PUBLIC later when API is improved and better secured.
	 */
	private static void registerResolver( @Nonnull String namespace, @Nonnull BindingResolver bindingResolver ) throws BindingException.Error
	{
		Objs.notEmpty( namespace );
		Objs.notNull( bindingResolver );

		Namespace ns = Namespace.parseString( namespace ).fixInvalidChars().normalizeAscii();
		if ( ns.startsWith( "io.amelia" ) )
			throw new BindingException.Error( "Namespace \"io.amelia\" is reserved for internal use only." );
		if ( ns.getNodeCount() < 3 )
			throw new BindingException.Error( "Resolvers can only be registered to namespaces with no less than 3 nodes." );
		namespace = ns.getString();

		bindingResolver.baseNamespace = namespace;
		resolvers.add( bindingResolver );
	}

	public static <T> T resolveClass( @Nonnull Class<T> expectedClass )
	{
		Objs.notNull( expectedClass );

		for ( BindingResolver bindingResolver : getResolvers() )
		{
			Object obj = bindingResolver.get( expectedClass );
			if ( obj != null )
				return ( T ) obj;
		}

		return null;
	}

	protected static <T> T resolveNamespace( @Nonnull String namespace, @Nonnull Class<T> expectedClass )
	{
		Objs.notEmpty( namespace );
		Objs.notNull( expectedClass );

		for ( BindingResolver bindingResolver : getResolvers( namespace ) )
		{
			Object obj = bindingResolver.get( namespace, expectedClass );
			if ( obj != null )
				return ( T ) obj;
		}

		return null;
	}

	public static Object[] resolveParameters( @Nonnull Parameter[] parameters ) throws BindingException.Error
	{
		if ( parameters.length == 0 )
			return new Object[0];

		Object[] parameterObjects = new Object[parameters.length];

		for ( int i = 0; i < parameters.length; i++ )
		{
			Parameter parameter = parameters[i];
			Object obj = null;

			if ( parameter.isAnnotationPresent( BindingNamespace.class ) )
			{
				String ns = parameter.getAnnotation( BindingNamespace.class ).value();
				obj = root.getObject( ns );
			}

			if ( obj == null && parameter.isAnnotationPresent( BindingClass.class ) )
				obj = resolveClass( parameter.getAnnotation( BindingClass.class ).value() );

			if ( obj == null )
				obj = resolveClass( parameter.getType() );

			// If the obj is null and the parameter is not nullable, then throw an exception.
			if ( obj == null && !parameter.isAnnotationPresent( Nullable.class ) )
				throw new BindingException.Error( "Method parameter " + parameter.getName() + " had BindingNamespace annotation and we failed to resolve it!" );
			else
				parameterObjects[i] = obj;
		}

		return parameterObjects;
	}

	private Bindings()
	{
		// Private Access
	}

	protected static class Lock
	{
		private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		private static final java.util.concurrent.locks.Lock readLock = lock.readLock();
		private static final java.util.concurrent.locks.Lock writeLock = lock.writeLock();

		protected static <T1, T2, T3, R, E extends Exception> R callWithReadLock( TriFunctionWithException<T1, T2, T3, R, E> function, T1 arg0, T2 arg1, T3 arg2 ) throws E
		{
			readLock.lock();
			try
			{
				return function.apply( arg0, arg1, arg2 );
			}
			finally
			{
				readLock.unlock();
			}
		}

		protected static <T1, T2, R, E extends Exception> R callWithReadLock( BiFunctionWithException<T1, T2, R, E> function, T1 arg0, T2 arg1 ) throws E
		{
			readLock.lock();
			try
			{
				return function.apply( arg0, arg1 );
			}
			finally
			{
				readLock.unlock();
			}
		}

		protected static <T, R, E extends Exception> R callWithReadLock( FunctionWithException<T, R, E> function, T arg0 ) throws E
		{
			readLock.lock();
			try
			{
				return function.apply( arg0 );
			}
			finally
			{
				readLock.unlock();
			}
		}

		protected static <T1, T2, T3, T4, R, E extends Exception> R callWithWriteLock( QuadFunctionWithException<T1, T2, T3, T4, R, E> function, T1 arg0, T2 arg1, T3 arg2, T4 arg3 ) throws E
		{
			writeLock.lock();
			try
			{
				return function.apply( arg0, arg1, arg2, arg3 );
			}
			finally
			{
				writeLock.unlock();
			}
		}

		protected static <T1, T2, T3, R, E extends Exception> R callWithWriteLock( TriFunctionWithException<T1, T2, T3, R, E> function, T1 arg0, T2 arg1, T3 arg2 ) throws E
		{
			writeLock.lock();
			try
			{
				return function.apply( arg0, arg1, arg2 );
			}
			finally
			{
				writeLock.unlock();
			}
		}

		protected static <T1, T2, R, E extends Exception> R callWithWriteLock( BiFunctionWithException<T1, T2, R, E> function, T1 arg0, T2 arg1 ) throws E
		{
			writeLock.lock();
			try
			{
				return function.apply( arg0, arg1 );
			}
			finally
			{
				writeLock.unlock();
			}
		}

		protected static <T, R, E extends Exception> R callWithWriteLock( FunctionWithException<T, R, E> function, T arg0 ) throws E
		{
			writeLock.lock();
			try
			{
				return function.apply( arg0 );
			}
			finally
			{
				writeLock.unlock();
			}
		}

		public void readLock()
		{
			readLock.lock();
		}

		public void readUnlock()
		{
			readLock.unlock();
		}

		public void writeLock()
		{
			writeLock.lock();
		}

		public void writeUnlock()
		{
			writeLock.unlock();
		}
	}
}
