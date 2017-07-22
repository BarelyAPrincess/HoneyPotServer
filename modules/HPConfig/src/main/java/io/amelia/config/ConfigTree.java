/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.config;

import io.amelia.support.Lists;
import io.amelia.support.Namespace;
import io.amelia.support.NamespaceParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class ConfigTree
{
	private static ConfigNode defaultConfigNode = new DefaultConfigNode();
	private static Map<String, List<ConfigRoot>> domains = new ConcurrentHashMap<>();

	public static Stream<ConfigNode> getChildren()
	{
		return domains.values().stream().flatMap( l -> l.stream() ).flatMap( ConfigNode::getChildrenRecursive0 );
	}

	public static Stream<ConfigRoot> getDomains( String tld )
	{
		return domains.values().stream().flatMap( l -> l.stream() ).filter( n -> tld.matches( n.getTld() ) );
	}

	public static Stream<String> getTLDsInuse()
	{
		return domains.keySet().stream();
	}

	public static ConfigNode parseDomain( NamespaceParser fullDomain )
	{
		Namespace root = fullDomain.getTld();
		Namespace child = fullDomain.getSub().reverseOrder();

		if ( root.isEmpty() && child.isEmpty() )
			return defaultConfigNode;

		if ( child.isEmpty() )
			throw new IllegalArgumentException( String.format( "Something went wrong, the tld \"%s\" has no children.", root ) );

		List<ConfigRoot> list = domains.compute( root.getString( "_", true ), ( k, v ) -> v == null ? new ArrayList<>() : v );

		String first = child.getFirst();
		ConfigNode node = Lists.findOrNew( list, v -> first.equals( v.getNodeName() ), new ConfigRoot( root.getString(), first ) );

		if ( child.getNodeCount() > 1 )
			for ( String s : child.subNodes( 1 ) )
				node = node.getConfigNode( s, true );

		return node;
	}

	public static ConfigNode parseDomain( String fullDomain )
	{
		return parseDomain( new NamespaceParser( fullDomain ) );
	}

	private ConfigTree()
	{

	}
}
