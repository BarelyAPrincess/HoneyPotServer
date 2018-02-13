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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
import io.amelia.support.Maps;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;

@SuppressWarnings( "unchecked" )
public abstract class StackerBase<BaseClass extends StackerBase<BaseClass>>
{
	public static final int LISTENER_CHILD_ADD = 0x00;
	public static final int LISTENER_CHILD_REMOVE = 0x01;
	protected final List<BaseClass> children = new ArrayList<>();
	private final BiFunction<BaseClass, String, BaseClass> creator;
	private final Map<Integer, StackerListener.Container> listeners = new ConcurrentHashMap<>();
	protected EnumSet<StackerWithValue.Flag> flags = EnumSet.noneOf( StackerWithValue.Flag.class );
	protected BaseClass parent;
	protected StackerOptions stackerOptions = null;
	private String localName;

	protected StackerBase( BiFunction<BaseClass, String, BaseClass> creator, String localName )
	{
		this( creator, null, localName );
	}

	protected StackerBase( BiFunction<BaseClass, String, BaseClass> creator, BaseClass parent, String localName )
	{
		Objs.notNull( creator );
		Objs.notNull( localName );

		if ( !localName.matches( "[a-z0-9_]*" ) )
			throwExceptionIgnorable( String.format( "The local name '%s' can only contain characters a-z, 0-9, and _.", localName ) );

		this.creator = creator;
		this.parent = parent;
		this.localName = localName;
	}

	public final int addChildAddListener( StackerListener.OnChildAdd<BaseClass> function, StackerListener.Flags... flags )
	{
		return addListener( new StackerListener.Container( LISTENER_CHILD_ADD, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0] );
			}
		} );
	}

	public final int addChildRemoveListener( StackerListener.OnChildRemove<BaseClass> function, StackerListener.Flags... flags )
	{
		return addListener( new StackerListener.Container( LISTENER_CHILD_REMOVE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( BaseClass ) objs[1] );
			}
		} );
	}

	public final BaseClass addFlag( Flag... flags )
	{
		disposeCheck();
		for ( Flag flag : flags )
		{
			if ( flag.equals( Flag.DISPOSED ) )
				throwExceptionIgnorable( "You can not set the DISPOSED flag. The flag is reserved for internal use." );
			this.flags.add( flag );
		}
		return ( BaseClass ) this;
	}

	protected final int addListener( StackerListener.Container container )
	{
		return Maps.firstKeyAndPut( listeners, container );
	}

	public final <C> Stream<C> collect( Function<BaseClass, C> function )
	{
		disposeCheck();
		return Stream.concat( Stream.of( function.apply( ( BaseClass ) this ) ), children.stream().flatMap( c -> c.collect( function ) ) ).filter( Objects::nonNull );
	}

	private BaseClass createChild( String key )
	{
		BaseClass child = creator.apply( ( BaseClass ) this, key );
		children.add( child );
		fireListener( LISTENER_CHILD_ADD, child );
		return child;
	}

	protected void destroy()
	{
		disposeCheck();
		notFlag( Flag.READ_ONLY );
		removeFromParent();
		for ( BaseClass child : children )
			child.destroy();
		children.clear();
		flags = EnumSet.of( Flag.DISPOSED );
	}

	public void destroyChild( String key )
	{
		getChild( key, StackerBase::destroy );
	}

	public BaseClass destroyChildThenCreate( String key )
	{
		destroyChild( key );
		return getChildOrCreate( key );
	}

	protected final void disposeCheck() throws ApplicationException.Ignorable
	{
		if ( hasFlag( Flag.DISPOSED ) )
			throwExceptionIgnorable( getCurrentPath() + " has been disposed." );
	}

	public final BaseClass empty( @Nonnull String key )
	{
		return creator.apply( null, key );
	}

	public final BaseClass empty()
	{
		return empty( "" );
	}

	@Nullable
	protected BaseClass findChild( @Nonnull String key, boolean create )
	{
		disposeCheck();
		Objs.notNull( key );

		Namespace ns = Namespace.parseString( key, getStackerOptions().getSeparator() );
		if ( ns.getNodeCount() == 0 )
			return ( BaseClass ) this;

		String first = ns.getFirst();
		BaseClass found = null;

		for ( BaseClass child : children )
			if ( child.getName() == null )
				children.remove( child );
			else if ( first.equals( child.getName() ) )
			{
				found = child;
				break;
			}

		if ( found == null && !create )
			return null;
		if ( found == null )
			found = createChild( first );

		if ( ns.getNodeCount() <= 1 )
			return found;
		else
			return found.findChild( ns.subString( 1 ), create );
	}

	final BaseClass findFlag( Flag flag )
	{
		disposeCheck();
		return ( BaseClass ) ( flags.contains( flag ) ? this : parent == null ? null : parent.findFlag( flag ) );
	}

	void fireListener( int type, Object... objs )
	{
		fireListener( true, type, objs );
	}

	void fireListener( boolean local, int type, Object... objs )
	{
		if ( hasParent() )
			parent.fireListener( false, type, objs );
		for ( Map.Entry<Integer, StackerListener.Container> entry : listeners.entrySet() )
			if ( entry.getValue().type == type )
			{
				if ( entry.getValue().flags.contains( StackerListener.Flags.FIRE_ONCE ) )
					listeners.remove( entry.getKey() );
				if ( local || !entry.getValue().flags.contains( StackerListener.Flags.NO_RECURSIVE ) )
					Kernel.getExecutorParallel().execute( () -> entry.getValue().call( objs ) );
			}
	}

	/**
	 * Calculates a specific child a key is referencing.
	 *
	 * @param key      The dot separator namespace
	 * @param consumer Action to perform
	 */
	public void getChild( String key, Consumer<BaseClass> consumer )
	{
		consumer.accept( findChild( key, false ) );
	}

	@Nullable
	public final BaseClass getChild( @Nonnull String key )
	{
		return findChild( key, false );
	}

	public final Stream<BaseClass> getChild( @Nonnull Predicate<BaseClass> predicate )
	{
		return children.stream().flatMap( child -> child.getChild( predicate ) );
	}

	public void getChildIfPresent( String key, Consumer<BaseClass> consumer )
	{
		BaseClass child = findChild( key, false );
		if ( child != null )
			consumer.accept( child );
	}

	@Nonnull
	public <R> R getChildOrCreate( String key, Function<BaseClass, R> function )
	{
		return function.apply( findChild( key, true ) );
	}

	public void getChildOrCreate( String key, Consumer<BaseClass> consumer )
	{
		consumer.accept( findChild( key, true ) );
	}

	public final BaseClass getChildOrCreate( @Nonnull String key )
	{
		return findChild( key, true );
	}

	public final Stream<BaseClass> getChildren()
	{
		disposeCheck();
		return children.stream();
	}

	public final Stream<BaseClass> getChildrenRecursive()
	{
		disposeCheck();
		return children.stream().flatMap( StackerBase::getChildrenRecursive0 );
	}

	protected final Stream<BaseClass> getChildrenRecursive0()
	{
		disposeCheck();
		return Stream.concat( Stream.of( ( BaseClass ) this ), children.stream().flatMap( StackerBase::getChildrenRecursive0 ) );
	}

	public final String getCurrentPath()
	{
		return getNamespace().reverseOrder().getString();
	}

	public final String getDomainChild()
	{
		disposeCheck();
		return Namespace.parseDomain( getCurrentPath() ).getChild().getString();
	}

	public final String getDomainTLD()
	{
		disposeCheck();
		return Namespace.parseDomain( getCurrentPath() ).getTld().getString();
	}

	public Flag[] getFlags()
	{
		return flags.toArray( new Flag[0] );
	}

	public final Set<String> getKeys()
	{
		disposeCheck();
		return children.stream().map( StackerBase::getName ).collect( Collectors.toSet() );
	}

	public final Set<String> getKeysDeep()
	{
		disposeCheck();
		return Stream.concat( getKeys().stream(), getChildren().flatMap( n -> n.getKeysDeep().stream().map( s -> n.getName() + "." + s ) ) ).sorted().collect( Collectors.toSet() );
	}

	/**
	 * Gets the name of this individual {@link BaseClass}, in the path.
	 *
	 * @return Name of this node
	 */
	public final String getName()
	{
		return localName;
	}

	public final Namespace getNamespace()
	{
		disposeCheck();
		if ( Objs.isEmpty( localName ) )
			return new Namespace();
		return hasParent() ? getParent().getNamespace().append( getName() ) : Namespace.parseString( getName(), getStackerOptions().getSeparator() );
	}

	public final BaseClass getParent()
	{
		disposeCheck();
		return parent;
	}

	public final Stream<BaseClass> getParents()
	{
		disposeCheck();
		return Stream.of( parent ).flatMap( StackerBase::getParents0 );
	}

	protected final Stream<BaseClass> getParents0()
	{
		disposeCheck();
		return Stream.concat( Stream.of( ( BaseClass ) this ), Stream.of( parent ).flatMap( StackerBase::getParents0 ) );
	}

	public final BaseClass getRoot()
	{
		return parent == null ? ( BaseClass ) this : parent.getRoot();
	}

	protected StackerOptions getStackerOptions()
	{
		if ( parent != null )
			return parent.getStackerOptions();
		if ( stackerOptions == null )
			stackerOptions = new StackerOptions();
		return stackerOptions;
	}

	public final boolean hasChild( String key )
	{
		return getChild( key ) != null;
	}

	public final boolean hasChildren()
	{
		return children.size() > 0;
	}

	protected final boolean hasFlag( Flag flag )
	{
		return flags.contains( flag ) || ( parent != null && !parent.hasFlag( Flag.NO_FLAG_RECURSION ) && parent.hasFlag( flag ) );
	}

	public final boolean hasParent()
	{
		return parent != null;
	}

	public final boolean isDisposed()
	{
		return hasFlag( Flag.DISPOSED );
	}

	/**
	 * Expects subclasses to override this method and add new checks.
	 *
	 * @see #trimChildren();
	 */
	protected boolean isTrimmable()
	{
		for ( BaseClass child : children )
			if ( !child.isTrimmable() )
				return false;
		return true;
	}

	public void mergeChild( @Nonnull BaseClass child )
	{
		disposeCheck();

		Objs.notNull( child );
		notFlag( Flag.READ_ONLY );

		for ( BaseClass oldChild : child.children )
		{
			BaseClass newChild = getChild( oldChild.getName() );
			if ( newChild == null )
				setChild( oldChild );
			else
			{
				notFlag( Flag.NO_OVERRIDE );
				newChild.notFlag( Flag.READ_ONLY );
				newChild.mergeChild( oldChild );
			}
		}

		flags.addAll( child.flags );
	}

	public final BaseClass move( @Nonnull String movePath )
	{
		return move( movePath, "/" );
	}

	public final BaseClass move( @Nonnull String movePath, @Nonnull String separator )
	{
		Objs.notEmpty( movePath );

		if ( separator.equals( "." ) )
			throwExceptionIgnorable( "Move path separator can't be a period." );

		Namespace ns = Namespace.parseString( movePath, separator );

		BaseClass newParent = parent;

		for ( int i = 0; i < ns.getNodeCount(); i++ )
		{
			String node = ns.getNode( i );

			if ( Objs.isEmpty( node ) )
			{
				if ( i == 0 ) // First node is empty, so remove from parent.
					newParent = null;
				// Otherwise, do nothing.
			}
			else if ( node.equals( ".." ) )
			{
				if ( hasParent() ) // We have a parent, so shift us to the parent of the parent.
					newParent = parent.parent;
			}
			else
			{
				if ( i == ns.getNodeCount() - 1 ) // We're on the last node, so set this to our new localName.
					this.localName = node;
				else
					newParent = newParent == null ? empty( node ) : newParent.getChildOrCreate( node ); // Shift us one child down
			}
		}

		if ( newParent != parent )
		{
			removeFromParent();
			if ( newParent != null )
				newParent.setChild( ( BaseClass ) this );
		}

		return ( BaseClass ) this;
	}

	public void notFlag( Flag flag )
	{
		if ( hasFlag( flag ) )
			throwExceptionIgnorable( getCurrentPath() + " has " + flag.name() + " flag." );
	}

	/**
	 * Polls a child from this stacker. If it exists, it's removed from it's parent.
	 *
	 * @param key The child key
	 *
	 * @return found instance or null if does not exist.
	 */
	public BaseClass pollChild( String key )
	{
		BaseClass child = getChild( key );
		if ( child != null )
			child.removeFromParent();
		return child;
	}

	public final void removeAllListeners()
	{
		listeners.clear();
	}

	public final BaseClass removeFlag( Flag... flags )
	{
		disposeCheck();
		this.flags.removeAll( Arrays.asList( flags ) );
		return ( BaseClass ) this;
	}

	public final BaseClass removeFlagRecursive( Flag... flags )
	{
		disposeCheck();
		if ( parent != null )
			parent.removeFlagRecursive( flags );
		return removeFlag( flags );
	}

	public final BaseClass removeFromParent()
	{
		if ( hasParent() )
		{
			parent.notFlag( Flag.READ_ONLY );
			parent.children.remove( this );
			fireListener( LISTENER_CHILD_REMOVE, parent, this );
			parent = null;
		}
		return ( BaseClass ) this;
	}

	public final void removeListener( int inx )
	{
		listeners.remove( inx );
	}

	public final void setChild( @Nonnull BaseClass child )
	{
		setChild( child, false );
	}

	public final void setChild( @Nonnull BaseClass child, boolean mergeWithExisting )
	{
		disposeCheck();
		Objs.notNull( child );

		notFlag( Flag.READ_ONLY );

		BaseClass oldChild = getChild( child.getName() );
		if ( oldChild != null )
		{
			notFlag( Flag.NO_OVERRIDE );

			if ( mergeWithExisting )
			{
				oldChild.mergeChild( child );
				return;
			}
			else
				oldChild.destroy();
		}

		child.removeFromParent();

		children.add( child );
		child.parent = ( BaseClass ) this;
		child.stackerOptions = null;
	}

	public final void setChild( @Nonnull String key, @Nonnull BaseClass child )
	{
		setChild( key, child, false );
	}

	public final void setChild( @Nonnull String key, @Nonnull BaseClass child, boolean mergeWithExisting )
	{
		disposeCheck();
		Objs.notEmpty( key );
		Objs.notNull( child );

		getChildOrCreate( key ).setChild( child, mergeWithExisting );
	}

	public int size()
	{
		return children.size();
	}

	public int sizeOf( String key )
	{
		BaseClass keyChild = getChild( key );
		return keyChild == null ? -1 : keyChild.size();
	}

	protected abstract void throwExceptionError( String message ) throws ApplicationException.Error;

	protected abstract void throwExceptionIgnorable( String message ) throws ApplicationException.Ignorable;

	/**
	 * Attempts to remove each sub-node based on if {@link #isTrimmable()} returns true.
	 */
	public void trimChildren()
	{
		for ( BaseClass child : children )
			if ( child.isTrimmable() )
				child.destroy();
			else
				child.trimChildren();
	}

	public enum Flag
	{
		/* Values and children can never be written to this object */
		READ_ONLY,
		/* This object will be ignored, if there is an attempt to write it to persistent disk */
		NO_SAVE,
		/* Prevents the overwriting of existing children and values */
		NO_OVERRIDE,
		/* Prevents flags from recurring to children */
		NO_FLAG_RECURSION,
		/* SPECIAL FLAG */
		DISPOSED
	}

	public class StackerOptions
	{
		private String separator = ".";

		public String getSeparator()
		{
			return separator;
		}

		public void setSeparator( String separator )
		{
			this.separator = separator;
		}
	}
}
