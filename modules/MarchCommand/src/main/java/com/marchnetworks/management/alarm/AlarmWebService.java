package com.marchnetworks.management.alarm;

import com.marchnetworks.alarm.alarmdetails.AlarmDetailEnum;
import com.marchnetworks.alarm.data.AlarmEntryView;
import com.marchnetworks.alarm.data.DeletedSourceAlarmEntry;
import com.marchnetworks.alarm.service.AlarmEntryCloseRecord;
import com.marchnetworks.alarm.service.AlarmException;
import com.marchnetworks.alarm.service.AlarmExceptionTypeEnum;
import com.marchnetworks.alarm.service.AlarmService;
import com.marchnetworks.common.device.DeletedDeviceData;
import com.marchnetworks.common.spring.ApplicationContextSupport;

import java.util.Set;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.WebServiceContext;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@WebService( serviceName = "AlarmService", name = "AlarmService", portName = "AlarmPort" )
@XmlSeeAlso( {DeletedSourceAlarmEntry.class, DeletedDeviceData.class} )
public class AlarmWebService
{
	private String accessDenied = "not_authorized";

	private AlarmService alarmService = ( AlarmService ) ApplicationContextSupport.getBean( "alarmServiceProxy" );

	@Resource
	WebServiceContext wsContext;

	@WebMethod( operationName = "queryAlarmEntries" )
	public AlarmEntryView[] queryAlarmEntries( @WebParam( name = "alarmSourceIDs" ) String[] alarmSourceIDs, @WebParam( name = "includeOpenEntries" ) boolean includeOpenEntries, @WebParam( name = "includeClosedEntries" ) boolean includeClosedEntries, @WebParam( name = "startTime" ) long startTime, @WebParam( name = "endTime" ) long endTime, @WebParam( name = "maxEntries" ) int maxEntries ) throws AlarmException
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		try
		{
			checkFeatureIsEnabled();
			return alarmService.queryAlarmEntries( username, alarmSourceIDs, includeOpenEntries, includeClosedEntries, startTime, endTime, maxEntries );
		}
		catch ( AccessDeniedException e )
		{
			throw new AlarmException( AlarmExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "closeAlarmEntries" )
	public void closeAlarmEntries( @WebParam( name = "alarmClosures" ) AlarmEntryCloseRecord[] alarmClosures ) throws AlarmException
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		try
		{
			checkFeatureIsEnabled();
			alarmService.closeAlarmEntries( username, alarmClosures );
		}
		catch ( AccessDeniedException e )
		{
			throw new AlarmException( AlarmExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "setAlarmHandling" )
	public void setAlarmHandling( @WebParam( name = "alarmEntryIDs" ) String[] alarmEntryIDs, @WebParam( name = "handling" ) boolean handling ) throws AlarmException
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		try
		{
			checkFeatureIsEnabled();
			alarmService.setAlarmHandling( username, alarmEntryIDs, handling );
		}
		catch ( AccessDeniedException e )
		{
			throw new AlarmException( AlarmExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "updateAlarmEntryDetails" )
	public void updateAlarmEntryDetails( @WebParam( name = "alarmEntryId" ) String alarmEntryId, @WebParam( name = "alarmDetails" ) Set<AlarmDetailEnum> alarmDetails, @WebParam( name = "note" ) String note ) throws AlarmException
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		try
		{
			checkFeatureIsEnabled();
			alarmService.updateAlarmEntryDetails( username, alarmEntryId, alarmDetails, note );
		}
		catch ( AccessDeniedException e )
		{
			throw new AlarmException( AlarmExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	private void checkFeatureIsEnabled() throws AlarmException
	{
		if ( !alarmService.getAlarmsEnabled() )
		{
			throw new AlarmException( AlarmExceptionTypeEnum.FEATURE_IS_DISABLED );
		}
	}
}
