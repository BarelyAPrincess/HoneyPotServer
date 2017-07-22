package io.amelia.foundation.binding;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppBindings
{
	private static BindingBase bindings = new BindingBase( "" );

	public static Object call( String prefix, Map<String, Object> generics )
	{

	}

	public static Object call( Function function, Map<String, Object> generics )
	{

	}

	private static void call( Supplier<String> testMethod )
	{

	}

	public static BindingsLookup<?> lookup()
	{
		Object o = new Object();

		call( o::toString );

		return new BindingsLookup<>();
	}

	public static class BindingsLookup<T>
	{
		private Class<?> aClass = null;
		private String key = null;

		private BindingsLookup()
		{

		}

		public List<T> asList()
		{
			return asStream().collect( Collectors.toList() );
		}

		public Stream<T> asStream()
		{
			return child().collect( v -> aClass == null ? v.fetch() : v.fetch( aClass ) ).flatMap( s -> s ).map( o -> ( T ) o );
		}

		private BindingBase child()
		{
			return ( key == null ? bindings : bindings.getChild( key, true ) );
		}

		public Stream<T> collect( Function<Object, T> function )
		{
			return child().collect( v -> v.fetch( o -> aClass == null || o.getClass() == aClass ? function.apply( o ) : null ) ).flatMap( s -> s );
		}

		private <C> BindingsLookup<C> copy()
		{
			BindingsLookup<C> bindingsLookup = new BindingsLookup<>();
			bindingsLookup.key = key;
			bindingsLookup.aClass = aClass;
			return bindingsLookup;
		}

		public <C> BindingsLookup<C> filterClass( Class<C> aClass )
		{
			BindingsLookup<C> bindingsLookup = copy();
			bindingsLookup.aClass = aClass;
			return bindingsLookup;
		}

		public BindingsLookup<T> filterKey( String key )
		{
			BindingsLookup<T> bindingsLookup = copy();
			bindingsLookup.key = key;
			return bindingsLookup;
		}
	}
}
