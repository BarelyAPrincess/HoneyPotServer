/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import io.amelia.config.ConfigRegistry;
import io.amelia.foundation.Kernel;

public class Info
{
	private static Properties metadata;

	static
	{
		loadMetaData( false );
	}

	/**
	 * Get the server build number
	 * The build number is only set when the server is built on our Jenkins Build Server or by Travis,
	 * meaning this will be 0 for all development builds
	 *
	 * @return The server build number
	 */
	public static String getBuildNumber()
	{
		return metadata.getProperty( "project.build", "0" );
	}

	/**
	 * Get the server copyright, e.g., Copyright (c) 2015 Chiori-chan
	 *
	 * @return The server copyright
	 */
	public static String getCopyright()
	{
		return metadata.getProperty( "project.copyright", "Copyright &copy; 2017 Amelia DeWitt" );
	}

	/**
	 * Get the developer e-mail address
	 * Suggested use is to report problems
	 *
	 * @return The developer e-mail address
	 */
	public static String getDeveloperContact()
	{
		return metadata.getProperty( "project.email", "me@ameliadewitt.com" );
	}

	/**
	 * Get the GitHub Branch this was built from, e.g., master
	 * Set by the Gradle build script
	 *
	 * @return The GitHub branch
	 */
	public static String getGitHubBranch()
	{
		return metadata.getProperty( "project.branch", "master" );
	}

	/**
	 * Generates a HTML suitable footer for general server info and exception pages
	 *
	 * @return HTML footer string
	 */
	public static String getHTMLFooter()
	{
		return "<small>Running <a href=\"https://github.com/ChioriGreene/ChioriWebServer\">" + getProduct() + "</a> Version " + getVersion() + " (Build #" + getBuildNumber() + ")<br />" + getCopyright() + "</small>";
	}

	/**
	 * Get the server product name, e.g., Honey Pot Server
	 *
	 * @return The Product Name
	 */
	public static String getProduct()
	{
		return metadata.getProperty( "project.name", "Honey Pot Server" );
	}

	/**
	 * Describe this product
	 *
	 * @return
	 */
	public static String getProductDescribe()
	{
		return "Running " + Info.getProduct() + " version " + Info.getVersion() + " (Build #" + getBuildNumber() + ")";
	}

	/**
	 * Get the server product name without spaces or special characters, e.g., HoneyPot
	 *
	 * @return The Product Name Simple
	 */
	public static String getProductSimple()
	{
		return metadata.getProperty( "project.name", "HoneyPot" ).replaceAll( " ", "" );
	}

	/**
	 * Get the server version, e.g., 9.2.1 (Milky Berry)
	 *
	 * @return The server version with code name
	 */
	public static String getVersion()
	{
		return metadata.getProperty( "project.version", "Unknown-Version" ) + " (" + metadata.getProperty( "project.codename" ) + ")";
	}

	/**
	 * Get the server version number, e.g., 9.2.1
	 *
	 * @return The server version number
	 */
	public static String getVersionNumber()
	{
		return metadata.getProperty( "project.version", "Unknown-Version" );
	}

	/**
	 * Indicates if we are running a development build of the server
	 *
	 * @return True is we are running in development mode
	 */
	public static boolean isDevelopment()
	{
		return "0".equals( getBuildNumber() ) || ConfigRegistry.getBoolean( "server.developmentMode" ).orElse( false );
	}

	/**
	 * Loads the server metadata from the file {@value "build.properties"},
	 * which is usually updated by our Gradle build script
	 *
	 * @param force Force a metadata reload
	 */
	private static void loadMetaData( boolean force )
	{
		if ( metadata != null && !metadata.isEmpty() && !force )
			return;

		metadata = new Properties();

		InputStream is = null;
		try
		{
			is = Kernel.class.getClassLoader().getResourceAsStream( "build.properties" );
			if ( is == null )
			{
				Kernel.L.severe( "Info", "This application is missing the `build.properties` file, we will now default to the API build properties file." );
				is = Kernel.class.getClassLoader().getResourceAsStream( "api.properties" );
			}

			metadata.load( is );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
		finally
		{
			IO.closeQuietly( is );
		}
	}
}
