/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class NamespaceBase<T extends NamespaceBase> implements Cloneable
{
	public static final Pattern RANGE_EXPRESSION = Pattern.compile( "(0-9+)-(0-9+)" );

	private final NotNullFunction<String[], T> creator;

	protected String[] nodes;

	private String glue;

	protected NamespaceBase( NotNullFunction<String[], T> creator, String glue, String[] nodes )
	{
		this.creator = creator;
		this.glue = glue;
		this.nodes = Strs.toLowerCase( nodes );
	}

	protected NamespaceBase( NotNullFunction<String[], T> creator, String glue, List<String> nodes )
	{
		this.creator = creator;
		this.glue = glue;
		this.nodes = Strs.toLowerCase( nodes.toArray( new String[0] ) );
	}

	protected NamespaceBase( NotNullFunction<String[], T> creator, String glue )
	{
		this.creator = creator;
		this.glue = glue;
		this.nodes = new String[0];
	}

	@SuppressWarnings( "unchecked" )
	public T append( String... nodes )
	{
		if ( nodes.length == 0 )
			throw new IllegalArgumentException( "Nodes are empty" );
		if ( nodes.length == 1 )
			nodes = splitString( nodes[0] );
		this.nodes = Arrs.merge( this.nodes, nodes );
		return ( T ) this;
	}

	public T appendNew( String... nodes )
	{
		if ( nodes.length == 0 )
			throw new IllegalArgumentException( "Nodes are empty" );
		if ( nodes.length == 1 )
			nodes = splitString( nodes[0] );
		return creator.apply( Arrs.merge( this.nodes, nodes ) );
	}

	@Override
	public T clone()
	{
		return creator.apply( nodes );
	}

	/**
	 * Checks is namespace only contains valid characters.
	 *
	 * @return True if namespace contains only valid characters
	 */
	public boolean containsOnlyValidChars()
	{
		for ( String n : nodes )
			if ( !n.matches( "[a-z0-9_]*" ) )
				return false;
		return true;
	}

	public boolean containsRegex()
	{
		for ( String s : nodes )
			if ( s.contains( "*" ) || s.matches( ".*[0-9]+-[0-9]+.*" ) )
				return true;
		return false;
	}

	public T dropFirst()
	{
		return subNamespace( 1 );
	}

	public T dropLast()
	{
		return subNamespace( 0, getNodeCount() - 1 );
	}

	/**
	 * Filters out invalid characters from namespace.
	 *
	 * @return The fixed PermissionNamespace.
	 */
	public T fixInvalidChars()
	{
		String[] result = new String[nodes.length];
		for ( int i = 0; i < nodes.length; i++ )
			result[i] = nodes[i].replaceAll( "[^a-z0-9_]", "" );
		return creator.apply( result );
	}

	public String getFirst()
	{
		return getNode( 0 );
	}

	public String getLast()
	{
		return getNode( getNodeCount() - 1 );
	}

	public String getLocalName()
	{
		return nodes[nodes.length - 1];
	}

	public String getNode( int inx )
	{
		try
		{
			return nodes[inx];
		}
		catch ( IndexOutOfBoundsException e )
		{
			return null;
		}
	}

	public int getNodeCount()
	{
		return nodes.length;
	}

	public String getNodeWithException( int inx )
	{
		return nodes[inx];
	}

	public String[] getNodes()
	{
		return nodes;
	}

	public String getParent()
	{
		if ( nodes.length <= 1 )
			return "";

		return Strs.join( Arrays.copyOf( nodes, nodes.length - 1 ), glue );
	}

	public T getParentNamespace()
	{
		return getParentNamespace( 1 );
	}

	public T getParentNamespace( int depth )
	{
		return getNodeCount() >= depth ? subNamespace( 0, getNodeCount() - depth ) : creator.apply( new String[0] );
	}

	public String getRootName()
	{
		return nodes[0];
	}

	public String getString()
	{
		return getString( false );
	}

	/**
	 * Converts Namespace to a String
	 *
	 * @param escape Shall we escape separator characters in node names
	 * @return The converted String
	 */
	public String getString( boolean escape )
	{
		if ( escape )
			return Arrays.stream( nodes ).map( n -> n.replace( glue, "\\" + glue ) ).collect( Collectors.joining() );
		return Strs.join( nodes, glue );
	}

	public boolean isEmpty()
	{
		return nodes.length == 0;
	}

	public int matchPercentage( String namespace )
	{
		return matchPercentage( namespace, "." );
	}

	public int matchPercentage( String namespace, String separator )
	{
		Objs.notEmpty( namespace );

		String[] dest = Strs.split( namespace.toLowerCase(), separator ).toArray( String[]::new );

		int total = 0;
		int perNode = 99 / nodes.length;

		for ( int i = 0; i < Math.min( nodes.length, dest.length ); i++ )
			if ( nodes[i].equals( dest[i] ) )
				total += perNode;
			else
				break;

		if ( nodes.length == dest.length )
			total += 1;

		return total;
	}

	public boolean matches( String perm )
	{
		/*
		 * We are not going to try and match a permission if it contains regex.
		 * This means someone must have gotten their strings backward.
		 */
		if ( perm.contains( "*" ) || perm.matches( ".*[0-9]+-[0-9]+.*" ) )
			return false;

		return prepareRegexp().matcher( perm ).matches();
	}

	public T merge( Namespace ns )
	{
		return creator.apply( Stream.of( nodes, ns.nodes ).flatMap( Stream::of ).toArray( String[]::new ) );
	}

	/**
	 * Prepares a namespace for parsing via RegEx
	 *
	 * @return The fully RegEx ready string
	 */
	public Pattern prepareRegexp()
	{
		String regexpOrig = Strs.join( nodes, "\\." );
		String regexp = regexpOrig.replace( "*", "(.*)" );

		try
		{
			Matcher rangeMatcher = RANGE_EXPRESSION.matcher( regexp );
			while ( rangeMatcher.find() )
			{
				StringBuilder range = new StringBuilder();
				int from = Integer.parseInt( rangeMatcher.group( 1 ) );
				int to = Integer.parseInt( rangeMatcher.group( 2 ) );

				range.append( "(" );

				for ( int i = Math.min( from, to ); i <= Math.max( from, to ); i++ )
				{
					range.append( i );
					if ( i < Math.max( from, to ) )
						range.append( "|" );
				}

				range.append( ")" );

				regexp = regexp.replace( rangeMatcher.group( 0 ), range.toString() );
			}
		}
		catch ( Throwable e )
		{
			// Ignore
		}

		try
		{
			return Pattern.compile( regexp, Pattern.CASE_INSENSITIVE );
		}
		catch ( PatternSyntaxException e )
		{
			return Pattern.compile( Pattern.quote( regexpOrig.replace( "*", "(.*)" ) ), Pattern.CASE_INSENSITIVE );
		}
	}

	@SuppressWarnings( "unchecked" )
	public T prepend( String... nodes )
	{
		if ( nodes.length == 0 )
			throw new IllegalArgumentException( "Nodes are empty" );
		if ( nodes.length == 1 )
			nodes = splitString( nodes[0] );
		this.nodes = Arrs.merge( nodes, this.nodes );
		return ( T ) this;
	}

	public T prependNew( String... nodes )
	{
		if ( nodes.length == 0 )
			throw new IllegalArgumentException( "Nodes are empty" );
		if ( nodes.length == 1 )
			nodes = splitString( nodes[0] );
		return creator.apply( Arrs.merge( nodes, this.nodes ) );
	}

	@SuppressWarnings( "unchecked" )
	public T replace( String literal, String replacement )
	{
		nodes = Arrays.stream( nodes ).map( s -> s.replace( literal, replacement ) ).collect( Collectors.toList() ).toArray( new String[0] );
		return ( T ) this;
	}

	public T replaceNew( String literal, String replacement )
	{
		return creator.apply( Arrays.stream( nodes ).map( s -> s.replace( literal, replacement ) ).collect( Collectors.toList() ).toArray( new String[0] ) );
	}

	public T reverseOrder()
	{
		List<String> tmpNodes = Arrays.asList( nodes );
		Collections.reverse( tmpNodes );
		nodes = tmpNodes.toArray( new String[0] );
		return ( T ) this;
	}

	public T reverseOrderNew()
	{
		List<String> tmpNodes = Arrays.asList( nodes );
		Collections.reverse( tmpNodes );
		return creator.apply( tmpNodes.toArray( new String[0] ) );
	}

	public T setGlue( String glue )
	{
		this.glue = glue;
		return ( T ) this;
	}

	private String[] splitString( String str )
	{
		return splitString( str, null );
	}

	private String[] splitString( String str, String glue )
	{
		glue = Objs.notEmptyOrDef( glue, "." );
		return Strs.split( str, glue ).filter( v -> !Objs.isEmpty( v ) ).toArray( String[]::new );
	}

	public T subNamespace( int start )
	{
		return subNamespace( start, getNodeCount() );
	}

	public T subNamespace( int start, int end )
	{
		return creator.apply( subNodes( start, end ) );
	}

	public String[] subNodes( int start )
	{
		return subNodes( start, getNodeCount() );
	}

	public String[] subNodes( int start, int end )
	{
		if ( start < 0 )
			throw new IllegalArgumentException( "Start can't be less than 0" );
		if ( start > nodes.length )
			throw new IllegalArgumentException( "Start can't be more than length " + nodes.length );
		if ( end > nodes.length )
			throw new IllegalArgumentException( "End can't be more than node count" );

		return Arrays.copyOfRange( nodes, start, end );
	}

	public String subString( int start )
	{
		return subString( start, getNodeCount() );
	}

	public String subString( int start, int end )
	{
		return Strs.join( subNodes( start, end ), glue );
	}

	@Override
	public String toString()
	{
		return getString();
	}
}
