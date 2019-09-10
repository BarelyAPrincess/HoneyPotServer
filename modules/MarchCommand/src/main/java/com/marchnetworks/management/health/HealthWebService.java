package com.marchnetworks.management.health;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.types.AlertDeviceStateFilterEnum;
import com.marchnetworks.common.types.AlertSeverityEnum;
import com.marchnetworks.common.types.AlertUserStateEnum;
import com.marchnetworks.common.types.HistoricalAlertSearchTimeFieldEnum;
import com.marchnetworks.health.data.AlertData;
import com.marchnetworks.health.data.AlertThresholdData;
import com.marchnetworks.health.data.DefaultAlertThresholdData;
import com.marchnetworks.health.data.DeletedDeviceAlertData;
import com.marchnetworks.health.data.DeviceAlertData;
import com.marchnetworks.health.data.ServerAlertData;
import com.marchnetworks.health.search.AlertSearchQuery;
import com.marchnetworks.health.search.AlertSearchResults;
import com.marchnetworks.health.service.HealthServiceIF;
import com.marchnetworks.server.event.health.HealthFault;
import com.marchnetworks.server.event.health.HealthFaultTypeEnum;

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

@WebService( serviceName = "HealthService", name = "HealthService", portName = "HealthPort" )
@XmlSeeAlso( {AlertSeverityEnum.class, AlertDeviceStateFilterEnum.class, AlertSearchQuery.class, HistoricalAlertSearchTimeFieldEnum.class, DeviceAlertData.class, DeletedDeviceAlertData.class, ServerAlertData.class} )
public class HealthWebService
{
	private String accessDenied = "not_authorized";

	private HealthServiceIF healthService = ( HealthServiceIF ) ApplicationContextSupport.getBean( "healthServiceProxy" );

	@Resource
	WebServiceContext wsContext;

	@WebMethod( operationName = "getOpenAlerts" )
	public AlertData[] getOpenAlerts() throws HealthFault
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		try
		{
			return healthService.getOpenAlerts( username );
		}
		catch ( AccessDeniedException e )
		{
			throw new HealthFault( HealthFaultTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "getAlertThresholds" )
	public AlertThresholdData[] getAlertThreshods() throws HealthFault
	{
		try
		{
			return healthService.getAlertThresholds();
		}
		catch ( AccessDeniedException e )
		{
			throw new HealthFault( HealthFaultTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "getDefaultAlertThresholds" )
	public DefaultAlertThresholdData[] getDefaultAlertThresholds() throws HealthFault
	{
		try
		{
			return healthService.getDefaultAlertThresholds();
		}
		catch ( AccessDeniedException e )
		{
			throw new HealthFault( HealthFaultTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "setAlertThresholds" )
	public void setAlertThresholds( AlertThresholdData[] alertThresholds ) throws HealthFault
	{
		try
		{
			healthService.setAlertThresholds( alertThresholds );
			return;
		}
		catch ( AccessDeniedException e )
		{
			throw new HealthFault( HealthFaultTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "getAlertById" )
	public AlertData getAlertById( @WebParam( name = "id" ) long id ) throws HealthFault
	{
		try
		{
			return healthService.getAlertById( id );
		}
		catch ( AccessDeniedException e )
		{
			throw new HealthFault( HealthFaultTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "setUserState" )
	public void setUserState( @WebParam( name = "ID" ) long id, @WebParam( name = "userState" ) AlertUserStateEnum userState ) throws HealthFault
	{
		try
		{
			healthService.setUserState( id, userState );
		}
		catch ( AccessDeniedException e )
		{
			throw new HealthFault( HealthFaultTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "setUserStates" )
	public void setUserStates( @WebParam( name = "alertIds" ) long[] alertIds, @WebParam( name = "userState" ) AlertUserStateEnum userState ) throws HealthFault
	{
		try
		{
			healthService.setUserStates( alertIds, userState );
		}
		catch ( AccessDeniedException e )
		{
			throw new HealthFault( HealthFaultTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "searchHistoricalClosedAlerts" )
	public AlertSearchResults searchHistoricalClosedAlerts( @WebParam( name = "searchQuery" ) AlertSearchQuery searchQuery ) throws HealthFault
	{
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		try
		{
			return healthService.searchHistoricalClosedAlerts( username, searchQuery );
		}
		catch ( AccessDeniedException e )
		{
			throw new HealthFault( HealthFaultTypeEnum.SECURITY, accessDenied );
		}
	}
}
