package io.amelia.support;

import com.sun.istack.internal.NotNull;
import io.amelia.lang.ObjectStackerException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings( "unchecked" )
public abstract class ObjectStackerBase<B extends ObjectStackerBase<B>>
{
	protected final List<B> children = new ArrayList<>();
	private final String localName;
	protected EnumSet<ObjectStackerWithValue.Flag> flags = EnumSet.noneOf( ObjectStackerWithValue.Flag.class );
	protected B parent;

	protected ObjectStackerBase( String localName )
	{
		this( null, localName );
	}

	protected ObjectStackerBase( B parent, String localName )
	{
		Objs.notNull( localName );

		if ( !localName.matches( "[a-z0-9_]*" ) )
			throwExceptionIgnorable( String.format( "The local name '%s' can only contain characters a-z, 0-9, and _.", localName ) );

		this.parent = parent;
		this.localName = localName;
	}

	public final B addFlag( Flag... flags )
	{
		disposeCheck();
		for ( Flag flag : flags )
		{
			if ( flag.equals( Flag.DISPOSED ) )
				throwExceptionIgnorable( "You can not set the DISPOSED flag. The flag is reserved for internal use." );
			this.flags.add( flag );
		}
		return ( B ) this;
	}

	private B child( @NotNull String localName, boolean create )
	{
		disposeCheck();
		for ( B child : children )
			if ( child.getLocalName() == null )
				children.remove( child );
			else if ( child.getLocalName().equals( localName ) )
				return child;
		return create ? createChild( localName ) : null;
	}

	public void clear()
	{
		disposeCheck();
		children.clear();

		Reflection.methodCall( Reflection.getMethod( getClass(), "clear" ) );
	}

	public final <C> Stream<C> collect( Function<B, C> function )
	{
		disposeCheck();
		return Stream.concat( Stream.of( function.apply( ( B ) this ) ), children.stream().flatMap( c -> c.collect( function ) ) ).filter( Objects::nonNull );
	}

	protected abstract B createChild( String key );

	protected void delete()
	{
		disposeCheck();
		parent.children.remove( this );
		parent = null;
		children.clear();
		flags = EnumSet.of( Flag.DISPOSED );
	}

	protected final void disposeCheck() throws ObjectStackerException.Ignorable
	{
		if ( hasFlag( Flag.DISPOSED ) )
			throwExceptionIgnorable( getNamespace() + " has been disposed." );
	}

	final B findFlag( Flag flag )
	{
		disposeCheck();
		return ( B ) ( flags.contains( flag ) ? this : parent == null ? null : parent.findFlag( flag ) );
	}

	public final B getChild( @NotNull Namespace path )
	{
		return getChild( path, true );
	}

	public final B getChild( @NotNull Namespace path, boolean create )
	{
		disposeCheck();
		Objs.notNull( path, "path can not be null" );
		if ( path.getNodeCount() == 0 )
			return ( B ) this;
		String key = path.getFirst();
		B child = child( key, create );
		return child == null ? null : path.getNodeCount() <= 1 ? child : child.getChild( path.subNamespace( 1 ), create );
	}

	public final B getChild( @NotNull String path )
	{
		return getChild( path, true );
	}

	public final B getChild( @NotNull String path, boolean create )
	{
		disposeCheck();
		Objs.notNull( path );
		return getChild( Namespace.parseString( path ), create );
	}

	public final String getChildNamespace()
	{
		disposeCheck();
		return new NamespaceParser( getNamespace() ).getSub().getString();
	}

	public final Stream<B> getChildren()
	{
		disposeCheck();
		return children.stream();
	}

	public final Stream<B> getChildrenRecursive()
	{
		disposeCheck();
		return children.stream().flatMap( ObjectStackerBase::getChildrenRecursive0 );
	}

	protected final Stream<B> getChildrenRecursive0()
	{
		disposeCheck();
		return Stream.concat( Stream.of( ( B ) this ), children.stream().flatMap( ObjectStackerBase::getChildrenRecursive0 ) );
	}

	public Flag[] getFlags()
	{
		return flags.toArray( new Flag[0] );
	}

	public final Set<String> getKeys()
	{
		disposeCheck();
		return children.stream().map( ObjectStackerBase::getLocalName ).collect( Collectors.toSet() );
	}

	public final Set<String> getKeysDeep()
	{
		disposeCheck();
		return Stream.concat( getKeys().stream(), getChildren().flatMap( n -> n.getKeysDeep().stream().map( s -> n.getLocalName() + "." + s ) ) ).sorted().collect( Collectors.toSet() );
	}

	public final String getLocalName()
	{
		return localName;
	}

	public final String getNamespace()
	{
		return getNamespaceObj().reverseOrder().getString();
	}

	public final Namespace getNamespaceObj()
	{
		disposeCheck();
		if ( Objs.isEmpty( localName ) )
			return new Namespace();
		return hasParent() ? getParent().getNamespaceObj().append( getLocalName() ) : Namespace.parseString( getLocalName() );
	}

	public final B getParent()
	{
		disposeCheck();
		return parent;
	}

	public final Stream<B> getParents()
	{
		disposeCheck();
		return Stream.of( parent ).flatMap( ObjectStackerBase::getParents0 );
	}

	protected final Stream<B> getParents0()
	{
		disposeCheck();
		return Stream.concat( Stream.of( ( B ) this ), Stream.of( parent ).flatMap( ObjectStackerBase::getParents0 ) );
	}

	public final String getRootNamespace()
	{
		disposeCheck();
		return new NamespaceParser( getNamespace() ).getTld().getString();
	}

	public final boolean hasChild( String nodes )
	{
		return getChild( nodes ) != null;
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

	protected final void putChild( B child ) throws ObjectStackerException.Ignorable
	{
		disposeCheck();
		B oldChild = getChild( child.getLocalName() );
		if ( oldChild != null )
		{
			if ( hasFlag( Flag.NO_OVERRIDE ) )
				throwExceptionIgnorable( getNamespace() + " has NO_OVERRIDE flag." );
			else
				oldChild.delete();
		}

		child.parent = ( B ) this;
		children.add( child );
	}

	public final B removeFlag( Flag... flags )
	{
		disposeCheck();
		this.flags.removeAll( Arrays.asList( flags ) );
		return ( B ) this;
	}

	public final B removeFlagRecursive( Flag... flags )
	{
		disposeCheck();
		if ( parent != null )
			parent.removeFlagRecursive( flags );
		return removeFlag( flags );
	}

	protected abstract void throwExceptionError( String message ) throws ObjectStackerException.Error;

	protected abstract void throwExceptionIgnorable( String message ) throws ObjectStackerException.Ignorable;

	public enum Flag
	{
		/* Values and children can never be written to this object */
		READ_ONLY,
		/* This object will be ignored, if there is an attempt to write it to persistent disk */
		NO_SAVE,
		/* Prevents the overwriting of existing children and values */
		NO_OVERRIDE,
		/* Prevents flags from recurring down to children */
		NO_FLAG_RECURSION,
		/* SPECIAL FLAG */
		DISPOSED
	}
}
