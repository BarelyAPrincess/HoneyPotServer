package com.marchnetworks.web.test;

import com.marchnetworks.audit.common.AuditLogException;
import com.marchnetworks.audit.data.AuditSearchQuery;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.service.AuditLogService;
import com.marchnetworks.command.api.security.UserInformation;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.command.common.user.data.ProfileView;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.ServletUtils;
import com.marchnetworks.management.user.UserService;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "TestAudit", urlPatterns = {"/TestAudit"} )
public class AuditTestServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private AuditLogService auditLogService = ( AuditLogService ) ApplicationContextSupport.getBean( "auditLogServiceProxy_internal" );
	private UserService userService = ( UserService ) ApplicationContextSupport.getBean( "userServiceProxy_internal" );
	AuditSearchQuery auditViewCrit = new AuditSearchQuery();

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		List<String> userNames = getUserNames();
		createResponse( request, response, null, userNames, "Refresh Complete" );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		onCreation( request, response );
		String status = null;
		String userName = ServletUtils.getStringParameterValue( request.getParameter( "userName" ) );
		List<AuditView> auditviews = new ArrayList();
		auditViewCrit.setUsernames( new String[] {userName} );
		try
		{
			auditviews = auditLogService.getAuditLogs( auditViewCrit );
		}
		catch ( AuditLogException e )
		{
			status = "Error getting audit logs, Exception: " + e.getMessage();
		}
		status = "Refresh complete";
		List<String> userNames = getUserNames();
		createResponse( request, response, auditviews, userNames, status );
	}

	private void createResponse( HttpServletRequest request, HttpServletResponse response, List<AuditView> auditviews, List<String> userNames, String status ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );
		onCreation( request, response );

		if ( auditviews != null )
		{
			request.setAttribute( "auditviews", auditviews );
		}
		if ( userNames != null )
		{
			request.setAttribute( "userNames", userNames );
		}
		request.setAttribute( "status", status );
		getServletContext().getRequestDispatcher( "/WEB-INF/pages/AuditTest.jsp" ).forward( request, response );
	}

	public void onCreation( HttpServletRequest request, HttpServletResponse response )
	{
		List<MemberView> members = userService.listAllMembers();
		String adminUser = "";
		for ( MemberView member : members )
		{
			ProfileView profile = userService.getProfile( member.getProfileId() );
			if ( ( profile != null ) && ( profile.isSuperAdmin() ) )
			{
				adminUser = member.getName();
			}
		}
		UserInformation userInfo = new UserInformation( adminUser, null );
		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken( userInfo, "" );

		WebAuthenticationDetails details = new WebAuthenticationDetails( request );
		result.setDetails( details );

		SecurityContextHolder.getContext().setAuthentication( result );
	}

	private List<String> getUserNames()
	{
		List<MemberView> members = userService.listAllMembers();
		List<String> userNames = new ArrayList();
		for ( MemberView member : members )
		{
			userNames.add( member.getName() );
		}
		return userNames;
	}
}
