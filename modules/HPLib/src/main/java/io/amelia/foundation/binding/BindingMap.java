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

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.lang.ParcelableException;
import io.amelia.support.data.StackerWithValue;

@SuppressWarnings( "unchecked" )
public final class BindingMap extends StackerWithValue<BindingMap, BindingMap.BaseBinding>
{
	WeakReference<WritableBinding> owner = null;

	protected BindingMap( String key )
	{
		super( BindingMap::new, key );
	}

	protected BindingMap( BindingMap parent, String key )
	{
		super( BindingMap::new, parent, key );
	}

	public Stream<BaseBinding> findValues( Class<?> valueClass )
	{
		return getChildrenRecursive().filter( child -> child.hasValue() && valueClass.isAssignableFrom( child.getValue().get().getObjClass() ) ).map( map -> map.value );
	}

	public <T> Optional<T> getValue( String key, Class<T> cls )
	{
		return getValue( key ).filter( obj -> cls.isAssignableFrom( obj.getClass() ) ).map( obj -> ( T ) obj );
	}

	public boolean isPrivatized()
	{
		return ( owner != null && owner.get() != null || parent != null && parent.isPrivatized() );
	}

	public void privatize( WritableBinding writableBinding ) throws BindingException.Denied
	{
		if ( isPrivatized() )
			throw new BindingException.Denied( "Namespace \"" + getNamespace().getString() + "\" has already been privatized." );
		// Indented to unprivatize children and notify the potentual owners.
		unprivatize();
		owner = new WeakReference<>( writableBinding );
	}

	public <S> void set( @Nonnull Class<S> objClass, @Nonnull Supplier<S> objSupplier )
	{
		setValue( new BaseBinding( objClass, objSupplier ) );
	}

	public void set( Object obj )
	{
		if ( obj == null )
			setValue( null );
		else
			setValue( new BaseBinding( obj ) );
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

	private void unprivatize()
	{
		getChildren().forEach( BindingMap::unprivatize );
		if ( owner != null && owner.get() != null )
			owner.get().destroy();
	}

	/**
	 * Called from the WritableBinding
	 */
	void unprivatize( WritableBinding writableBinding )
	{
		if ( owner != null && owner.get() != null && owner.get() == writableBinding )
			owner = null;
	}

	class BaseBinding<S>
	{
		Class<S> objClass;
		S objInstance = null;
		Supplier<S> objSupplier = null;

		BaseBinding( @Nonnull S objInstance )
		{
			this.objClass = ( Class<S> ) objInstance.getClass();
			this.objInstance = objInstance;
		}

		BaseBinding( @Nonnull Class<S> objClass, @Nonnull Supplier<S> objSupplier )
		{
			this.objClass = objClass;
			this.objSupplier = objSupplier;
		}

		public S getInstance()
		{
			if ( objInstance == null )
				objInstance = objSupplier.get();
			return objInstance;
		}

		public Class<S> getObjClass()
		{
			return objClass;
		}
	}
}
