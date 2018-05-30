/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.webroot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.amelia.http.HttpRequestWrapper;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.ScriptingException;
import io.amelia.scripting.ScriptingContext;
import io.amelia.scripting.ScriptingFactory;
import io.amelia.support.IO;
import io.amelia.support.Objs;

public class WebrootScriptingContext extends ScriptingContext
{
	public static WebrootScriptingContext fromFile( final Site site, final String file )
	{
		// We block absolute file paths for both unix-like and windows
		if ( file.startsWith( File.separator ) || file.matches( "[A-Za-z]:\\.*" ) )
			throw new SecurityException( "To protect system resources, this page has been blocked from accessing an absolute file path." );
		if ( file.startsWith( ".." + File.separator ) )
			throw new SecurityException( "To protect system resources, this page has been blocked from accessing a protected file path." );
		try
		{
			return fromFile( site.resourceFile( file ) );
		}
		catch ( IOException e )
		{
			ScriptingContext context = ScriptingContext.fromSource( "", file );
			context.getResult().addException( new ScriptingException( ReportingLevel.E_IGNORABLE, String.format( "Could not locate the file '%s' within webroot '%s'", file, site.getId() ), e ) );
			context.site( site );
			return context;
		}
	}

	public static WebrootScriptingContext fromPackage( final Site site, final String pack )
	{
		ScriptingContext context;

		try
		{
			File packFile = site.resourcePackage( pack );
			FileInterpreter fi = new FileInterpreter( packFile );
			context = ScriptingContext.fromFile( fi );
		}
		catch ( IOException e )
		{
			context = ScriptingContext.fromSource( "", pack );
			context.getResult().addException( new ScriptingException( ReportingLevel.E_IGNORABLE, String.format( "Could not locate the package '%s' within webroot '%s'", pack, site.getId() ), e ) );
		}

		context.site( site );

		return context;
	}

	public static WebrootScriptingContext fromPackageWithException( final Site site, final String pack ) throws IOException
	{
		File packFile = site.resourcePackage( pack );
		FileInterpreter fi = new FileInterpreter( packFile );
		ScriptingContext context = ScriptingContext.fromFile( fi );
		context.site( site );

		return context;
	}

	public static WebrootScriptingContext fromWebrootResource( @Nonnull BaseWebroot webroot, @Nonnull Path resourcePath ) throws ScriptingException.Runtime
	{
		try
		{
			if ( !resourcePath.isAbsolute() )
				resourcePath = resourcePath.resolve( webroot.getResourcePath() );
			resourcePath = resourcePath.toRealPath( LinkOption.NOFOLLOW_LINKS );

			if ( !resourcePath.startsWith( webroot.getResourcePath() ) )
				throw new ScriptingException.Runtime( "Path must be relative to the webroot's resource directory." );

			List<String> preferred = ScriptingContext.getPreferredExtensions();
			Path finalPath = null;

			// Are we looking for an incomplete resource path?
			if ( Files.isRegularFile( resourcePath ) )
				finalPath = resourcePath;
			else if ( Files.isDirectory( resourcePath ) )
			{
				final List<Path> matches = Files.list( resourcePath ).filter( path -> path.getFileName().startsWith( "index." ) ).collect( Collectors.toList() );
				for ( String ext : preferred )
					for ( Path filePath : matches )
						if ( filePath.getFileName().toString().toLowerCase().endsWith( "." + ext.toLowerCase() ) )
						{
							finalPath = filePath;
							break;
						}
			}
			else if ( Files.isDirectory( resourcePath.getParent() ) )
			{
				final Path resourcePath0 = resourcePath;
				final List<Path> matches = Files.list( resourcePath.getParent() ).filter( path -> path.getFileName().startsWith( resourcePath0.getFileName().toString() + "." ) ).collect( Collectors.toList() );
				for ( String ext : preferred )
					for ( Path filePath : matches )
						if ( filePath.getFileName().toString().toLowerCase().endsWith( "." + ext.toLowerCase() ) )
						{
							finalPath = filePath;
							break;
						}
			}

			if ( finalPath == null )
				throw new ScriptingException.Runtime( String.format( "Could not find script '%s' from webroot '%s' resource directory '%s'.", IO.relPath( resourcePath ), webroot.getId(), IO.relPath( webroot.getResourcePath() ) ) );

			return fromScriptInterpreter( finalPath );
		}
		catch ( Throwable cause )
		{
			return new ScriptingContext( cause );
		}
	}

	public static WebrootScriptingContext fromWebrootResource( @Nonnull BaseWebroot webroot, @Nonnull String resourceNamespace ) throws IOException
	{
		return fromWebrootResource( webroot, Paths.get( resourceNamespace.replace( '.', File.separatorChar ) ) );
	}

	private HttpRequestWrapper request;
	private BaseWebroot webroot;

	@Override
	protected Path getDefaultCachePath()
	{
		return webroot.getCachePath();
	}

	@Override
	public ScriptingFactory getScriptingFactory()
	{
		return request.getScriptingFactory();
	}

	public HttpRequestWrapper getRequest()
	{
		Objs.notNull( request );
		return request;
	}

	public BaseWebroot getWebroot()
	{
		Objs.notNull( webroot );
		return webroot;
	}
}
