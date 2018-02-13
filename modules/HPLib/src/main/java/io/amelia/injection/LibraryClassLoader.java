/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.injection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * Acts as the classloader for downloaded Maven Libraries
 */
@SuppressWarnings( {"unchecked", "rawtypes"} )
public class LibraryClassLoader
{
	private static final Class[] parameters = new Class[] {URL.class};

	public static void addPath( File f ) throws IOException
	{
		addPath( f.toURI().toURL() );
	}

	public static void addPath( String s ) throws IOException
	{
		addPath( new File( s ) );
	}

	public static void addPath( URL u ) throws IOException
	{
		URLClassLoader sysloader = ( URLClassLoader ) ClassLoader.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;

		try
		{
			Method method = sysclass.getDeclaredMethod( "addURL", parameters );
			method.setAccessible( true );
			method.invoke( sysloader, u );
		}
		catch ( Throwable t )
		{
			throw new IOException( String.format( "Error, could not add path '%s' to system classloader", u.toString() ), t );
		}

	}

	public static boolean pathLoaded( File f ) throws MalformedURLException
	{
		return pathLoaded( f.toURI().toURL() );
	}

	public static boolean pathLoaded( String s ) throws MalformedURLException
	{
		return pathLoaded( new File( s ) );
	}

	public static boolean pathLoaded( URL u )
	{
		URLClassLoader sysloader = ( URLClassLoader ) ClassLoader.getSystemClassLoader();
		return Arrays.asList( sysloader.getURLs() ).contains( u );
	}
}
