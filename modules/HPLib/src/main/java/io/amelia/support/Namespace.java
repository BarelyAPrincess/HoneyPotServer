/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;

public class Namespace extends NamespaceBase<Namespace>
{
	private static final List<String> tldMaps = new ArrayList<>();

	static
	{
		try
		{
			tldMaps.addAll( ConfigRegistry.getChild( "conf.tlds" ).getList( String.class ).orElse( new ArrayList<>() ) );
		}
		catch ( Exception e )
		{
			Kernel.L.severe( "Could not read TLD configuration. Check if config list `conf.tlds` exists.", e );
		}
	}

	public static boolean isTld( String domain )
	{
		domain = Web.hostnameNormalize( domain );
		for ( String tld : tldMaps )
			if ( domain.matches( tld ) )
				return true;
		return false;
	}

	public static Domain parseDomain( String namespace )
	{
		namespace = Web.hostnameNormalize( namespace );

		if ( Objs.isEmpty( namespace ) )
			return new Domain( new Namespace(), new Namespace() );

		Namespace ns = Namespace.parseString( namespace );
		int parentNodePos = -1;

		for ( int n = 0; n < ns.getNodeCount(); n++ )
		{
			String sns = ns.subNamespace( n ).getString();
			if ( isTld( sns ) )
			{
				parentNodePos = n;
				break;
			}
		}

		return parentNodePos > 0 ? new Domain( ns.subNamespace( parentNodePos ), ns.subNamespace( 0, parentNodePos ) ) : new Domain( new Namespace(), ns );
	}

	public static Namespace parseString( String namespace )
	{
		return parseString( namespace, null );
	}

	public static Namespace parseString( String namespace, String glue )
	{
		namespace = Objs.notNullOrDef( namespace, "" );
		glue = Objs.notEmptyOrDef( glue, "." );
		return new Namespace( Strs.split( namespace, Pattern.compile( glue, Pattern.LITERAL ) ).collect( Collectors.toList() ), glue );
	}

	public static Namespace parseStringRegex( String namespace, String regex )
	{
		namespace = Objs.notNullOrDef( namespace, "" );
		regex = Objs.notEmptyOrDef( regex, "\\." );
		return new Namespace( Strs.split( namespace, Pattern.compile( regex ) ).collect( Collectors.toList() ) );
	}

	public Namespace( String[] nodes, String glue )
	{
		super( Namespace::new, glue, nodes );
	}

	public Namespace( List<String> nodes, String glue )
	{
		super( Namespace::new, glue, nodes );
	}

	public Namespace( String glue )
	{
		super( Namespace::new, glue );
	}

	public Namespace( String[] nodes )
	{
		super( Namespace::new, ".", nodes );
	}

	public Namespace( List<String> nodes )
	{
		super( Namespace::new, ".", nodes );
	}

	public Namespace()
	{
		super( Namespace::new, "." );
	}

	public static class Domain
	{
		private final Namespace child;
		private final Namespace tld;

		private Domain( Namespace tld, Namespace child )
		{
			this.tld = tld;
			this.child = child;
		}

		public Namespace getChild()
		{
			return child;
		}

		public Namespace getChildDomain()
		{
			return child.getNodeCount() <= 1 ? new Namespace() : child.subNamespace( 1 );
		}

		public Namespace getFullDomain()
		{
			return child.merge( tld );
		}

		public Namespace getRootDomain()
		{
			return Namespace.parseString( child.getLast() + "." + tld.getString() );
		}

		public Namespace getTld()
		{
			return tld;
		}
	}
}
