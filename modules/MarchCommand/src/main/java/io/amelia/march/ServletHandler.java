package io.amelia.march;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import io.amelia.foundation.Foundation;
import io.amelia.http.HttpRequestWrapper;
import io.amelia.http.HttpResponseWrapper;
import io.amelia.lang.ApplicationException;

public class ServletHandler
{
	private static final Set<ServletRegistration> registrations = new HashSet<>();

	static
	{
		Set<Class<?>> servletsFound = Foundation.getReflections().getTypesAnnotatedWith( WebServlet.class );

		for ( Class<?> servletClass : servletsFound )
		{
			try
			{
				registrations.add( new ServletRegistration( servletClass, Foundation.make( servletClass ).orElseThrow( () -> new ApplicationException.Error( "Foundation#make() returned empty." ) ) ) );
			}
			catch ( ApplicationException.Error e )
			{
				Foundation.L.severe( "We encountered a exception while trying to construct the servlet class \"" + servletClass.getName() + "\".", e );
			}
		}
	}

	public void process( HttpRequestWrapper request, HttpResponseWrapper response )
	{


		registrations.stream().filter( reg -> reg.matches( uri ) ).findFirst().ifPresent( reg -> {
			HttpServlet httpServlet = reg.servletInstance;

			httpServlet.service( request, response );
		} );
	}

	private static class ServletRegistration<T extends HttpServlet>
	{
		private final boolean asyncSupported;
		private final String description;
		private final String displayName;
		private final WebInitParam[] initParams;
		private final String largeIcon;
		private final int loadOnStartup;
		private final String name;
		private final T servletInstance;
		private final String smallIcon;
		private final String[] urlPatterns;
		private final String[] value;

		public ServletRegistration( Class<T> servletClass, T servletInstance )
		{
			this.servletInstance = servletInstance;

			WebServlet webServlet = servletClass.getAnnotation( WebServlet.class );

			this.name = webServlet.name();
			this.value = webServlet.value();
			this.urlPatterns = webServlet.urlPatterns();
			this.loadOnStartup = webServlet.loadOnStartup();
			this.initParams = webServlet.initParams();
			this.asyncSupported = webServlet.asyncSupported();
			this.smallIcon = webServlet.smallIcon();
			this.largeIcon = webServlet.largeIcon();
			this.description = webServlet.description();
			this.displayName = webServlet.displayName();
		}

		public boolean matches( String uri )
		{
			return Arrays.stream( urlPatterns ).anyMatch( url -> url.startsWith( uri ) );
		}
	}
}
