package io.amelia.foundation.binding;

import java.util.Optional;
import java.util.stream.Stream;

import io.amelia.lang.ParcelableException;
import io.amelia.support.data.StackerWithValue;

@SuppressWarnings( "unchecked" )
public final class BindingReference extends StackerWithValue<BindingReference, Object>
{
	protected BindingReference( String key )
	{
		super( BindingReference::new, key );
	}

	protected BindingReference( BindingReference parent, String key )
	{
		super( BindingReference::new, parent, key );
	}

	protected BindingReference( BindingReference parent, String key, Object value )
	{
		super( BindingReference::new, parent, key, value );
	}

	public Stream<BindingReference> findValues( Class<?> valueClass )
	{
		return getChild( child -> child.hasValue() && valueClass.isAssignableFrom( child.getValue().getClass() ) );
	}

	public <T> Optional<T> getValue( String key, Class<T> cls )
	{
		return getValue( key ).filter( obj -> cls.isAssignableFrom( obj.getClass() ) ).map( obj -> ( T ) obj );
	}

	@Override
	protected void throwExceptionError( String message ) throws BindingException.Error
	{
		// TODO Include node in exception
		throw new BindingException.Error( message );
	}

	@Override
	protected void throwExceptionIgnorable( String message ) throws ParcelableException.Ignorable
	{
		throw new BindingException.Ignorable( message );
	}
}
