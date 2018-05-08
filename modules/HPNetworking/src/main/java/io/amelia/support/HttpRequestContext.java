package io.amelia.support;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.amelia.foundation.Kernel;
import io.amelia.http.HttpError;
import io.amelia.http.HttpRequestWrapper;
import io.amelia.networking.Networking;
import io.amelia.scripting.ScriptingContext;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpRequestContext extends FileContext
{
	private String action = null;
	private boolean fwRequest = false;
	private boolean isDirectoryRequest = false;
	private Map<String, String> rewriteParams = new TreeMap<>();
	private HttpResponseStatus status = HttpResponseStatus.OK;

	public HttpRequestContext( HttpRequestWrapper request ) throws IOException, HttpError
	{
		super();

		List<String> preferredExtensions = ScriptingContext.getPreferredExtensions();
		Routes routes = request.getLocation().getRoutes();
		String uri = request.getUri();
		File dest = null;

		fwRequest = uri.startsWith( "wisp" );
		if ( fwRequest )
		{
			DomainMapping defaultMapping = SiteManager.instance().getDefaultSite().getDefaultMapping();
			request.setDomainMapping( defaultMapping );
			request.setUri( uri.substring( 5 ) );
			routes = defaultMapping.getSite().getRoutes();
		}
		else
		{
			DomainMapping mapping = request.getDomainMapping();
			if ( mapping.hasConfig( "redirect" ) && !Objs.isEmpty( mapping.getConfig( "redirect" ) ) )
			{
				String url = mapping.getConfig( "redirect" );
				status = HttpResponseStatus.valueOf( Objs.castToInt( mapping.getConfig( "redirectCode" ), 301 ) );
				request.getResponse().sendRedirect( url.toLowerCase().startsWith( "http" ) ? url : request.getFullDomain() + url, status.code() );
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
				status = HttpResponseStatus.valueOf( route.httpCode() );
				request.getResponse().sendRedirect( url.toLowerCase().startsWith( "http" ) ? url : request.getFullDomain() + url, status.code() );
				return;
			}
			else if ( route.hasParam( "file" ) && !Objs.isEmpty( route.getParam( "file" ) ) )
			{
				/* Assume file action */
				Map<String, String> rewrites = routeResult.getRewrites();
				rewriteParams.putAll( rewrites );
				annotations.putAll( route.getParams() );
				dest = new File( request.getDomainMapping().directory(), route.getParam( "file" ) );

				if ( rewrites.containsKey( "action" ) )
					action = rewrites.get( "action" );
				else
				{
					List<String> actions = new ArrayList<>();
					for ( int i = 0; i < 9; i++ )
						if ( rewrites.containsKey( "action" + i ) )
							actions.add( rewrites.get( "action" + i ) );
					action = actions.stream().collect( Collectors.joining( "/" ) );
				}

				if ( !dest.exists() )
					returnErrorOrThrowException( HttpResponseStatus.NOT_FOUND, "The route [%s] file [%s] does not exist.", route.getId(), dest.getAbsolutePath() );
			}
			else
				returnErrorOrThrowException( HttpResponseStatus.INTERNAL_SERVER_ERROR, "The route [%s] has no action available, this is either a bug or one was not specified.", route.getId() );
		}
		else
		{
			dest = new File( request.getDomainMapping().directory(), uri );

			if ( dest.exists() && dest.getName().startsWith( "index." ) && AppConfig.get().getBoolean( "advanced.security.disallowDirectIndexFiles", true ) )
				throw new HttpError( HttpResponseStatus.FORBIDDEN, "Accessing index files by name is disallowed!" );

			if ( dest.exists() && dest.getName().contains( ".controller." ) )
				throw new HttpError( HttpResponseStatus.FORBIDDEN, "Accessing controller files by name is disallowed!" );
		}

		/* If our destination does not exist, try to determine if the uri simply contains server side options or is a filename with extension */
		if ( !dest.exists() )
			if ( dest.getParentFile().exists() && dest.getParentFile().isDirectory() )
			{
				FileFilter fileFilter = new WildcardFileFilter( dest.getName() + ".*" );
				File[] files = dest.getParentFile().listFiles( fileFilter );

				if ( files != null && files.length > 0 )
				{
					/* First check is any files with the specified name prefix exist */
					for ( File f : files )
						if ( f.exists() )
						{
							String name = f.getName();
							String ext = name.substring( name.indexOf( ".", dest.getName().length() ) + 1 ).toLowerCase();
							if ( preferredExtensions.contains( ext ) )
							{
								dest = f;
								break;
							}
							else
								dest = f;
						}
				}
				else if ( uri.contains( "_" ) )
				{
					/* Second check the server-side options, e.g., http://images.example.com/logo_x150.jpg = images/logo.jpg and resize to 150px wide. */
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
					File newFile;

					do
					{
						opts.add( newUri.substring( newUri.lastIndexOf( "_" ) + 1 ) );
						newUri = newUri.substring( 0, newUri.lastIndexOf( "_" ) );

						newFile = new File( request.getDomainMapping().directory(), conditionExt == null ? newUri : newUri + "." + conditionExt );
						if ( newFile.exists() )
							break;
						else if ( conditionExt == null )
						{
							fileFilter = new WildcardFileFilter( newUri + ".*" );
							files = dest.getParentFile().listFiles( fileFilter );

							if ( files != null && files.length > 0 )
								for ( File f : files )
									if ( f.exists() )
									{
										String ext = f.getName().substring( newUri.length() + 1 ).toLowerCase();
										if ( preferredExtensions.contains( ext ) )
										{
											newFile = f;
											break;
										}
										else
											newFile = f;
									}
						}
					}
					while ( newUri.contains( "_" ) && !newFile.exists() );

					if ( newFile.exists() )
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
								String key = UtilStrings.regexCapture( o, "([a-z]+).*" );
								return new Pair<>( key, o.substring( key.length() ) );
							}
							return null;
						} ).filter( o -> !Objs.isNull( o ) ).collect( Collectors.toMap( Pair::getKey, Pair::getValue ) ) );
					}
				}
			}

		/* TODO Implement new file destination subroutines here! */

		/* If the specified file exists and is a directory, try to resolve the index file. */
		if ( dest.exists() && dest.isDirectory() )
		{
			FileFilter fileFilter = new WildcardFileFilter( "index.*" );
			Map<String, File> maps = UtilIO.mapExtensions( dest.listFiles( fileFilter ) );

			File selectedFile = null;

			if ( maps.size() > 0 )
			{
				for ( String ext : preferredExtensions )
					if ( maps.containsKey( ext.toLowerCase() ) )
					{
						selectedFile = maps.get( ext.toLowerCase() );
						break;
					}
				if ( selectedFile == null )
					selectedFile = new ArrayList<>( maps.values() ).get( 0 );
			}

			if ( selectedFile != null )
			{
				request.enforceTrailingSlash();
				uri = uri + "/" + selectedFile.getName();
				dest = new File( request.getDomainMapping().directory(), uri );
			}
			else if ( AppConfig.get().getBoolean( "server.allowDirectoryListing" ) )
			{
				request.enforceTrailingSlash();
				isDirectoryRequest = true;
				return;
			}
			else
				throw new HttpError( 403, "Directory Listing is Disallowed" );
		}

		/* Search for Controllers */
		if ( !dest.exists() )
		{
			String newUri = UtilStrings.trimAll( uri, '/' );
			File newFile;

			if ( newUri.contains( "/" ) )
			{
				do
				{
					action = Arrays.stream( new String[] {newUri.substring( newUri.lastIndexOf( "/" ) + 1 ), action} ).filter( s -> !Objs.isEmpty( s ) ).collect( Collectors.joining( "/" ) );
					newUri = newUri.substring( 0, newUri.lastIndexOf( "/" ) );

					newFile = new File( request.getDomainMapping().directory(), newUri );
					File parentFile = newFile.getParentFile();
					String fileName = newFile.getName();

					if ( parentFile.exists() )
					{
						FileFilter fileFilter = new WildcardFileFilter( fileName + ".controller.*" );
						File[] files = parentFile.listFiles( fileFilter );

						if ( files != null && files.length > 0 )
							for ( File f : files )
								if ( f.exists() )
								{
									String ext = f.getName().substring( ( fileName + ".controller." ).length() ).toLowerCase();
									if ( preferredExtensions.contains( ext ) )
									{
										newFile = f;
										break;
									}
									else
										newFile = f;
								}

						File indexDirectory = new File( parentFile, fileName );
						if ( indexDirectory.isDirectory() )
						{
							fileFilter = new WildcardFileFilter( "index.controller.*" );
							files = indexDirectory.listFiles( fileFilter );

							if ( files != null && files.length > 0 )
								for ( File f : files )
									if ( f.exists() )
									{
										String ext = f.getName().substring( ( fileName + ".controller." ).length() ).toLowerCase();
										if ( preferredExtensions.contains( ext ) )
										{
											newFile = f;
											break;
										}
										else
											newFile = f;
									}
						}
					}
				}
				while ( newUri.contains( "/" ) && !newFile.exists() );

				if ( newFile.exists() )
					dest = newFile;
				else
					action = null;
			}
		}

		if ( dest.exists() && !dest.isDirectory() )
		{
			if ( Objs.isEmpty( action ) && dest.getName().contains( ".controller." ) )
				request.enforceTrailingSlash();
			readFromFile( dest );
		}
		else
			status = HttpResponseStatus.NOT_FOUND;
	}

	public String getAction()
	{
		return action;
	}

	public Map<String, String> getRewriteParams()
	{
		return rewriteParams;
	}

	public HttpResponseStatus getStatus()
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

	public void returnErrorOrThrowException( HttpResponseStatus code, Throwable t, String message, Object... objs ) throws HttpError
	{
		if ( objs != null && objs.length > 0 )
			message = String.format( message, objs );

		if ( Kernel.isDevelopment() )
		{
			if ( t == null )
				throw new HttpError( HttpResponseStatus.INTERNAL_SERVER_ERROR, message );
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

		for ( Map.Entry<String, String> o : annotations.entrySet() )
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

		return "WebInterpreter[content=" + data.writerIndex() + " bytes,contentType=" + getContentType() + ",overrides=[" + overrides + "],rewrites=[" + rewrites + "]]";
	}
}
