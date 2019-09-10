package com.marchnetworks.command.api.notification;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.DateUtils;
import com.marchnetworks.command.common.FastStringTemplate;
import com.marchnetworks.command.common.notification.data.Notification;
import com.marchnetworks.command.common.notification.data.NotificationContent;
import com.marchnetworks.command.common.notification.data.NotificationFrequency;
import com.marchnetworks.command.common.timezones.TimezonesDictionary;

import java.util.List;
import java.util.TimeZone;

public class EmailContentProvider
{
	private static final String LOGO_IMAGE_FILE = "/logo.jpg";
	private static final String TEMPLATE_FILE = "/emailNotificationTemplate.html";
	private static final String template = CommonAppUtils.readFileToStringInBundle( EmailContentProvider.class, "/emailNotificationTemplate.html", "UTF-8" );
	private static final byte[] logo = CommonAppUtils.readFileToByteArray( EmailContentProvider.class, "/logo.jpg" );

	public static NotificationContent getEmailContent( EmailContentSpecification specification, String recipient )
	{
		Notification notification = specification.getNotification();
		NotificationFrequency frequency = notification.getFrequency();
		long endTime = specification.getEndTime();
		long startTime = specification.getStartTime();

		TimeZone notificationTimeZone = TimezonesDictionary.fromWindowToTimeZone( notification.getTimeZone() );

		String from = DateUtils.getDateStringFromMillis( startTime, notificationTimeZone );
		String to = DateUtils.getDateStringFromMillis( endTime, notificationTimeZone );
		String timezone = notification.getTimeZone();

		FastStringTemplate tmpMessage = new FastStringTemplate( template );
		List<List<String>> tableData = specification.getTableData();
		String frequencyDisplayName = frequency.getDisplayName();
		String reportName = specification.getReportName();

		tmpMessage.replace( "${reportFrequency}", frequencyDisplayName ).replace( "${reportType}", reportName ).replace( "${recipient}", recipient ).replace( "${fromTime}", from + " (" + timezone + ")" ).replace( "${toTime}", to + " (" + timezone + ")" );

		if ( ( tableData == null ) || ( tableData.isEmpty() ) )
		{
			String msg = "<p>There were no " + reportName + " triggered " + notification.getFrequency().getDisplayPeriod() + "</p>";
			tmpMessage.replace( "${message}", msg );
			tmpMessage.removeString( "<div class=\"container\">", "</div>" );
		}
		else
		{
			String header = getTableHeader( tableData, reportName, notification );
			String content = getTableContent( tableData, reportName, notification );

			tmpMessage.replace( "${header}", header ).replace( "${content}", content ).replace( "${message}", "" );
		}

		NotificationContent result = new NotificationContent();

		String subject = "March Networks " + specification.getProduct() + " - " + frequencyDisplayName + " " + reportName + " Report";
		result.setSubject( subject );
		result.setMessage( tmpMessage.toString() );
		result.addInlines( "logo", logo );

		return result;
	}

	private static String getTableHeader( List<List<String>> tableData, String reportName, Notification notification )
	{
		StringBuilder sb = new StringBuilder();
		List<String> headers = ( List ) tableData.get( 0 );

		for ( int i = 0; i < headers.size(); i++ )
		{
			sb.append( "<th>" + ( String ) headers.get( i ) + "</th>" );
		}

		return sb.toString();
	}

	private static String getTableContent( List<List<String>> tableData, String reportName, Notification notification )
	{
		StringBuilder sb = new StringBuilder();

		for ( int i = 1; i < tableData.size(); i++ )
		{
			List<String> row = ( List ) tableData.get( i );

			if ( i % 2 == 0 )
			{
				sb.append( "<tr class=\"oddrowcolor\">" );
			}
			else
			{
				sb.append( "<tr class=\"evenrowcolor\">" );
			}
			for ( String cell : row )
			{
				if ( cell != null )
				{
					sb.append( "<td>" + cell + "</td>" );
				}
				else
				{
					sb.append( "<td></td>" );
				}
			}
			sb.append( "</tr>" );
		}
		return sb.toString();
	}
}
