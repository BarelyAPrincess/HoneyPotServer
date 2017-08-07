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

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Namespace extends NamespaceBase<Namespace>
{
	public static Namespace parseString( String namespace, String separator )
	{
		if ( namespace == null )
			namespace = "";
		if ( separator == null || separator.length() == 0 )
			separator = ".";
		return new Namespace( Strs.split( namespace, Pattern.compile( separator, Pattern.LITERAL ) ).collect( Collectors.toList() ) );
	}

	public static Namespace parseString( String namespace )
	{
		return parseString( namespace, null );
	}

	public static Namespace parseStringRegex( String namespace, String regex )
	{
		if ( namespace == null )
			namespace = "";
		if ( regex == null || regex.length() == 0 )
			regex = "\\.";
		return new Namespace( Strs.split( namespace, Pattern.compile( regex ) ).collect( Collectors.toList() ) );
	}

	public Namespace( String[] nodes )
	{
		super( Namespace::new, nodes );
	}

	public Namespace( List<String> nodes )
	{
		super( Namespace::new, nodes );
	}

	public Namespace()
	{
		super( Namespace::new );
	}
}
