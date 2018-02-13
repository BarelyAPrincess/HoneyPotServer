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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.amelia.support.Objs;
import io.amelia.support.StringLengthComparator;
import io.amelia.support.Strs;

/**
 * <pre>
 * public Object exampleMethod( @BindingClass( Logger.class ) Object obj )
 * {
 *
 * }
 * </pre>
 */
public abstract class BindingResolver
{
	private final Map<Class<?>, Class<?>> classToClassMappings = new HashMap<>();
	private final Map<Class<?>, String> classToNamespaceMappings = new HashMap<>();
	private final Map<String, Object> instances = new HashMap<>();
	private final Map<String, String> namespaceToNamespaceMappings = new HashMap<>();
	private final Map<String, Supplier<Object>> suppliers = new HashMap<>();

	@Nonnull
	String baseNamespace = "";
	private String defaultKey = null;

	protected final void addAlias( String sourceNamespace, String targetNamespace )
	{
		sourceNamespace = Bindings.normalizeNamespace( sourceNamespace );
		if ( sourceNamespace.startsWith( baseNamespace ) )
			sourceNamespace = Strs.trimAll( sourceNamespace.substring( baseNamespace.length() ), '.' );

		targetNamespace = Bindings.normalizeNamespace( targetNamespace );
		if ( targetNamespace.startsWith( baseNamespace ) )
			targetNamespace = Strs.trimAll( targetNamespace.substring( baseNamespace.length() ), '.' );

		namespaceToNamespaceMappings.put( sourceNamespace, targetNamespace );
	}

	protected final void addAlias( Class<?> sourceClass, String targetNamespace )
	{
		targetNamespace = Bindings.normalizeNamespace( targetNamespace );
		if ( targetNamespace.startsWith( baseNamespace ) )
			targetNamespace = Strs.trimAll( targetNamespace.substring( baseNamespace.length() ), '.' );

		classToNamespaceMappings.put( sourceClass, targetNamespace );
	}

	protected final void addAlias( Class<?> sourceClass, Class<?> targetClass )
	{
		classToClassMappings.put( sourceClass, targetClass );
	}

	protected <T> T get( @Nonnull String namespace, @Nonnull final String key, @Nonnull Class<T> expectedClass )
	{
		Object obj = null;

		// TODO WARNING! It's possible that a subclass could crash the application by making looping aliases. We should prevent this!
		if ( namespaceToNamespaceMappings.containsKey( key ) )
			obj = get( namespace, namespaceToNamespaceMappings.get( key ), expectedClass );

		// Check for already instigated instances.
		if ( obj == null && instances.containsKey( key ) )
			try
			{
				obj = instances.get( key );
			}
			catch ( ClassCastException e )
			{
				// Ignore
			}

		if ( obj == null && suppliers.containsKey( key ) )
			try
			{
				obj = suppliers.get( key );
			}
			catch ( ClassCastException e )
			{
				// Ignore
			}

		if ( obj == null )
			obj = Bindings.invokeMethods( this, method -> {
				if ( expectedClass.isAssignableFrom( method.getReturnType() ) )
				{
					if ( method.isAnnotationPresent( ProvidesBinding.class ) )
					{
						String provides = method.getAnnotation( ProvidesBinding.class ).value();
						String fullNamespace = Bindings.normalizeNamespace( provides );
						if ( fullNamespace.equals( namespace ) || Strs.trimStart( fullNamespace, baseNamespace ).equals( namespace ) || provides.equals( key ) )
							return true;
					}
					return method.getName().equals( key );
				}
				return false;
			}, namespace );

		if ( obj == null )
			obj = Bindings.invokeFields( this, field -> {
				if ( expectedClass.isAssignableFrom( field.getType() ) )
				{
					if ( field.isAnnotationPresent( ProvidesBinding.class ) )
					{
						String provides = field.getAnnotation( ProvidesBinding.class ).value();
						String fullNamespace = Bindings.normalizeNamespace( provides );
						if ( fullNamespace.equals( namespace ) || Strs.trimStart( fullNamespace, baseNamespace ).equals( namespace ) || provides.equals( key ) )
							return true;
					}
					return field.getName().equals( key );
				}
				return false;
			}, namespace );

		return ( T ) obj;
	}

	protected <T> T get( @Nonnull String namespace, @Nonnull Class<T> expectedClass )
	{
		Objs.notNull( namespace );
		Objs.notNull( expectedClass );

		// If the requested key is empty, we use the default
		if ( Objs.isEmpty( namespace ) )
			if ( Objs.isEmpty( defaultKey ) )
				return null;
			else
				namespace = defaultKey;

		String subNamespace;
		if ( namespace.startsWith( baseNamespace ) )
			subNamespace = Strs.trimAll( namespace.substring( baseNamespace.length() ), '.' );
		else
		{
			namespace = baseNamespace + "." + namespace;
			subNamespace = namespace;
		}

		// Convert namespaces to friendly keys
		Object obj = get( namespace, Strs.toCamelCase( subNamespace ), expectedClass );

		if ( obj == null )
			obj = get( namespace, Strs.toCamelCase( namespace ), expectedClass );

		return ( T ) obj;
	}

	/**
	 * Called when a class needs resolving.
	 * Each registered resolver will be called for this purpose, first to return non-null will succeed.
	 *
	 * @param expectedClass
	 * @param <T>
	 *
	 * @return
	 */
	protected <T> T get( @Nonnull Class<T> expectedClass )
	{
		Objs.notNull( expectedClass );

		Object obj = null;

		if ( classToClassMappings.containsKey( expectedClass ) )
			obj = get( classToClassMappings.get( expectedClass ) );

		if ( obj == null && classToNamespaceMappings.containsKey( expectedClass ) )
			obj = get( classToNamespaceMappings.get( expectedClass ), expectedClass );

		if ( obj == null )
			obj = Bindings.invokeMethods( this, method -> expectedClass.isAssignableFrom( method.getReturnType() ) );

		if ( obj == null )
			obj = Bindings.invokeFields( this, field -> expectedClass.isAssignableFrom( field.getType() ) );

		return ( T ) obj;
	}

	public boolean isRegistered()
	{
		return Bindings.resolvers.contains( this );
	}

	protected void setDefault( String defaultKey )
	{
		this.defaultKey = defaultKey;
	}

	public static class Comparator implements java.util.Comparator<BindingResolver>
	{
		private StringLengthComparator comparator;

		public Comparator()
		{
			this( true );
		}

		public Comparator( boolean ascendingOrder )
		{
			comparator = new StringLengthComparator( ascendingOrder );
		}

		@Override
		public int compare( BindingResolver left, BindingResolver right )
		{
			return comparator.compare( left.baseNamespace, right.baseNamespace );
		}
	}
}
