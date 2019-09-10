/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
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
import io.amelia.lang.ScriptingException;
import io.amelia.scripting.ScriptingContext;
import io.amelia.scripting.ScriptingFactory;
import io.amelia.support.FileContext;
import io.amelia.support.IO;
import io.amelia.support.Objs;

public class WebrootScriptingContext extends ScriptingContext<WebrootScriptingContext>
{
	public static WebrootScriptingContext empty()
	{
		return new WebrootScriptingContext();
	}

	public static WebrootScriptingContext fromCause( Throwable cause, Path file )
	{
		return fromCause( new ScriptingException.Runtime( String.format( "Could not locate the file '%s'", file ), cause ) ).setFileName( file.toString() );
	}

	public static WebrootScriptingContext fromCause( Throwable cause )
	{
		WebrootScriptingContext scriptingContext = empty();
		scriptingContext.getResult().handleException( cause );
		return scriptingContext;
	}

	public static WebrootScriptingContext fromFile( final Path file )
	{
		try
		{
			return fromFile( FileContext.fromFile( file ) );
		}
		catch ( IOException cause )
		{
			return fromCause( cause, file );
		}
	}

	public static WebrootScriptingContext fromFile( final FileContext fileContext )
	{
		WebrootScriptingContext context = fromSource( fileContext.getContentBytes(), fileContext.getFilePath() );
		context.setVirtual( false );
		context.setContentType( fileContext.getContentType() );
		context.setShell( fileContext.getAnnotations().get( "shell" ) );
		return context;
	}

	public static WebrootScriptingContext fromFile( final Webroot webroot, final Path file )
	{
		if ( webroot.isProtectedFilePath( webroot.getDirectory().resolve( file ) ) )
			throw new SecurityException( "This page has been blocked from accessing a protected system file path." );
		return fromFile( webroot.getDirectory().resolve( file ) ).setWebroot( webroot );
	}

	public static WebrootScriptingContext fromPackage( final Webroot webroot, final String pack )
	{
		WebrootScriptingContext scriptingContext;

		try
		{
			Path packFile = webroot.getResourcePackage( pack );
			FileContext fileContext = FileContext.fromFile( packFile );
			scriptingContext = WebrootScriptingContext.fromFile( fileContext );
		}
		catch ( IOException cause )
		{
			scriptingContext = WebrootScriptingContext.fromSource( "", pack );
			scriptingContext.getResult().addException( new ScriptingException.Runtime( String.format( "Could not locate the package '%s' within webroot '%s'", pack, webroot.getWebrootId() ), cause ) );
		}

		scriptingContext.setWebroot( webroot );

		return scriptingContext;
	}

	public static WebrootScriptingContext fromPackageWithException( final Webroot webroot, final String pack ) throws IOException
	{
		Path packFile = webroot.getResourcePackage( pack );
		FileContext fileContext = FileContext.fromFile( packFile );
		WebrootScriptingContext scriptingContext = fromFile( fileContext );
		scriptingContext.setWebroot( webroot );

		return scriptingContext;
	}

	public static WebrootScriptingContext fromSource( byte[] source )
	{
		return fromSource( source, "<no file>" );
	}

	public static WebrootScriptingContext fromSource( final byte[] source, final Path file )
	{
		return fromSource( source, file.toString() );
	}

	public static WebrootScriptingContext fromSource( final byte[] source, final String filename )
	{
		WebrootScriptingContext context = new WebrootScriptingContext();
		context.setVirtual( true );
		context.setFileName( filename );
		context.write( source );
		context.setBaseSource( new String( source, context.getCharset() ) );
		return context;
	}

	public static WebrootScriptingContext fromSource( String source )
	{
		return fromSource( source, "" );
	}

	public static WebrootScriptingContext fromSource( final String source, final File file )
	{
		return fromSource( source, file.getAbsolutePath() );
	}

	public static WebrootScriptingContext fromSource( final String source, final String filename )
	{
		WebrootScriptingContext context = fromSource( new byte[0], filename );
		context.write( source.getBytes( context.getCharset() ) );
		return context;
	}

	public static WebrootScriptingContext fromWebrootResource( @Nonnull Webroot webroot, @Nonnull Path resourcePath ) throws ScriptingException.Runtime
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
				throw new ScriptingException.Runtime( String.format( "Could not find script '%s' from webroot '%s' resource directory '%s'.", IO.relPath( resourcePath ), webroot.getWebrootId(), IO.relPath( webroot.getResourcePath() ) ) );

			return fromFile( finalPath );
		}
		catch ( Throwable cause )
		{
			WebrootScriptingContext scriptingContext = empty();
			scriptingContext.getResult().handleException( cause );
			return scriptingContext;
		}
	}

	public static WebrootScriptingContext fromWebrootResource( @Nonnull Webroot webroot, @Nonnull String resourceNamespace ) throws IOException
	{
		return fromWebrootResource( webroot, Paths.get( resourceNamespace.replace( '.', File.separatorChar ) ) );
	}

	private HttpRequestWrapper request;
	private Webroot webroot;

	@Override
	protected Path getDefaultCachePath()
	{
		return webroot.getCacheDirectory();
	}

	public HttpRequestWrapper getRequest()
	{
		Objs.notNull( request );
		return request;
	}

	@Override
	public ScriptingFactory getScriptingFactory()
	{
		return request.getScriptingFactory();
	}

	@Override
	public Path getSourceDirectory()
	{
		Path current = getPath();
		if ( current.startsWith( webroot.getResourceDirectory() ) )
			return webroot.getResourceDirectory();
		if ( current.startsWith( webroot.getPublicDirectory() ) )
			return webroot.getPublicDirectory();
		else
			return webroot.getDirectory();
	}

	public Webroot getWebroot()
	{
		Objs.notNull( webroot );
		return webroot;
	}

	public WebrootScriptingContext setWebroot( Webroot webroot )
	{
		this.webroot = webroot;
		return this;
	}
}
