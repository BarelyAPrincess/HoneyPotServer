package com.marchnetworks.command.api.servlet;

import com.marchnetworks.command.api.security.UserInformation;
import com.marchnetworks.command.api.user.UserCoreService;
import com.marchnetworks.command.common.ReflectionUtils;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.command.common.user.data.ProfileView;
import com.marchnetworks.command.common.user.data.RightEnum;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ManagedServlet extends HttpServlet
{
	protected static final String STATUS = "status";
	protected static final String BASE_PAGE_PATH = "/WEB-INF/pages/";
	protected static final String JAVA_SERVLET_PAGE = ".jsp";
	ThreadLocal<HttpServletRequest> request = new ThreadLocal();
	ThreadLocal<HttpServletResponse> response = new ThreadLocal();

	private Map<String, Method> actionMap = new HashMap();

	static final Class<?>[] cArg = {HttpServletRequest.class, HttpServletResponse.class};
	private static final String SC_ATTRIBUTE_BUNDLE_CONTEXT = "osgi-bundlecontext";

	public void init() throws ServletException
	{
		Method[] methods = getClass().getMethods();
		for ( Method m : methods )
		{
			ManagedAction ann = ( ManagedAction ) m.getAnnotation( ManagedAction.class );
			if ( ann != null )
			{
				register( ann.action(), m.getName() );
			}
		}
	}

	private void register( String action, String method )
	{
		try
		{
			actionMap.put( action, getClass().getMethod( method, cArg ) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		doPost( request, response );
	}

	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		try
		{
			this.request.set( request );
			this.response.set( response );

			String action = request.getParameter( "action" );
			if ( action == null )
			{
				Map<String, String[]> paramMap = request.getParameterMap();

				for ( String k : paramMap.keySet() )
				{
					if ( k.startsWith( "action" ) )
					{
						action = request.getParameter( k );
						String[] ids = k.split( "-" );
						request.setAttribute( "action-id", ids[1] );
						break;
					}
				}
			}

			if ( !StringUtils.hasText( action ) )
			{
				action = "default";
			}

			Method method = ( Method ) actionMap.get( action );
			try
			{
				onCreation( request, response );
				if ( method != null )
				{
					method.invoke( this, new Object[] {request, response} );
				}
				onCompletion( request, response );
			}
			catch ( Exception e )
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream s = new PrintStream( baos, true );
				e.printStackTrace( s );
				request.setAttribute( "javax.servlet.error.message", new String( baos.toByteArray() ) );
				response.sendError( 500 );
			}

			Object annotation = ReflectionUtils.getAnnotationValue( getClass(), WebServlet.class, "urlPatterns" );

			String value = ( ( String[] ) ( String[] ) annotation )[0];

			getServletContext().getRequestDispatcher( "/WEB-INF/pages/" + value + ".jsp" ).forward( request, response );
		}
		finally
		{
			this.request.remove();
			this.response.remove();
		}
	}

	protected Long getActionId()
	{
		return Long.valueOf( Long.parseLong( ( String ) ( ( HttpServletRequest ) request.get() ).getAttribute( "action-id" ) ) );
	}

	public void onCreation( HttpServletRequest request, HttpServletResponse response )
	{
		BundleContext bundleContext = ( BundleContext ) getServletContext().getAttribute( "osgi-bundlecontext" );
		ServiceReference<UserCoreService> sr = bundleContext.getServiceReference( UserCoreService.class );
		UserCoreService userCoreService = ( UserCoreService ) bundleContext.getService( sr );
		Map<String, Set<String>> allAppRights = userCoreService.getAllAppRights();
		List<String> authoritiesAsStrings = new ArrayList();
		List<GrantedAuthority> authorities = new ArrayList();
		for ( Set<String> rights : allAppRights.values() )
		{
			for ( String right : rights )
			{
				authoritiesAsStrings.add( right );
				authorities.add( new SimpleGrantedAuthority( "ROLE_" + right ) );
			}
		}
		for ( RightEnum right : RightEnum.values() )
		{
			authoritiesAsStrings.add( right.name() );
			authorities.add( new SimpleGrantedAuthority( "ROLE_" + right.name() ) );
		}

		List<MemberView> members = userCoreService.listAllMembers();
		String adminUser = "";
		for ( MemberView member : members )
		{
			ProfileView profile = userCoreService.getProfile( member.getProfileId() );
			if ( ( profile != null ) && ( profile.isSuperAdmin() ) )
			{
				adminUser = member.getName();
				break;
			}
		}

		UserInformation userInfo = new UserInformation( adminUser, authoritiesAsStrings );
		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken( userInfo, "", authorities );

		WebAuthenticationDetails details = new WebAuthenticationDetails( request );
		result.setDetails( details );

		SecurityContextHolder.getContext().setAuthentication( result );
	}

	public void onCompletion( HttpServletRequest request, HttpServletResponse response )
	{
	}

	@ManagedAction( action = "Refresh" )
	protected void refresh( HttpServletRequest request, HttpServletResponse response )
	{
		request.setAttribute( "status", "Page Refresh" );
	}

	protected void setState( String status )
	{
		( ( HttpServletRequest ) request.get() ).setAttribute( "status", status );
	}

	protected void addEnumeration( Class... enums )
	{
		for ( Class i : enums )
		{
			String name = i.getSimpleName();
			Object[] val = i.getEnumConstants();
			( ( HttpServletRequest ) request.get() ).setAttribute( name, val );
		}
	}
}
