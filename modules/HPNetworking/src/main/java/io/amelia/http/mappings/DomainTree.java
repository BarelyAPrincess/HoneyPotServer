/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.mappings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import io.amelia.support.Lists;
import io.amelia.support.Namespace;

public final class DomainTree
{
	private static DomainNode defaultDomainNode = new DefaultDomainNode();
	private static Map<String, List<DomainRoot>> domains = new ConcurrentHashMap<>();

	public static Stream<DomainNode> getChildren()
	{
		return domains.values().stream().flatMap( l -> l.stream() ).flatMap( DomainNode::getChildrenRecursive0 );
	}

	public static Stream<DomainRoot> getDomains( String tld )
	{
		return domains.values().stream().flatMap( l -> l.stream() ).filter( n -> tld.matches( n.getTld() ) );
	}

	public static Stream<String> getTLDsInuse()
	{
		return domains.keySet().stream();
	}

	public static DomainNode parseDomain( DomainParser fullDomain )
	{
		Namespace root = fullDomain.getTld();
		Namespace child = fullDomain.getSub().reverseOrder();

		if ( root.isEmpty() && child.isEmpty() )
			return defaultDomainNode;

		if ( child.isEmpty() )
			throw new IllegalArgumentException( String.format( "Something went wrong, the tld \"%s\" has no child domains.", root ) );

		List<DomainRoot> list = domains.compute( root.setGlue( "_" ).getString( true ), ( k, v ) -> v == null ? new ArrayList<>() : v );

		String first = child.getStringFirst();
		DomainNode node = Lists.findOrNew( list, v -> v.getNodeName().equals( first ), new DomainRoot( root.getString(), first ) );

		if ( child.getNodeCount() > 1 )
			for ( String s : child.subArray( 1 ) )
				node = node.getChild( s, true );

		return node;
	}

	public static DomainNode parseDomain( String fullDomain )
	{
		return parseDomain( new DomainParser( fullDomain ) );
	}

	private DomainTree()
	{

	}
}
