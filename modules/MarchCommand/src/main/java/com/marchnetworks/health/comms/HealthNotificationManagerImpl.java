package com.marchnetworks.health.comms;

import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.api.notification.EmailContentProvider;
import com.marchnetworks.command.api.notification.EmailContentSpecification;
import com.marchnetworks.command.api.notification.NotificationCoreService;
import com.marchnetworks.command.api.provider.ContentProvider;
import com.marchnetworks.command.common.DateUtils;
import com.marchnetworks.command.common.notification.data.Notification;
import com.marchnetworks.command.common.notification.data.NotificationContent;
import com.marchnetworks.command.common.timezones.TimezonesDictionary;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.health.data.HealthNotificationSummaryData;
import com.marchnetworks.health.service.HealthServiceIF;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class HealthNotificationManagerImpl implements ContentProvider<List<NotificationContent>, Notification>, InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( HealthNotificationManagerImpl.class );
	private static final String HEALTH_GROUP = "HEALTH";
	private static final String dateFormat = "dd/MM/yyyy HH:mm:ss";
	private static final List<String> headerRow = Arrays.asList( new String[] {"Device Name", "Total Issues", "Drives", "Unit", "Network", "Video", "Power", "Peripheral", "First Occurrence", "Last Occurrence"} );

	private HealthServiceIF healthService;

	private NotificationCoreService notificationService;

	public void onAppInitialized()
	{
		notificationService.setContentProvider( "HEALTH", null, this );
	}

	public List<NotificationContent> getContent( Notification notification )
	{
		List<NotificationContent> result = new ArrayList();

		List<String> arrRecipients = notification.getRecipients();

		EmailContentSpecification specification = new EmailContentSpecification();
		specification.setReportName( "System Health" );
		specification.setProduct( "Command" );
		specification.setNotification( notification );

		long endTime = DateUtils.currentTimeMillisRounded( 60000L );
		long startTime = notification.getNotificationStartTime( endTime );
		TimeZone tz = TimezonesDictionary.fromWindowToTimeZone( notification.getTimeZone() );
		specification.setStartTime( startTime );
		specification.setEndTime( endTime );

		for ( String recipient : arrRecipients )
		{
			HealthNotificationSummaryData[] summaryData = getHealthService().getAllAlertsByUser( recipient, startTime, endTime );

			if ( summaryData == null )
			{
				LOG.debug( String.format( "There were no alerts for user %s", new Object[] {recipient} ) );
			}
			else
			{
				List<List<String>> tableData = new ArrayList();
				tableData.add( headerRow );

				for ( HealthNotificationSummaryData data : summaryData )
				{
					List<String> row = new ArrayList();
					row.add( data.getDeviceName() );
					row.add( String.valueOf( data.getTotalIssues() ) );
					row.add( String.valueOf( data.getDriveIssues() ) );
					row.add( String.valueOf( data.getUnitIssues() ) );
					row.add( String.valueOf( data.getNetworkIssue() ) );
					row.add( String.valueOf( data.getVideoIssue() ) );
					row.add( String.valueOf( data.getPowerIssue() ) );
					row.add( String.valueOf( data.getPeripheralIssue() ) );
					row.add( DateUtils.getDateStringFromMillis( data.getFirstOccurence(), tz, "dd/MM/yyyy HH:mm:ss" ) );
					row.add( DateUtils.getDateStringFromMillis( data.getLastOccurence(), tz, "dd/MM/yyyy HH:mm:ss" ) );
					tableData.add( row );
				}
				specification.setTableData( tableData );

				NotificationContent content = EmailContentProvider.getEmailContent( specification, recipient );
				content.setRecipients( Collections.singletonList( recipient ) );
				result.add( content );
			}
		}
		return result;
	}

	private HealthServiceIF getHealthService()
	{
		if ( healthService == null )
		{
			healthService = ( ( HealthServiceIF ) ApplicationContextSupport.getBean( "healthServiceProxy_internal" ) );
		}

		return healthService;
	}

	public void setNotificationService( NotificationCoreService notificationService )
	{
		this.notificationService = notificationService;
	}
}
