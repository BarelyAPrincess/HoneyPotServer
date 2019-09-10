/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.http.HttpRequestWrapper;
import io.amelia.http.mappings.DomainMapping;
import io.amelia.http.routes.Route;
import io.amelia.http.routes.RouteResult;
import io.amelia.http.routes.Routes;
import io.amelia.http.webroot.WebrootRegistry;
import io.amelia.lang.HttpCode;
import io.amelia.lang.HttpError;
import io.amelia.net.Networking;
import io.amelia.scripting.ScriptingContext;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpRequestContext extends HttpContext
{
	private boolean fwRequest;
	private boolean isDirectoryRequest = false;
	private Map<String, String> rewriteParams = new TreeMap<>();
	private HttpCode status = HttpCode.HTTP_OK;

	public Map<String, String> getRewriteParams()
	{
		return rewriteParams;
	}

	public HttpCode getStatus()
	{
		return status;
	}

	public boolean isDirectoryRequest()
	{
		return isDirectoryRequest;
	}

	public boolean isFrameworkRequest()
	{
		return fwRequest;
	}

	public void readFromHttpRequest( HttpRequestWrapper request ) throws IOException, HttpError
	{
		List<String> preferredExtensions = ScriptingContext.getPreferredExtensions();
		Routes routes = request.getWebroot().getRoutes();
		String uri = request.getUri();
		Path dest = null;

		fwRequest = uri.startsWith( "wisp" );
		if ( fwRequest )
		{
			DomainMapping defaultMapping = WebrootRegistry.getDefaultWebroot().getDefaultMapping();
			request.setDomainMapping( defaultMapping );
			request.setUri( uri.substring( 5 ) );
			routes = defaultMapping.getWebroot().getRoutes();
		}
		else
		{
			DomainMapping mapping = request.getDomainMapping();
			if ( mapping.hasConfig( "redirect" ) && !Objs.isEmpty( mapping.getConfig( "redirect" ) ) )
			{
				String url = mapping.getConfig( "redirect" );
				status = HttpCode.getHttpCode( Objs.castToInt( mapping.getConfig( "redirectCode" ) ) ).orElse( HttpCode.HTTP_MOVED_PERM );
				request.getResponse().sendRedirect( url.toLowerCase().startsWith( "http" ) ? url : request.getFullDomain() + url, status );
				return;
			}
		}

		/* Search Site Routes */
		RouteResult routeResult = routes.searchRoutes( uri, request.getHostDomain() );

		if ( routeResult != null )
		{
			Route route = routeResult.getRoute();

			if ( route.isRedirect() )
			{
				/*Assume redirect action  */
				String url = route.hasParam( "redirect" ) ? route.getParam( "redirect" ) : route.getParam( "url" );
				status = HttpCode.getHttpCode( route.httpCode() ).orElse( HttpCode.HTTP_MOVED_TEMP );
				request.getResponse().sendRedirect( url.toLowerCase().startsWith( "http" ) ? url : request.getFullDomain() + url, status );
				return;
			}
			else if ( route.hasParam( "file" ) && !Objs.isEmpty( route.getParam( "file" ) ) )
			{
				/* Assume file action */
				Map<String, String> rewrites = routeResult.getRewrites();
				rewriteParams.putAll( rewrites );
				for ( Map.Entry<String, String> entry : route.getParams().entrySet() )
					putMetaValue( entry.getKey(), entry.getValue() );
				dest = request.getDomainMapping().directory().resolve( route.getParam( "file" ) );

				if ( rewrites.containsKey( "action" ) )
					action = rewrites.get( "action" );
				else
				{
					List<String> actions = new ArrayList<>();
					for ( int i = 0; i < 9; i++ )
						if ( rewrites.containsKey( "action" + i ) )
							actions.add( rewrites.get( "action" + i ) );
					action = Strs.join( actions, "/" );
				}

				if ( Files.notExists( dest ) )
					returnErrorOrThrowException( HttpResponseStatus.NOT_FOUND, "The route [%s] file [%s] does not exist.", route.getId(), IO.relPath( dest ) );
			}
			else
				returnErrorOrThrowException( HttpResponseStatus.INTERNAL_SERVER_ERROR, "The route [%s] has no action available, this is either a bug or one was not specified.", route.getId() );
		}
		else
		{
			dest = request.getDomainMapping().directory().resolve( uri );

			if ( Files.exists( dest ) && dest.getFileName().startsWith( "index." ) && ConfigRegistry.config.getBoolean( "advanced.security.disallowDirectIndexFiles" ).orElse( true ) )
				throw new HttpError( HttpCode.HTTP_FORBIDDEN, "Accessing index files by name is prohibited!" );

			if ( Files.exists( dest ) && dest.getFileName().toString().contains( ".controller." ) )
				throw new HttpError( HttpCode.HTTP_FORBIDDEN, "Accessing controller files by name is prohibited!" );
		}

		/* If our destination does not exist, try to determine if the uri simply contains server side options or is a filename with extension */
		if ( Files.notExists( dest ) )
			if ( Files.isDirectory( dest.getParent() ) )
			{
				final String searchA = dest.getFileName().toString() + ".";
				List<Path> results = Files.list( dest.getParent() ).filter( path -> path.startsWith( searchA ) ).collect( Collectors.toList() );

				if ( results.size() > 0 )
					for ( Path file : results )
					{
						String name = file.getFileName().toString();
						String ext = name.substring( name.indexOf( ".", dest.getFileName().toString().length() ) + 1 ).toLowerCase();
						if ( preferredExtensions.contains( ext ) )
						{
							dest = file;
							break;
						}
						else
							dest = file;
					}
				else if ( uri.contains( "_" ) )
				{
					/* Second check the server-side options, e.g., http://example.com/images/logo_x150.jpg = images/logo.jpg and resize to 150px wide. */
					String conditionExt;
					String newUri = uri;
					if ( newUri.contains( "." ) && newUri.lastIndexOf( "." ) > newUri.lastIndexOf( "_" ) )
					{
						conditionExt = newUri.substring( newUri.lastIndexOf( "." ) + 1 );
						newUri = newUri.substring( 0, newUri.lastIndexOf( "." ) );
					}
					else
						conditionExt = null;

					List<String> opts = new ArrayList<>();
					Path newFile;

					do
					{
						opts.add( newUri.substring( newUri.lastIndexOf( "_" ) + 1 ) );
						newUri = newUri.substring( 0, newUri.lastIndexOf( "_" ) );

						newFile = request.getDomainMapping().directory().resolve( conditionExt == null ? newUri : newUri + "." + conditionExt );
						if ( Files.exists( newFile ) )
							break;
						else if ( conditionExt == null )
						{
							final String searchB = newUri + ".";
							results = Files.list( dest.getParent() ).filter( path -> path.startsWith( searchB ) ).collect( Collectors.toList() );
							for ( Path file : results )
							{
								String ext = file.getFileName().toString().substring( newUri.length() + 1 ).toLowerCase();
								if ( preferredExtensions.contains( ext ) )
								{
									newFile = file;
									break;
								}
								else
									newFile = file;
							}
						}
					}
					while ( newUri.contains( "_" ) && Files.notExists( newFile ) );

					if ( Files.exists( newFile ) )
					{
						dest = newFile;
						rewriteParams.putAll( opts.stream().map( o -> {
							if ( o.contains( ":" ) )
								return new Pair<>( o.substring( 0, o.indexOf( ":" ) ), o.substring( o.indexOf( ":" ) + 1 ) );
							if ( o.contains( "=" ) )
								return new Pair<>( o.substring( 0, o.indexOf( "=" ) ), o.substring( o.indexOf( "=" ) + 1 ) );
							if ( o.contains( "-" ) )
								return new Pair<>( o.substring( 0, o.indexOf( "-" ) ), o.substring( o.indexOf( "-" ) + 1 ) );
							if ( o.contains( "~" ) )
								return new Pair<>( o.substring( 0, o.indexOf( "~" ) ), o.substring( o.indexOf( "~" ) + 1 ) );
							if ( o.substring( 0, 1 ).matches( "[a-z]" ) )
							{
								String key = Strs.regexCapture( o, "([a-z]+).*" );
								return new Pair<>( key, o.substring( key.length() ) );
							}
							return null;
						} ).filter( o -> !Objs.isNull( o ) ).collect( Collectors.toMap( Pair::getKey, Pair::getValue ) ) );
					}
				}
			}

		/* TODO Implement new file destination subroutines here! */

		/* If the specified file exists and is a directory, try to resolve the index file. */
		if ( Files.isDirectory( dest ) )
		{
			Map<String, List<Path>> maps = Files.list( dest ).filter( path -> path.startsWith( "index." ) ).collect( Collectors.groupingBy( IO::getFileExtension ) );
			Path selectedFile = null;

			if ( maps.size() > 0 )
			{
				for ( String ext : preferredExtensions )
					if ( maps.containsKey( ext.toLowerCase() ) )
					{
						selectedFile = Lists.first( maps.get( ext.toLowerCase() ) ).orElse( null );
						break;
					}
				if ( selectedFile == null )
					selectedFile = Lists.first( Lists.first( maps.values() ).orElseGet( ArrayList::new ) ).orElse( null );
			}

			if ( selectedFile != null )
			{
				request.enforceTrailingSlash( true );
				uri = uri + "/" + selectedFile.getFileName();
				dest = request.getDomainMapping().directory().resolve( uri );
			}
			else if ( ConfigRegistry.config.getBoolean( "server.allowDirectoryListing" ).orElse( false ) )
			{
				request.enforceTrailingSlash( true );
				isDirectoryRequest = true;
				return;
			}
			else
				throw new HttpError( 403, "Directory Listing is Prohibited" );
		}

		/* Search for Controllers */
		if ( Files.notExists( dest ) )
		{
			String newUri = Strs.trimAll( uri, '/' );
			Path newFile;

			if ( newUri.contains( "/" ) )
			{
				do
				{
					action = Arrays.stream( new String[] {newUri.substring( newUri.lastIndexOf( "/" ) + 1 ), action} ).filter( s -> !Objs.isEmpty( s ) ).collect( Collectors.joining( "/" ) );
					newUri = newUri.substring( 0, newUri.lastIndexOf( "/" ) );

					newFile = request.getDomainMapping().directory().resolve( newUri );
					Path parentFile = newFile.getParent();
					String fileName = newFile.getFileName().toString();

					if ( Files.exists( parentFile ) )
					{
						for ( Path file : Files.list( parentFile ).filter( path -> path.startsWith( fileName + ".controller." ) ).collect( Collectors.toList() ) )
						{
							String ext = file.getFileName().toString().substring( ( fileName + ".controller." ).length() ).toLowerCase();
							if ( preferredExtensions.contains( ext ) )
							{
								newFile = file;
								break;
							}
							else
								newFile = file;
						}

						Path indexDirectory = parentFile.resolve( fileName );
						if ( Files.isDirectory( indexDirectory ) )
						{
							for ( Path file : Files.list( indexDirectory ).filter( path -> path.startsWith( "index.controller." ) ).collect( Collectors.toList() ) )
							{
								String ext = file.getFileName().toString().substring( "index.controller.".length() ).toLowerCase();
								if ( preferredExtensions.contains( ext ) )
								{
									newFile = file;
									break;
								}
								else
									newFile = file;
							}
						}
					}
				}
				while ( newUri.contains( "/" ) && Files.notExists( newFile ) );

				if ( Files.exists( newFile ) )
					dest = newFile;
				else
					action = null;
			}
		}

		if ( Files.isRegularFile( dest ) )
		{
			if ( Objs.isEmpty( action ) && dest.getFileName().toString().contains( ".controller." ) )
				request.enforceTrailingSlash( true );
			readFromFile( dest );
		}
		else
			status = HttpCode.HTTP_NOT_FOUND;
	}

	public void returnErrorOrThrowException( HttpCode code, Throwable t, String message, Object... objs ) throws HttpError
	{
		if ( objs != null && objs.length > 0 )
			message = String.format( message, objs );

		if ( Kernel.isDevelopment() )
		{
			if ( t == null )
				throw new HttpError( HttpCode.HTTP_INTERNAL_SERVER_ERROR, message );
			else
				throw new HttpError( t, message );
		}
		else
		{
			Networking.L.warning( message );
			status = code;
		}
	}

	public void returnErrorOrThrowException( HttpResponseStatus code, String message, Object... objs ) throws HttpError
	{
		returnErrorOrThrowException( code, null, message, objs );
	}

	@Override
	public String toString()
	{
		String overrides = "";

		for ( Map.Entry<String, String> o : metaValues.entrySet() )
		{
			String l = o.getValue();
			if ( l != null )
			{
				l = l.replace( "\n", "" );
				l = l.replace( "\r", "" );
			}

			overrides += "," + o.getKey() + "=" + l;
		}

		if ( overrides.length() > 1 )
			overrides = overrides.substring( 1 );

		String rewrites = "";

		for ( Map.Entry<String, String> o : rewriteParams.entrySet() )
			rewrites += "," + o.getKey() + "=" + o.getValue();

		if ( rewrites.length() > 1 )
			rewrites = rewrites.substring( 1 );

		// String cachedFileStr = ( cachedFile == null ) ? "N/A" : cachedFile.getAbsolutePath();

		return "WebInterpreter[content=" + content.writerIndex() + " bytes,contentType=" + getContentType() + ",overrides=[" + overrides + "],rewrites=[" + rewrites + "]]";
	}
}
