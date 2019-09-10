package com.marchnetworks.web.util.compression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CompressResponseFilter implements Filter
{
	private static final Logger LOG = LoggerFactory.getLogger( CompressResponseFilter.class );

	public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain ) throws IOException, ServletException
	{
		if ( ( req instanceof HttpServletRequest ) )
		{
			HttpServletRequest request = ( HttpServletRequest ) req;
			HttpServletResponse response = ( HttpServletResponse ) res;

			String customEncoding = request.getHeader( "X-Accept-Encoding" );

			boolean compressionRequested = ( customEncoding != null ) && ( customEncoding.indexOf( "gzip" ) != -1 );

			if ( compressionRequested )
			{
				CompressedResponseWrapper wrappedResponse = new CompressedResponseWrapper( response );
				try
				{
					chain.doFilter( req, wrappedResponse );
				}
				catch ( ServletException ex )
				{
					LOG.warn( "Exception while applying compression to http response", ex );
					throw ex;
				}
				catch ( IOException ex )
				{
					LOG.warn( "Exception while applying compression to http response", ex );
					throw ex;
				}
				catch ( Exception ex )
				{
					LOG.warn( "Exception while applying compression to http response", ex );

					throw new ServletException( ex );
				}
				finally
				{
					wrappedResponse.finishResponse();
				}
				return;
			}
		}
		chain.doFilter( req, res );
	}

	public void init( FilterConfig filterConfig )
	{
	}

	public void destroy()
	{
	}
}
