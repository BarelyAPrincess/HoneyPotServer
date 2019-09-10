package com.marchnetworks.web.info;

import com.marchnetworks.command.common.timezones.TimezonesDictionary;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "InfoTimezones", urlPatterns = {"/InfoTimezones"} )
public class TimezonesInfoServlet extends HttpServlet
{
	private static final String TRANLSATES_TO = " tranlsates to ";
	private static final long serialVersionUID = 1L;

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		request.setAttribute( "size", Integer.valueOf( TimezonesDictionary.getTimezones().size() ) );
		request.setAttribute( "timezones", TimezonesDictionary.getTimezones() );

		setWindows( request );
		setOlsons( request );
		setEnglish( request );

		getServletContext().getRequestDispatcher( "/WEB-INF/pages/InfoTimezones.jsp" ).forward( request, response );
	}

	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		String olson = request.getParameter( "olson" );
		String window = request.getParameter( "window" );
		String english = request.getParameter( "english" );
		String windowToEnglish = request.getParameter( "windowToEnglish" );

		if ( ( olson != null ) && ( !olson.isEmpty() ) )
		{
			request.setAttribute( "fromOlsonToWindow", olson + " tranlsates to " + TimezonesDictionary.fromOlsonToWindow( olson ) );
		}

		if ( ( window != null ) && ( !window.isEmpty() ) )
		{
			request.setAttribute( "fromWindowToOlson", window + " tranlsates to " + TimezonesDictionary.fromWindowToOlson( window ) );
		}

		if ( ( english != null ) && ( !english.isEmpty() ) )
		{
			request.setAttribute( "fromEnglishToWindow", english + " tranlsates to " + TimezonesDictionary.fromEnglishToWindow( english ) );
		}

		if ( ( windowToEnglish != null ) && ( !windowToEnglish.isEmpty() ) )
		{
			request.setAttribute( "fromWindowToEnglish", windowToEnglish + " tranlsates to " + TimezonesDictionary.fromWindowToEnglish( windowToEnglish ) );
		}

		doGet( request, response );
	}

	private void setEnglish( HttpServletRequest request )
	{
		request.setAttribute( "english", TimezonesDictionary.getAllEnglishTimezones() );
	}

	private void setWindows( HttpServletRequest request )
	{
		request.setAttribute( "windows", TimezonesDictionary.getAllWindowsTimezones() );
	}

	private void setOlsons( HttpServletRequest request )
	{
		List<String> olsons = TimezonesDictionary.getAllOlson();
		Map<String, String> olsonToWindow = new Hashtable();
		for ( String olson : olsons )
		{
			olsonToWindow.put( olson, TimezonesDictionary.fromOlsonToWindow( olson ) );
		}

		request.setAttribute( "olsons", olsonToWindow );
	}
}
