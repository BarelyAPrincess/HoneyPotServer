/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.routes;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.amelia.foundation.Kernel;
import io.amelia.http.webroot.BaseWebroot;
import io.amelia.support.Objs;
import io.netty.util.internal.ConcurrentSet;

/**
 * Keeps track of routes
 */
public class Routes
{
	public static final Kernel.Logger L = Kernel.getLogger( Routes.class );

	protected final Set<Route> routes = new ConcurrentSet<>();
	protected final BaseWebroot webroot;
	private RouteWatcher jsonWatcher;
	private RouteWatcher yamlWatcher;

	public Routes( BaseWebroot webroot )
	{
		this.webroot = webroot;

		File routesJson = new File( webroot.getDirectory(), "routes.json" );
		File routesYaml = new File( webroot.getDirectory(), "routes.yaml" );

		jsonWatcher = new RouteWatcher( this, routesJson );
		yamlWatcher = new RouteWatcher( this, routesYaml );
	}

	public boolean hasRoute( String id )
	{
		return routes.stream().filter( r -> id.equalsIgnoreCase( r.getId() ) || id.matches( r.getId() ) ).findAny().isPresent();
	}

	public Route routeUrl( @Nonnull String id )
	{
		Objs.notEmpty( id );

		jsonWatcher.reviveTask();
		yamlWatcher.reviveTask();

		return routes.stream().filter( r -> id.equalsIgnoreCase( r.getId() ) || id.matches( r.getId() ) ).findFirst().orElse( null );
	}

	public RouteResult searchRoutes( String uri, String host ) throws IOException
	{
		jsonWatcher.reviveTask();
		yamlWatcher.reviveTask();

		AtomicInteger keyInteger = new AtomicInteger();

		Map<String, RouteResult> matches = routes.stream().map( route -> route.match( uri, host ) ).filter( result -> result != null ).collect( Collectors.toMap( result -> result.getWeight() + keyInteger.getAndIncrement(), result -> result ) );

		if ( matches.size() > 0 )
			return ( RouteResult ) matches.values().toArray()[0];
		else
			L.fine( String.format( "Failed to find route for... {host=%s,uri=%s}", host, uri ) );

		return null;
	}
}
