package com.marchnetworks.management.app;

import com.marchnetworks.app.data.App;
import com.marchnetworks.app.data.AppStatus;
import com.marchnetworks.app.data.TestApp;
import com.marchnetworks.app.service.AppConstants;
import com.marchnetworks.app.service.AppManager;
import com.marchnetworks.command.common.app.AppException;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.license.model.ApplicationIdentityToken;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "AppTest", urlPatterns = {"/AppTest"} )
public class AppServlet extends HttpServlet
{
	private static final long serialVersionUID = 6814357323970544714L;
	private AppManager appManager = ( AppManager ) ApplicationContextSupport.getBean( "appManagerProxy_internal" );

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );
		PrintWriter out = response.getWriter();
		createPageContent( request.getContextPath(), out, "Refresh complete" );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );

		String installFilename = null;
		String startId = null;
		String restartId = null;
		String stopId = null;
		String uninstallId = null;
		String upgradeFileName = null;
		boolean shouldStartAll = false;
		boolean shouldStopAll = false;
		Enumeration<String> params = request.getParameterNames();
		while ( params.hasMoreElements() )
		{
			String param = ( String ) params.nextElement();
			if ( param.startsWith( "installApp-" ) )
			{
				installFilename = param.substring( param.indexOf( "-" ) + 1 );
			}
			if ( param.startsWith( "startApp-" ) )
			{
				startId = param.substring( param.indexOf( "-" ) + 1 );
			}
			if ( param.startsWith( "restartApp-" ) )
			{
				restartId = param.substring( param.indexOf( "-" ) + 1 );
			}
			if ( param.startsWith( "stopApp-" ) )
			{
				stopId = param.substring( param.indexOf( "-" ) + 1 );
			}
			if ( param.startsWith( "uninstallApp-" ) )
			{
				uninstallId = param.substring( param.indexOf( "-" ) + 1 );
			}
			if ( param.startsWith( "upgradeApp-" ) )
			{
				upgradeFileName = param.substring( param.indexOf( "-" ) + 1 );
			}
			if ( param.startsWith( "startAll" ) )
			{
				shouldStartAll = true;
			}
			if ( param.startsWith( "stopAll" ) )
			{
				shouldStopAll = true;
			}
		}

		TaskScheduler taskScheduler = ( TaskScheduler ) ApplicationContextSupport.getBean( "taskScheduler" );
		if ( shouldStartAll )
		{
			for ( App app : appManager.getApps() )
			{
				if ( app.getStatus() != AppStatus.RUNNING )
				{
					final String appId = app.getIdentity().getId();
					taskScheduler.executeNow( new Runnable()
					{
						public void run()
						{
							try
							{
								appManager.start( appId );
							}
							catch ( AppException e )
							{
								System.out.println( "Failed to Start add-in: " + e );
							}
						}
					} );
				}
			}
		}

		if ( shouldStopAll )
		{
			for ( App app : appManager.getApps() )
			{
				if ( app.getStatus() == AppStatus.RUNNING )
				{
					final String appId = app.getIdentity().getId();
					taskScheduler.executeNow( new Runnable()
					{
						public void run()
						{
							try
							{
								appManager.stop( appId );
							}
							catch ( AppException e )
							{
								System.out.println( "Failed to Stop add-in: " + e );
							}
						}
					} );
				}
			}
		}

		String status = "";
		if ( installFilename != null )
		{
			status = "Installed App " + installFilename;
			try
			{
				appManager.install( AppConstants.APP_TEST_DIRECTORY + installFilename );
			}
			catch ( AppException e )
			{
				status = "Failed to install App: " + e;
			}
		}
		else if ( startId != null )
		{
			status = "Started App " + startId;
			try
			{
				appManager.start( startId );
			}
			catch ( AppException e )
			{
				status = "Failed to start App: " + e;
			}
		}
		else if ( restartId != null )
		{
			status = "Restarted App " + restartId;
			try
			{
				appManager.restart( restartId );
			}
			catch ( AppException e )
			{
				status = "Failed to restart App: " + e;
			}
		}
		else if ( stopId != null )
		{
			status = "Stopped App " + stopId;
			try
			{
				appManager.stop( stopId );
			}
			catch ( AppException e )
			{
				status = "Failed to stop App: " + e;
			}
		}
		else if ( uninstallId != null )
		{
			status = "Uninstalled App " + uninstallId;
			try
			{
				appManager.uninstall( uninstallId );
			}
			catch ( AppException e )
			{
				status = "Failed to uninstall App: " + e;
			}
		}
		else if ( upgradeFileName != null )
		{
			status = "Upgraded App " + upgradeFileName;
			try
			{
				appManager.upgrade( AppConstants.APP_TEST_DIRECTORY + upgradeFileName, null );
			}
			catch ( AppException e )
			{
				status = "Failed to upgrade App: " + e;
			}
		}
		else
		{
			status = "Refresh complete";
		}

		PrintWriter out = response.getWriter();
		createPageContent( request.getContextPath(), out, status );
	}

	public void createPageContent( String path, PrintWriter out, String status )
	{
		App[] apps = appManager.getApps();
		List<TestApp> availableApps = appManager.getAvailableApps();

		out.println( "<html><head>" );

		out.println( "<title>App Manager</title></head><body>" );
		out.println( "<h2>App Manager</h2>" );

		out.println( "<form method='post' action ='" + path + "/AppTest' >" );

		out.println( "<h3>Installed Apps</h3>" );
		out.println( "<table border='1' cellpadding='2'>" );
		out.println( "<tr>" );
		out.println( "<th>ID</th>" );
		out.println( "<th>Name</th>" );
		out.println( "<th>App Type</th>" );
		out.println( "<th>Version</th>" );
		out.println( "<th>Developer</th>" );
		out.println( "<th>Description</th>" );
		out.println( "<th>Status</th>" );
		out.println( "<th>Installed Time</th>" );
		out.println( "<th>Started Time</th>" );
		out.println( "<th colspan='4'>Action</th>" );
		out.println( "<th></th>" );
		out.println( "</tr>" );

		for ( App app : apps )
		{
			out.println( "<tr>" );
			out.println( "<td>" + app.getIdentity().getId() + "</td>" );
			out.println( "<td>" + app.getIdentity().getName() + "</td>" );
			out.println( "<td>" + app.getIdentity().getAppType() + "</td>" );
			out.println( "<td>" + app.getVersion() + "</td>" );
			out.println( "<td>" + app.getIdentity().getDeveloper() + "</td>" );
			out.println( "<td>" + app.getIdentity().getDescription() + "</td>" );
			out.println( "<td>" + app.getStatus().name() + "</td>" );
			out.println( "<td>" + DateUtils.getDateStringFromMicros( app.getInstalledTime() ) + "</td>" );
			out.println( "<td>" + DateUtils.getDateStringFromMicros( app.getStartedTime() ) + "</td>" );
			String startName = "startApp-" + app.getIdentity().getId();
			String restartName = "restartApp-" + app.getIdentity().getId();
			String stopName = "stopApp-" + app.getIdentity().getId();
			String uninstallName = "uninstallApp-" + app.getIdentity().getId();
			out.println( "<td align='center'><input type='submit' name='" + startName + "' value='Start'></td>" );
			out.println( "<td><input type='submit' name='" + restartName + "' value='Restart'></td>" );
			out.println( "<td><input type='submit' name='" + stopName + "' value='Stop'></td>" );
			out.println( "<td><input type='submit' name='" + uninstallName + "' value='Uninstall'></td>" );
			out.println( "</tr>" );
		}
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td></td>" );
		out.println( "<td><input type='submit' name='startAll' value='Start All'> <input type='submit' name='stopAll' value='Stop All'></td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<h3>Available Apps</h3>" );
		out.println( "<table border='1' cellpadding='2'>" );
		out.println( "<tr>" );
		out.println( "<th>Filename</th>" );
		out.println( "<th></th>" );
		out.println( "</tr>" );

		for ( TestApp app : availableApps )
		{
			out.println( "<tr>" );
			out.println( "<td>" + app.getFilePath() + "</td>" );
			String installName = "installApp-" + app.getFilePath();
			String upgradeName = "upgradeApp-" + app.getFilePath();
			if ( app.isInstalled() )
			{
				out.println( "<td><input type='submit' name='" + upgradeName + "' value='Upgrade'></td>" );
			}
			else
			{
				out.println( "<td><input type='submit' name='" + installName + "' value='Install'></td>" );
			}
			out.println( "</tr>" );
		}
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<input type='submit' name='refresh' value=Refresh>" );
		out.println( "</form>" );

		out.println( "<h4>Status: " + status + "</h4>" );

		out.println( "</body></html>" );
	}
}
