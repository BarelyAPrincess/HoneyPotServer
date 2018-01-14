package io.amelia.foundation.binding;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.foundation.Foundation;
import io.amelia.foundation.Kernel;
import io.amelia.support.Maps;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;

public class Bindings implements WritableBinding
{
	public static final Kernel.Logger L = Kernel.getLogger( Bindings.class );
	/**
	 * Privately bound SELF that allows internal code to make changes to the bindings without restrictions.
	 * Public access to restricted to the static methods contained.
	 */
	private static final Bindings SELF = new Bindings();
	private static final BindingReference bindings = new BindingReference( "" );
	private static final Map<String, WeakReference<BoundNamespace>> boundNamespaces = new HashMap<>();
	private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private static final Lock readLock = lock.readLock();
	private static final List<BindingResolver> resolvers = new ArrayList<>();
	private static final SharedNamespace systemNamespace = new SharedNamespace( "io.amelia" );
	private static final Lock writeLock = lock.writeLock();

	/**
	 * Returns a {@link BoundNamespace} for the requested namespace.
	 * Bindings can only be updated using the returned instance.
	 * Binding the namespace allows for only the requesting code to make changes to requested namespace bindings.
	 * <p>
	 * Be sure not to keep reference to the returned binding or the binding could and will be automatically unbound and disposed of at any moment.
	 * <p>
	 * Setter:
	 * <pre>
	 * {
	 *      NamespaceBinding nb = Bindings.bindNamespace("com.google.somePlugin");
	 *      nb.setObject("facade", new SomeFacadeService());
	 *      nb.setObject("obj", new Object());
	 * }
	 * </pre>
	 * <p>
	 * Getter:
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
	 *
	 * @param namespace The namespace you wish to bind.
	 *
	 * @return The namespace binder.
	 */
	public static BoundNamespace bindNamespace( @Nonnull String namespace ) throws BindingException.Error
	{
		readLock.lock();
		try
		{
			Namespace ns = Namespace.parseString( namespace ).fixInvalidChars().normalizeAscii();
			if ( ns.startsWith( "io.amelia" ) )
				throw new BindingException.Error( "Namespace \"io.amelia\" is reserved for internal use only." );
			if ( ns.getNodeCount() < 3 )
				throw new BindingException.Error( "Namespaces can only be bound with no less than 3 nodes." );
			namespace = ns.getString();

			for ( Map.Entry<String, WeakReference<BoundNamespace>> entry : boundNamespaces.entrySet() )
				if ( namespace.startsWith( entry.getKey() ) && entry.getValue().get() != null )
					throw new BindingException.Error( "That namespace (or parent there of) is already bound!" );

			BoundNamespace boundNamespace = new BoundNamespace( namespace );
			boundNamespaces.put( namespace, new WeakReference<>( boundNamespace ) );
			return boundNamespace;
		}
		finally
		{
			readLock.unlock();
		}
	}

	static BindingReference getChild( @Nonnull String namespace )
	{
		return bindings.getChild( namespace );
	}

	static BindingReference getChildOrCreate( @Nonnull String namespace )
	{
		return bindings.getChildOrCreate( namespace );
	}

	private static List<BindingResolver> getResolvers()
	{
		return getResolvers( null );
	}

	private static List<BindingResolver> getResolvers( String namespace )
	{
		readLock.lock();
		try
		{
			List<BindingResolver> list = new ArrayList<>();
			namespace = normalizeNamespace( namespace );

			for ( Map.Entry<String, WeakReference<BoundNamespace>> entry : boundNamespaces.entrySet() )
				if ( ( namespace == null || namespace.startsWith( entry.getKey() ) ) && entry.getValue().get() != null )
				{
					BindingResolver bindingResolver = entry.getValue().get().getResolver();
					if ( bindingResolver != null )
						list.add( bindingResolver );
				}

			resolvers.sort( new BindingResolver.Comparator() );

			for ( BindingResolver bindingResolver : resolvers )
				if ( namespace == null || namespace.startsWith( bindingResolver.baseNamespace ) )
					list.add( bindingResolver );

			return list;
		}
		finally
		{
			readLock.unlock();
		}
	}

	public static SharedNamespace getSharedNamespace( @Nonnull String namespace )
	{
		readLock.lock();
		try
		{
			namespace = Namespace.parseString( namespace ).fixInvalidChars().getString();

			// TODO Check if the namespace is allowed to be publicly writable.
			if ( true )
				return new WritableSharedNamespace( namespace );
			return new SharedNamespace( namespace );
		}
		finally
		{
			readLock.unlock();
		}
	}

	public static SharedNamespace getSystemNamespace()
	{
		return systemNamespace;
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
						bindings.getChildOrCreate( namespace ).setValue( obj );

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
					bindings.getChildOrCreate( namespace ).setValue( obj );

				return obj;
			}
			catch ( IllegalAccessException | InvocationTargetException | BindingException.Error e )
			{
				e.printStackTrace();
			}

		return null;
	}

	static boolean isBound0( @Nonnull BoundNamespace boundNamespace )
	{
		for ( WeakReference<BoundNamespace> ref : boundNamespaces.values() )
			if ( ref.get() == boundNamespace )
				return true;
		return false;
	}

	static boolean isRegistered0( @Nonnull BindingResolver bindingResolver )
	{
		return resolvers.contains( bindingResolver );
	}

	static String normalizeNamespace( @Nonnull String namespace )
	{
		return Namespace.parseString( namespace ).fixInvalidChars().normalizeAscii().getString();
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
				obj = SELF.getObject( ns );
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

	static void unbind0( BoundNamespace boundNamespace )
	{
		Maps.removeIf( boundNamespaces, ( key, value ) -> value.get() == boundNamespace );
	}

	private Bindings()
	{
		// Private Access
	}

	@Override
	public String getBaseNamespace()
	{
		return "";
	}
}
