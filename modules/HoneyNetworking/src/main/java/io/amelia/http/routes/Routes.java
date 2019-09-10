/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.routes;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.amelia.http.webroot.Webroot;
import io.amelia.foundation.Kernel;
import io.amelia.support.Objs;
import io.netty.util.internal.ConcurrentSet;

/**
 * Keeps track of routes
 */
public class Routes
{
	public static final Kernel.Logger L = Kernel.getLogger( Routes.class );

	protected final Set<Route> routes = new ConcurrentSet<>();
	protected final Webroot webroot;
	private RouteWatcher jsonWatcher;
	private RouteWatcher yamlWatcher;

	public Routes( Webroot webroot )
	{
		this.webroot = webroot;

		Path routesJson = webroot.getDirectory().resolve( "routes.json" );
		Path routesYaml = webroot.getDirectory().resolve( "routes.yaml" );

		jsonWatcher = new RouteWatcher( this, routesJson );
		yamlWatcher = new RouteWatcher( this, routesYaml );
	}

	public boolean hasRoute( String id )
	{
		return routes.stream().anyMatch( r -> id.equalsIgnoreCase( r.getId() ) || id.matches( r.getId() ) );
	}

	public Route routeUrl( @Nonnull String id )
	{
		Objs.notEmpty( id );

		jsonWatcher.reviveTask();
		yamlWatcher.reviveTask();

		return routes.stream().filter( r -> id.equalsIgnoreCase( r.getId() ) || id.matches( r.getId() ) ).findFirst().orElse( null );
	}

	public RouteResult searchRoutes( String uri, String host )
	{
		jsonWatcher.reviveTask();
		yamlWatcher.reviveTask();

		AtomicInteger keyInteger = new AtomicInteger();

		Map<String, RouteResult> matches = routes.stream().map( route -> route.match( uri, host ) ).filter( Objs::isNotNull ).collect( Collectors.toMap( result -> result.getWeight() + keyInteger.getAndIncrement(), result -> result ) );

		if ( matches.size() > 0 )
			return ( RouteResult ) matches.values().toArray()[0];
		else
			L.fine( String.format( "Failed to find route for... {host=%s,uri=%s}", host, uri ) );

		return null;
	}
}
