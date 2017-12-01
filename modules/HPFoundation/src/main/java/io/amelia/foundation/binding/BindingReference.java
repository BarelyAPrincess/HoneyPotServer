package io.amelia.foundation.binding;

import com.sun.istack.internal.NotNull;
import io.amelia.support.Namespace;
import io.amelia.support.NamespaceBase;
import io.amelia.support.Objs;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class BindingReference<T extends BindingReference<T, V>, V>
{
	BindingBase binding;
	BindingReference<T, V> parent = null;
	Namespace prefix;
	Supplier<T> supplier;
	Class<V> valueClass;

	public BindingReference( @NotNull Class<V> valueClass )
	{
		this.valueClass = valueClass;
	}

	public V computeValue( Function<Optional<V>, V> function )
	{
		Supplier<Stream<V>> values = () -> binding.fetch( 1, valueClass );

		if ( values.get().count() > 1 )
			values.get().skip( 1 ).forEach( v -> binding.remove( v ) );

		Optional<V> value = values.get().findFirst();

		V returnedValue = function.apply( value );

		if ( !value.isPresent() || !Objects.equals( value, returnedValue ) )
		{
			value.ifPresent( v -> binding.remove( v ) );
			binding.store( returnedValue );
		}

		return returnedValue;
	}

	T create( BindingBase bindingBase, BindingReference parent )
	{
		@NotNull
		T bindingReference = supplier.get();
		bindingReference.parent = parent;
		return bindingReference;
	}

	T create( BindingBase bindingBase )
	{
		@NotNull
		T bindingReference = supplier.get();
		bindingReference.prefix = prefix;
		bindingReference.supplier = supplier;
		bindingReference.binding = bindingBase;
		return bindingReference;
	}

	public final T getChild( @NotNull Namespace path )
	{
		goCheck();
		@NotNull
		T bindingReference = create( binding.getChild( path ) );
		bindingReference.parent = this;
		return bindingReference;
	}

	public final T getChild( @NotNull String path )
	{
		return getChild( Namespace.parseString( path ) );
	}

	public String getChildNamespace()
	{
		goCheck();
		return binding.getDomainChild();
	}

	public Stream<T> getChildren()
	{
		goCheck();
		return binding.getChildren().filter( v -> v.fetch( 1, valueClass ).count() > 0 ).map( v -> create( v, this ) );
	}

	public final Stream<T> getChildrenRecursive()
	{
		return getChildren().flatMap( BindingReference::getChildrenRecursive0 );
	}

	protected final Stream<T> getChildrenRecursive0()
	{
		goCheck();
		return Stream.concat( Stream.of( ( T ) this ), getChildren().flatMap( BindingReference::getChildrenRecursive0 ) );
	}

	public String getLocalName()
	{
		goCheck();
		return binding.getName();
	}

	public String getNamespace()
	{
		goCheck();
		return binding.getCurrentPath().substring( prefix.getNodeCount() );
	}

	public NamespaceBase getNamespaceObj()
	{
		goCheck();
		return binding.getNamespace().subNamespace( prefix.getNodeCount() );
	}

	public boolean hasParent()
	{
		goCheck();
		return binding.getNamespace().getNodeCount() - prefix.getNodeCount() > 0;
	}

	public BindingReference<T, V> getParent()
	{
		goCheck();
		if ( parent == null && hasParent() )
			parent = create( binding.getParent() );
		return parent;
	}

	public String getRootNamespace()
	{
		return binding.getDomainTLD();
	}

	protected void goCheck()
	{
		Objs.notNull( binding );
		Objs.notNull( valueClass );
		Objs.notNull( prefix );
		Objs.notNull( supplier );
	}

	public boolean hasChildren()
	{
		goCheck();
		return binding.getChildren().filter( v -> v.fetch( 1, valueClass ).count() > 0 ).count() > 0;
	}
}
