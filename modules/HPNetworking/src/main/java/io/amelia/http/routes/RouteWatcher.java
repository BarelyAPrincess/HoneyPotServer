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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.amelia.support.FileWatcher;

public class RouteWatcher extends FileWatcher
{
	private Routes parent;

	public RouteWatcher( Routes parent, File fileToWatch )
	{
		super( fileToWatch );

		this.parent = parent;
	}

	@Override
	public void readChanges()
	{
		if ( !fileToWatch.exists() )
			return;

		Set<Route> routes = parent.routes;

		if ( fileToWatch.getName().endsWith( ".json" ) )
		{
			routes.clear();

			int line = 0;
			AtomicInteger inx = new AtomicInteger();

			LogBuilder.get().fine( "Loading Routes from JSON file '" + UtilIO.relPath( fileToWatch ) + "'" );

			try
			{
				for ( String l : UtilIO.readFileToLines( fileToWatch ) )
				{
					try
					{
						line++;
						if ( !l.startsWith( "#" ) && !UtilObjects.isEmpty( l.trim() ) )
						{
							Map<String, String> values = new HashMap<>();
							Map<String, String> rewrites = new HashMap<>();

							JSONObject obj = new JSONObject( l );

							String id = obj.optString( "id" );
							if ( UtilObjects.isEmpty( id ) )
							{
								do
								{
									id = "route_rule_" + String.format( "%04d", inx.getAndIncrement() );
								}
								while ( parent.hasRoute( id ) );
							}
							else if ( parent.hasRoute( id ) )
							{
								NetworkManager.getLogger().severe( String.format( "Found duplicate route id '%s' in route file '%s', route will be ignored.", id, UtilIO.relPath( fileToWatch ) ) );
								continue;
							}

							for ( String sectionKey : obj.keySet() )
							{
								Object sectionObject = obj.get( sectionKey );

								if ( sectionObject instanceof JSONObject && "vargs".equals( sectionKey ) )
								{
									for ( String argsKey : ( ( JSONObject ) sectionObject ).keySet() )
									{
										Object argsObject = ( ( JSONObject ) sectionObject ).get( argsKey );
										if ( !( argsObject instanceof JSONObject ) && !( argsObject instanceof JSONArray ) )
											try
											{
												rewrites.put( argsKey, UtilObjects.castToStringWithException( argsObject ) );
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
										values.put( sectionKey, UtilObjects.castToStringWithException( sectionObject ) );
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
						LogBuilder.get().severe( "Failed to parse '" + UtilIO.relPath( fileToWatch ) + "' file, line " + line + ".", e );
					}
				}
			}
			catch ( IOException e )
			{
				LogBuilder.get().severe( "Failed to load '" + UtilIO.relPath( fileToWatch ) + "' file.", e );
			}

			LogBuilder.get().fine( "Finished Loading Routes from JSON file '" + UtilIO.relPath( fileToWatch ) + "'" );
		}
		else if ( fileToWatch.getName().endsWith( ".yaml" ) )
		{
			LogBuilder.get().fine( "Loading Routes from YAML file '" + UtilIO.relPath( fileToWatch ) + "'" );

			YamlConfiguration yaml = YamlConfiguration.loadConfiguration( fileToWatch );

			for ( String key : yaml.getKeys() )
				if ( yaml.isConfigurationSection( key ) )
				{
					String id = key;
					ConfigurationSection section = yaml.getConfigurationSection( key );
					if ( section.contains( "id" ) )
					{
						id = section.getString( "id" );
						section.set( "id", null );
					}

					if ( parent.hasRoute( id ) )
					{
						NetworkManager.getLogger().severe( String.format( "Found duplicate route id '%s' in route file '%s', route will be ignored.", id, UtilIO.relPath( fileToWatch ) ) );
						continue;
					}

					Map<String, String> values = new HashMap<>();
					Map<String, String> rewrites = new HashMap<>();

					for ( String sectionKey : section.getKeys() )
					{
						if ( section.isConfigurationSection( sectionKey ) && "vargs".equals( sectionKey ) )
						{
							ConfigurationSection args = section.getConfigurationSection( sectionKey );
							for ( String argsKey : args.getKeys() )
								if ( !args.isConfigurationSection( argsKey ) )
									try
									{
										rewrites.put( argsKey, UtilObjects.castToStringWithException( args.get( argsKey ) ) );
									}
									catch ( Exception e )
									{
										// Ignore
									}
						}
						else if ( !section.isConfigurationSection( sectionKey ) )
						{
							try
							{
								values.put( sectionKey, UtilObjects.castToStringWithException( section.get( sectionKey ) ) );
							}
							catch ( Exception e )
							{
								// Ignore
							}
						}
					}

					routes.add( new Route( id, parent.webroot, values, rewrites ) );
				}

			LogBuilder.get().fine( "Finished Loading Routes from YAML file '" + UtilIO.relPath( fileToWatch ) + "'" );
		}
	}
}
