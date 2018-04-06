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

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.foundation.ConfigMap;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.support.data.StackerWithValue;

/**
 * Provides an easy translator for content-types specified from configuration.
 */
public class ContentTypes
{
	public static final String CONFIG_KEY_CONTENT_TYPE = ConfigRegistry.ConfigKeys.CONFIGURATION_BASE + ".contentTypes";

	public static void clearType( String ext )
	{
		getConfigMap().destroyChild( ext );
	}

	public static Stream<String> getAllTypes()
	{
		return getConfigMap().getChildren().map( StackerWithValue::getValue ).filter( Optional::isPresent ).map( Optional::get ).map( Objs::castToString );
	}

	private static ConfigMap getConfigMap()
	{
		return ConfigRegistry.config.getChildOrCreate( CONFIG_KEY_CONTENT_TYPE );
	}

	@Nonnull
	public static Stream<String> getContentTypes( @Nonnull String filename )
	{
		Objs.notNull( filename );

		String ext = Strs.regexCapture( filename, "\\.(\\w+)$" );

		return Stream.concat( getConfigMap().getChildren().filter( child -> child.getName().equalsIgnoreCase( ext ) && child.hasValue() ).flatMap( child -> Strs.split( child.getString().get(), "," ) ), Stream.of( "application/octet-stream" ) );
	}

	@Nonnull
	public static Stream<String> getContentTypes( @Nonnull File file )
	{
		Objs.notNull( file );

		if ( file.isDirectory() )
			return Stream.of( "directory" );

		return getContentTypes( file.getName() );
	}

	public static void setType( String ext, String type )
	{
		ConfigMap map = getConfigMap().getChildOrCreate( ext );
		if ( map.hasValue() )
			map.setValue( map.getString().get() + "," + type );
		else
			map.setValue( type );
	}

	private ContentTypes()
	{
		// Static Access
	}
}
