/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.mappings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.amelia.http.webroot.Webroot;
import io.amelia.support.Lists;
import io.amelia.support.NIO;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;

public class DomainNode
{
	protected final List<DomainNode> children = new ArrayList<>();
	protected final String nodeName;
	protected final DomainNode parent;
	protected Webroot webroot;

	protected DomainNode( DomainNode parent, String nodeName )
	{
		this.parent = parent;
		this.nodeName = nodeName;
	}

	protected DomainNode( String nodeName )
	{
		this.parent = null;
		this.nodeName = nodeName;
	}

	public DomainNode getChild( String domain )
	{
		return getChild( domain, false );
	}

	/**
	 * Supports RegEx for each domain node
	 *
	 * @param domain The path to the child requested
	 * @param create Shall we create the child if it's missing or fail gracefully
	 *
	 * @return
	 */
	public DomainNode getChild( String domain, boolean create )
	{
		Objs.notEmpty( domain );

		if ( NIO.isValidIPv4( domain ) )
			throw new IllegalArgumentException( "Can't match child by IPv4 address" );
		if ( NIO.isValidIPv6( domain ) )
			throw new IllegalArgumentException( "Can't match child by IPv6 address" );

		// XXX Can this inner method be replaced by the Java 8 Stream feature?

		Namespace ns = Namespace.of( domain ).reverseOrder();
		DomainNode domainNode = this;

		for ( final String node : ns.getNames() )
		{
			Optional<DomainNode> results = domainNode.children.stream().filter( c -> node.matches( c.getNodeName() ) ).findFirst();
			if ( results.isPresent() )
				domainNode = results.get();
			else
			{
				if ( create )
					domainNode = Lists.add( domainNode.children, new DomainNode( this, node ) );
				else
					return null;
			}
		}

		return domainNode;
	}

	public String getChildDomain()
	{
		return new DomainParser( getFullDomain() ).getSub().getString();
	}

	public Stream<DomainNode> getChildren()
	{
		return children.stream();
	}

	public Stream<DomainNode> getChildrenRecursive()
	{
		return children.stream().flatMap( DomainNode::getChildrenRecursive0 );
	}

	protected Stream<DomainNode> getChildrenRecursive0()
	{
		return Stream.concat( Stream.of( this ), children.stream().flatMap( DomainNode::getChildrenRecursive0 ) );
	}

	public DomainMapping getDomainMapping()
	{
		return getWebroot() == null ? null : getWebroot().getMappings( getFullDomain() ).findFirst().orElse( null );
	}

	public String getFullDomain()
	{
		return getNamespace().reverseOrder().getString();
	}

	public Namespace getNamespace()
	{
		return hasParent() ? getParent().getNamespace().append( getNodeName() ) : Namespace.of( getNodeName() );
	}

	public String getNodeName()
	{
		return nodeName;
	}

	public DomainNode getParent()
	{
		return parent;
	}

	public Stream<DomainNode> getParents()
	{
		return Stream.of( parent ).flatMap( DomainNode::getParents0 );
	}

	protected Stream<DomainNode> getParents0()
	{
		return Stream.concat( Stream.of( this ), Stream.of( parent ).flatMap( DomainNode::getParents0 ) );
	}

	public String getRootDomain()
	{
		return new DomainParser( getFullDomain() ).getTld().getString();
	}

	public Webroot getWebroot()
	{
		return webroot;
	}

	protected DomainNode setWebroot( Webroot webroot )
	{
		return setWebroot( webroot, false );
	}

	public boolean hasChildren()
	{
		return children.size() > 0;
	}

	public boolean hasParent()
	{
		return parent != null;
	}

	protected DomainNode setWebroot( Webroot webroot, boolean override )
	{
		Objs.notNull( webroot );

		if ( this.webroot != null && this.webroot != webroot && !override )
			throw new IllegalStateException( String.format( "You can not override the webroot set on domain node [%s], it was already assigned to webroot [%s]", webroot.getWebrootId(), this.webroot.getWebrootId() ) );
		this.webroot = webroot;
		return this;
	}
}
