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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.amelia.data.parcel.Parcel;
import io.amelia.data.parcel.ParcelLoader;
import io.amelia.lang.ParcelableException;
import io.amelia.net.Networking;
import io.amelia.support.FileWatcher;
import io.amelia.support.IO;
import io.amelia.support.Objs;

public class RouteWatcher extends FileWatcher
{
	private Routes parent;

	public RouteWatcher( Routes parent, Path path )
	{
		super( path );

		this.parent = parent;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void readChanges()
	{
		if ( !Files.isRegularFile( path ) )
			return;

		Set<Route> routes = parent.routes;

		if ( path.endsWith( ".json" ) )
		{
			routes.clear();

			AtomicInteger line = new AtomicInteger();
			AtomicInteger inx = new AtomicInteger();

			Networking.L.fine( "Loading Routes from JSON file '" + IO.relPath( path ) + "'" );

			try
			{
				IO.readFileToStream( path ).filter( l -> !l.startsWith( "#" ) && !Objs.isEmpty( l.trim() ) ).forEach( l -> {
					try
					{
						line.addAndGet( 1 );

						Map<String, String> values = new HashMap<>();
						Map<String, String> rewrites = new HashMap<>();

						JSONObject obj = new JSONObject( l );

						String id = obj.optString( "id" );

						if ( Objs.isEmpty( id ) )
						{
							do
							{
								id = "route_rule_" + String.format( "%04d", inx.getAndIncrement() );
							}
							while ( parent.hasRoute( id ) );
						}

						if ( parent.hasRoute( id ) )
							Networking.L.severe( String.format( "Found duplicate route id '%s' in route file '%s', route will be ignored.", id, IO.relPath( path ) ) );
						else
						{
							Iterator<String> iterator = obj.keys();
							while ( iterator.hasNext() )
							{
								String sectionKey = iterator.next();
								Object sectionObject = obj.get( sectionKey );

								if ( sectionObject instanceof JSONObject && "vargs".equals( sectionKey ) )
								{
									Iterator<String> iteratorArgs = ( ( JSONObject ) sectionObject ).keys();
									while ( iteratorArgs.hasNext() )
									{
										String argsKey = iteratorArgs.next();
										Object argsObject = ( ( JSONObject ) sectionObject ).get( argsKey );
										if ( !( argsObject instanceof JSONObject ) && !( argsObject instanceof JSONArray ) )
											try
											{
												rewrites.put( argsKey, Objs.castToStringWithException( argsObject ) );
											}
											catch ( Exception e )
											{
												// Ignore
											}
									}
								}
								else if ( !( sectionObject instanceof JSONObject ) && !( sectionObject instanceof JSONArray ) )
								{
									try
									{
										values.put( sectionKey, Objs.castToStringWithException( sectionObject ) );
									}
									catch ( Exception e )
									{
										// Ignore
									}
								}
							}

							routes.add( new Route( id, parent.webroot, values, rewrites ) );
						}
					}
					catch ( JSONException e )
					{
						Networking.L.severe( "Failed to parse '" + IO.relPath( path ) + "' file, line " + line + ".", e );
					}
				} );
			}
			catch ( IOException e )
			{
				Networking.L.severe( "Failed to load '" + IO.relPath( path ) + "' file.", e );
			}

			Networking.L.fine( "Finished Loading Routes from JSON file '" + IO.relPath( path ) + "'" );
		}
		else if ( path.endsWith( ".yaml" ) )
		{
			Networking.L.fine( "Loading Routes from YAML file '" + IO.relPath( path ) + "'" );

			Parcel data = null;
			try
			{
				data = ParcelLoader.decodeYaml( path );
			}
			catch ( IOException | ParcelableException.Error e )
			{
				Networking.L.severe( "Failed to load '" + IO.relPath( path ) + "' file.", e );
				return;
			}

			data.getChildren().forEach( child -> {
				String id = child.getString( "id" ).orElse( child.getLocalName() );

				if ( !parent.hasRoute( id ) )
				{
					Map<String, String> values = new HashMap<>();
					Map<String, String> rewrites = new HashMap<>();

					child.getChildren().forEach( section -> {
						if ( "vargs".equals( section.getLocalName() ) )
							section.getChildrenWithValue().forEach( args -> rewrites.put( args.getLocalName(), args.getString().orElse( null ) ) );
						else
							values.put( section.getLocalName(), section.getString().orElse( null ) );
					} );

					routes.add( new Route( id, parent.webroot, values, rewrites ) );
				}
				else
					Networking.L.severe( String.format( "Found duplicate route id '%s' in route file '%s', route will be ignored.", id, IO.relPath( path ) ) );
			} );
		}

		Networking.L.fine( "Finished Loading Routes from YAML file '" + IO.relPath( path ) + "'" );
	}
}
