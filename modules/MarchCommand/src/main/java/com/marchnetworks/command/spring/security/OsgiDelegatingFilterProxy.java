package com.marchnetworks.command.spring.security;

import com.marchnetworks.command.spring.SpringSupport;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class OsgiDelegatingFilterProxy extends GenericFilterBean
{
	private static final Logger LOG = LoggerFactory.getLogger( OsgiDelegatingFilterProxy.class );

	private static final String SC_ATTRIBUTE_BUNDLE_CONTEXT = "osgi-bundlecontext";
	private String contextAttribute;
	private String targetBeanName;
	private boolean targetFilterLifecycle = false;
	private Filter delegate;
	private final Object delegateMonitor = new Object();

	public void setContextAttribute( String contextAttribute )
	{
		this.contextAttribute = contextAttribute;
	}

	public String getContextAttribute()
	{
		return contextAttribute;
	}

	public void setTargetBeanName( String targetBeanName )
	{
		this.targetBeanName = targetBeanName;
	}

	protected String getTargetBeanName()
	{
		return targetBeanName;
	}

	public void setTargetFilterLifecycle( boolean targetFilterLifecycle )
	{
		this.targetFilterLifecycle = targetFilterLifecycle;
	}

	protected boolean isTargetFilterLifecycle()
	{
		return targetFilterLifecycle;
	}

	protected void initFilterBean() throws ServletException
	{
		if ( targetBeanName == null )
		{
			targetBeanName = getFilterName();
		}

		synchronized ( delegateMonitor )
		{
			delegate = initDelegate();
		}
	}

	public void doFilter( ServletRequest request, ServletResponse response, FilterChain filterChain ) throws ServletException, IOException
	{
		Filter delegateToUse = null;
		synchronized ( delegateMonitor )
		{
			if ( delegate == null )
			{
				delegate = initDelegate();
				if ( delegate == null )
				{
					throw new IllegalStateException( "OsgiDelegatingFilterProxy can not find " + getTargetBeanName() + " bean" );
				}
			}
			delegateToUse = delegate;
		}

		invokeDelegate( delegateToUse, request, response, filterChain );
	}

	public void destroy()
	{
		Filter delegateToUse = null;
		synchronized ( delegateMonitor )
		{
			delegateToUse = delegate;
		}
		if ( delegateToUse != null )
		{
			destroyDelegate( delegateToUse );
		}
	}

	protected Filter initDelegate() throws ServletException
	{
		Filter delegate = null;

		ServletContext sc = getServletContext();
		BundleContext bundleContext = ( BundleContext ) sc.getAttribute( "osgi-bundlecontext" );
		if ( bundleContext == null )
		{
			LOG.error( "Could not obtain BundleContext from osgi-bundlecontext servlet context property" );
			return null;
		}

		Bundle bundle = bundleContext.getBundle();
		try
		{
			delegate = ( Filter ) SpringSupport.beanFromBundle( bundle, getTargetBeanName() );
		}
		catch ( RuntimeException e )
		{
			LOG.error( "Could not obtain " + getTargetBeanName() + " bean, Exception: " + e.getMessage() );
			return null;
		}

		if ( ( delegate != null ) && ( isTargetFilterLifecycle() ) )
		{
			delegate.init( getFilterConfig() );
		}
		return delegate;
	}

	protected void invokeDelegate( Filter delegate, ServletRequest request, ServletResponse response, FilterChain filterChain ) throws ServletException, IOException
	{
		delegate.doFilter( request, response, filterChain );
	}

	protected void destroyDelegate( Filter delegate )
	{
		if ( isTargetFilterLifecycle() )
		{
			delegate.destroy();
		}
	}
}
