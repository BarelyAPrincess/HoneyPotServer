package com.marchnetworks.web.test;

import com.marchnetworks.command.api.user.UserCoreService;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.AppProfileData;
import com.marchnetworks.command.common.user.data.ProfileView;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.user.UserService;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.amelia.support.Strs;

@WebServlet( name = "TestUserRights", urlPatterns = {"/TestUserRights"} )
public class UserRightTestServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private final UserCoreService userCoreService = ( UserCoreService ) ApplicationContextSupport.getBean( "userCoreServiceProxy" );
	private final UserService userService = ( UserService ) ApplicationContextSupport.getBean( "userServiceProxy_internal" );

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		prepareToPage( request, response );
	}

	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		String profileId = request.getParameter( "profileId" );
		String[] appRights = request.getParameterValues( "appRights" );

		Map<String, Set<String>> map = new Hashtable();

		for ( String appRight : appRights )
		{
			String[] splitted = Strs.split( appRight, "&" ).toArray( String[]::new );
			String appId = splitted[0].trim();
			String right = splitted[1].trim();
			Set<String> rights = ( Set ) map.get( appId );
			if ( rights == null )
			{
				rights = new HashSet();
			}
			rights.add( right );
			map.put( appId, rights );
		}

		ProfileView profile = userService.getProfile( Long.valueOf( profileId ) );
		Set<AppProfileData> appProfileDatas = new HashSet();
		for ( Entry<String, Set<String>> entry : map.entrySet() )
		{
			appProfileDatas.add( new AppProfileData( ( String ) entry.getKey(), ( Set ) entry.getValue() ) );
		}

		profile.setAppProfileData( appProfileDatas );
		try
		{
			userService.updateProfile( profile, false );
		}
		catch ( UserException e )
		{
			log( e.getMessage() );
		}

		prepareToPage( request, response );
	}

	private void prepareToPage( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		request.setAttribute( "profiles", userService.listAllProfiles() );
		request.setAttribute( "appsRights", userCoreService.getAllAppRights().entrySet() );
		getServletContext().getRequestDispatcher( "/WEB-INF/pages/UserRightsTest.jsp" ).forward( request, response );
	}
}
