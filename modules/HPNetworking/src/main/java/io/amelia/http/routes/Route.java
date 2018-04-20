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

import io.amelia.logging.LogBuilder;
import com.chiorichan.site.Site;
import com.chiorichan.utils.UtilObjects;
import com.chiorichan.utils.UtilStrings;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Route
{
	private final String id;
	private final Map<String, String> params = new HashMap<>();
	private final Map<String, String> rewrites = new HashMap<>();
	private final Site site;

	protected Route( String id, Site site, Map<String, String> params, Map<String, String> rewrites )
	{
		this.id = id;
		this.site = site;
		this.params.putAll( params );
		this.rewrites.putAll( rewrites );
	}

	public String getId()
	{
		return id;
	}

	public String getParam( String id )
	{
		return params.get( id );
	}

	public void putParam( String id, String value )
	{
		params.put( id, value );
	}

	public boolean hasParam( String id )
	{
		return params.containsKey( id );
	}

	public Map<String, String> getParams()
	{
		return Collections.unmodifiableMap( params );
	}

	public Map<String, String> getRewrites()
	{
		return Collections.unmodifiableMap( rewrites );
	}

	public int httpCode()
	{
		return UtilObjects.isEmpty( params.get( "status" ) ) ? 301 : Integer.parseInt( params.get( "status" ) );
	}

	public boolean isRedirect()
	{
		return params.get( "redirect" ) != null || params.get( "url" ) != null;
	}

	public RouteResult match( String uri, String host )
	{
		Map<String, String> localRewrites = new HashMap<>( rewrites );
		String prop = params.get( "pattern" );

		if ( prop == null )
			return null; // Ignore, is likely a route url entry

		prop = StringUtils.trimToEmpty( prop );
		uri = StringUtils.trimToEmpty( uri );

		if ( prop.startsWith( "/" ) )
		{
			prop = prop.substring( 1 );
			params.put( "pattern", prop );
		}

		if ( !UtilObjects.isEmpty( params.get( "host" ) ) && !host.matches( params.get( "host" ) ) )
		{
			LogBuilder.get().finer( "The host failed validation for route " + this );
			return null;
		}

		if ( UtilObjects.isEmpty( params.get( "host" ) ) )
			LogBuilder.get().warning( "The Route [" + params.entrySet().stream().map( e -> e.getKey() + "=\"" + e.getValue() + "\"" ).collect( Collectors.joining( "," ) ) + "] has no host (Uses RegEx, e.g., ^example.com$) defined, it's recommended that one is set so that the rule is not used unintentionally." );

		String[] propsRaw = prop.split( "[.//]" );
		String[] urisRaw = uri.split( "[.//]" );

		ArrayList<String> props = Lists.newArrayList();
		ArrayList<String> uris = Lists.newArrayList();

		for ( String s : propsRaw )
			if ( s != null && !s.isEmpty() )
				props.add( s );

		for ( String s : urisRaw )
			if ( s != null && !s.isEmpty() )
				uris.add( s );

		if ( uris.isEmpty() )
			uris.add( "" );

		if ( props.isEmpty() )
			props.add( "" );

		if ( props.size() > uris.size() )
		{
			LogBuilder.get().finer( "The length of elements in route " + this + " is LONGER then the length of elements on the uri; " + uris );
			return null;
		}

		if ( props.size() < uris.size() )
		{
			LogBuilder.get().finer( "The length of elements in route " + this + " is SHORTER then the length of elements on the uri; " + uris );
			return null;
		}

		String weight = StringUtils.repeat( "?", Math.max( props.size(), uris.size() ) );

		boolean match = true;
		for ( int i = 0; i < Math.max( props.size(), uris.size() ); i++ )
			try
			{
				LogBuilder.get().finest( prop + " --> " + props.get( i ) + " == " + uris.get( i ) );

				if ( props.get( i ).matches( "\\[([a-zA-Z0-9]+)=\\]" ) )
				{
					weight = UtilStrings.replaceAt( weight, i, "Z" );

					String key = props.get( i ).replaceAll( "[\\[\\]=]", "" );
					String value = uris.get( i );

					localRewrites.put( key, value );

					// PREG MATCH
					LogBuilder.get().finer( "Found a PREG match for " + prop + " on route " + this );
				}
				else if ( props.get( i ).equals( uris.get( i ) ) )
				{
					weight = UtilStrings.replaceAt( weight, i, "A" );

					LogBuilder.get().finer( "Found a match for " + prop + " on route " + this );
					// MATCH
				}
				else
				{
					match = false;
					LogBuilder.get().finer( "Found no match for " + prop + " on route " + this );
					break;
					// NO MATCH
				}
			}
			catch ( ArrayIndexOutOfBoundsException e )
			{
				match = false;
				break;
			}

		return match ? new RouteResult( this, weight, localRewrites ) : null;
	}

	@Override
	public String toString()
	{
		return "Route {params=[" + params.entrySet().stream().map( e -> e.getKey() + "=\"" + e.getValue() + "\"" ).collect( Collectors.joining( "," ) ) + "]}";
	}
}
