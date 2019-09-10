package com.marchnetworks.command.api.servlet;

import com.marchnetworks.command.common.HttpUtils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LocalConnectionFilter implements Filter
{
	public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain ) throws IOException, ServletException
	{
		if ( ( req instanceof HttpServletRequest ) )
		{
			HttpServletRequest request = ( HttpServletRequest ) req;
			HttpServletResponse response = ( HttpServletResponse ) res;

			if ( !HttpUtils.isLocalAddress( request.getRemoteAddr() ) )
			{
				response.sendError( 403, "Only local connections are allowed." );
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
