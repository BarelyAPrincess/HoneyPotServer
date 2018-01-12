package io.amelia.foundation.binding;

import io.amelia.lang.ParcelableException;
import io.amelia.support.data.StackerWithValue;

@SuppressWarnings( "unchecked" )
public final class BindingBase extends StackerWithValue<BindingBase, Object>
{
	protected BindingBase( String key )
	{
		super( BindingBase::new, key );
	}

	protected BindingBase( BindingBase parent, String key )
	{
		super( BindingBase::new, parent, key );
	}

	protected BindingBase( BindingBase parent, String key, Object value )
	{
		super( BindingBase::new, parent, key, value );
	}

	@Override
	protected void throwExceptionError( String message ) throws BindingException.Error
	{
		throw new BindingException.Error( this, message );
	}

	@Override
	protected void throwExceptionIgnorable( String message ) throws ParcelableException.Ignorable
	{
		throw new BindingException.Ignorable( this, message );
	}
}
