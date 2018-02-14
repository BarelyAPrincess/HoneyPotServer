/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.foundation.Kernel;
import io.amelia.support.Objs;
import io.amelia.support.Pair;

@SuppressWarnings( "unchecked" )
public abstract class StackerWithValue<BaseClass extends StackerWithValue<BaseClass, ValueType>, ValueType> extends StackerBase<BaseClass>
{
	public static final int LISTENER_VALUE_CHANGE = 0x02;
	public static final int LISTENER_VALUE_STORE = 0x03;
	public static final int LISTENER_VALUE_REMOVE = 0x04;
	protected volatile ValueType value;

	protected StackerWithValue( BiFunction<BaseClass, String, BaseClass> creator, String key )
	{
		this( creator, null, key, null );
	}

	protected StackerWithValue( BiFunction<BaseClass, String, BaseClass> creator, BaseClass parent, String key )
	{
		this( creator, parent, key, null );
	}

	protected StackerWithValue( BiFunction<BaseClass, String, BaseClass> creator, BaseClass parent, String key, ValueType value )
	{
		super( creator, parent, key );
		this.value = value;
	}

	public final int addValueChangeListener( StackerListener.OnValueChange<BaseClass, ValueType> function, StackerListener.Flags... flags )
	{
		return addListener( new StackerListener.Container( LISTENER_VALUE_CHANGE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( ValueType ) objs[1], ( ValueType ) objs[2] );
			}
		} );
	}

	public final int addValueRemoveListener( StackerListener.OnValueRemove<BaseClass, ValueType> function, StackerListener.Flags... flags )
	{
		return addListener( new StackerListener.Container( LISTENER_VALUE_REMOVE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( ValueType ) objs[1] );
			}
		} );
	}

	public final int addValueStoreListener( StackerListener.OnValueStore<BaseClass, ValueType> function, StackerListener.Flags... flags )
	{
		return addListener( new StackerListener.Container( LISTENER_VALUE_STORE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( ValueType ) objs[1] );
			}
		} );
	}

	public <O> O asObject( Class<O> cls )
	{
		try
		{
			Constructor<?> constructor = cls.getConstructor( StackerBase.class );
			return ( O ) constructor.newInstance( this );
		}
		catch ( Exception ignore )
		{
		}

		try
		{
			Constructor<?> constructor = cls.getConstructor();
			Object instance = constructor.newInstance();

			for ( Field field : cls.getFields() )
			{
				BaseClass child = findChild( field.getName(), false );

				if ( child == null )
				{
					Kernel.L.warning( "Could not assign field " + field.getName() + " with type " + field.getType() + " within class " + cls.getSimpleName() + "." );
					continue;
				}

				field.setAccessible( true );

				Object obj = child.getValue().orElse( null );
				boolean assigned = true;

				if ( obj != null )
				{
					if ( obj.getClass().isAssignableFrom( field.getType() ) )
						field.set( instance, obj );
					else if ( String.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToString( value ) );
					else if ( Double.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToDouble( value ) );
					else if ( Integer.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToInt( value ) );
					else if ( Long.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToLong( value ) );
					else if ( Boolean.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToBoolean( value ) );
					else
						assigned = false;
				}

				if ( field.get( instance ) == null )
					assigned = false;

				if ( !assigned )
				{
					Object o = child.asObject( field.getType() );
					if ( o == null )
						Kernel.L.severe( "Could not cast field " + field.getName() + " with type " + field.getType() + " with value " + value.getClass().getSimpleName() + " within class" + cls.getSimpleName() + "." );
					else
						field.set( instance, o );
				}
			}

			//for ( Field field : cls.getFields() )
			//	if ( field.get( instance ) == null && !field.isSynthetic() )
			//		Kernel.L.warning( "The field " + field.getProductName() + " is unassigned for object " + cls );

			return ( O ) instance;
		}
		catch ( Exception ignore )
		{
		}

		return null;
	}

	@Override
	public void destroy()
	{
		super.destroy();
		updateValue( null );
	}

	public Stream<ValueType> flatValues()
	{
		disposeCheck();
		Stream<ValueType> stream = children.stream().flatMap( StackerWithValue::flatValues );
		return Optional.ofNullable( value ).map( t -> Stream.concat( Stream.of( t ), stream ) ).orElse( stream );
	}

	public <LT extends ValueType> List<LT> getChildAsList( String key, Class<LT> type )
	{
		BaseClass child = getChild( key );
		if ( child == null )
			return null;
		return child.children.stream().map( c -> Objs.castTo( c.value, type ) ).filter( Objects::nonNull ).collect( Collectors.toList() );
	}

	public <LT extends ValueType> List<LT> getChildAsList( String key )
	{
		BaseClass child = getChild( key );
		if ( child == null )
			return null;
		return child.children.stream().map( c -> ( LT ) c.value ).filter( Objects::nonNull ).collect( Collectors.toList() );
	}

	public <T> T getChildAsObject( @Nonnull String key, Class<T> cls )
	{
		BaseClass child = findChild( key, false );
		if ( child == null )
			return null;
		return child.asObject( cls );
	}

	public <ExpectedValueType extends ValueType> Map<String, ExpectedValueType> getChildrenAsMap()
	{
		return children.stream().collect( Collectors.toMap( StackerBase::getName, c -> ( ExpectedValueType ) c.value ) );
	}

	public <ExpectedValueType extends ValueType> Map<String, ExpectedValueType> getChildrenAsMap( String key )
	{
		BaseClass child = getChild( key );
		if ( child == null )
			return null;
		return child.getChildrenAsMap();
	}

	public <ExpectedValueType extends ValueType> Stream<Pair<String, ExpectedValueType>> getChildrenWithKeys()
	{
		return children.stream().map( c -> new Pair( c.getName(), c.value ) );
	}

	public ValueType getValue( String key, Function<ValueType, ValueType> computeFunction )
	{
		return findChild( key, true ).getValue( computeFunction );
	}

	public ValueType getValue( Function<ValueType, ValueType> computeFunction )
	{
		ValueType val = computeFunction.apply( value );
		if ( val != value )
			setValue( val );
		return val;
	}

	public ValueType getValue( String key, Supplier<ValueType> supplier )
	{
		return findChild( key, true ).getValue( supplier );
	}

	public ValueType getValue( Supplier<ValueType> supplier )
	{
		if ( value == null )
			setValue( supplier.get() );
		return value;
	}

	public Optional<ValueType> getValue( String key )
	{
		BaseClass child = findChild( key, false );
		return Optional.ofNullable( child == null ? null : child.value );
	}

	public Optional<ValueType> getValue()
	{
		disposeCheck();
		return Optional.ofNullable( value );
	}

	public void setValue( ValueType value )
	{
		disposeCheck();
		notFlag( Flag.READ_ONLY );
		if ( hasFlag( Flag.NO_OVERRIDE ) && hasValue() )
			throwExceptionIgnorable( getCurrentPath() + " has NO_OVERRIDE flag" );
		updateValue( value );
	}

	public void getValueIfPresent( String key, Consumer<ValueType> consumer )
	{
		BaseClass child = findChild( key, false );
		if ( child != null && child.value != null )
			consumer.accept( child.value );
	}

	public final boolean hasValue( String key )
	{
		BaseClass child = findChild( key, false );
		return child != null && child.hasValue();
	}

	public boolean hasValue()
	{
		return value != null;
	}

	@Override
	protected boolean isTrimmable()
	{
		return super.isTrimmable() && !hasValue();
	}

	@Override
	public void mergeChild( @Nonnull BaseClass child )
	{
		super.mergeChild( child );
		updateValue( child.value );
	}

	public Optional<ValueType> pollValue( String key )
	{
		BaseClass child = findChild( key, false );
		if ( child == null )
			return Optional.empty();
		else
		{
			ValueType value = child.value;
			updateValue( null );
			return Optional.ofNullable( value );
		}
	}

	public void setValue( String key, ValueType value )
	{
		getChildOrCreate( key ).setValue( value );
	}

	public void setValueIfAbsent( ValueType value )
	{
		if ( !hasValue() )
			setValue( value );
	}

	public void setValueIfAbsent( String key, ValueType value )
	{
		getChildOrCreate( key ).setValueIfAbsent( value );
	}

	/**
	 * Used privately to update the value and call the proper value listener.
	 */
	protected void updateValue( ValueType value )
	{
		if ( this.value == null && value != null )
			fireListener( LISTENER_VALUE_STORE, value );
		if ( this.value != null && value == null )
			fireListener( LISTENER_VALUE_REMOVE, this.value );
		fireListener( LISTENER_VALUE_CHANGE, this.value, value );
		this.value = value;
	}

	public Map<String, ValueType> values()
	{
		disposeCheck();
		return children.stream().filter( StackerWithValue::hasValue ).collect( Collectors.toMap( StackerWithValue::getName, c -> c.getValue().orElse( null ) ) );
	}
}
