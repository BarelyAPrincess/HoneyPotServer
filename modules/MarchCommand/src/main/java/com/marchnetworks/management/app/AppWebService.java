package com.marchnetworks.management.app;

import com.marchnetworks.app.data.App;
import com.marchnetworks.app.service.AppManager;
import com.marchnetworks.command.common.app.AppException;
import com.marchnetworks.command.common.app.AppExceptionTypeEnum;
import com.marchnetworks.common.spring.ApplicationContextSupport;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.springframework.security.access.AccessDeniedException;

@WebService( serviceName = "AppService", name = "AppService", portName = "AppPort" )
public class AppWebService
{
	private String accessDenied = "not_authorized";

	private AppManager appManager = ( AppManager ) ApplicationContextSupport.getBean( "appManagerProxy" );

	@WebMethod( operationName = "getApps" )
	public App[] getApps() throws AppException
	{
		try
		{
			return appManager.getApps();
		}
		catch ( AccessDeniedException e )
		{
			throw new AppException( AppExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "getAllApps" )
	public App[] getAllApps() throws AppException
	{
		try
		{
			return appManager.getAllApps();
		}
		catch ( AccessDeniedException e )
		{
			throw new AppException( AppExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "getApp" )
	public App getApp( @WebParam( name = "appID" ) String appID ) throws AppException
	{
		try
		{
			return appManager.getApp( appID );
		}
		catch ( AccessDeniedException e )
		{
			throw new AppException( AppExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "start" )
	public void start( @WebParam( name = "appID" ) String appID ) throws AppException
	{
		try
		{
			appManager.start( appID );
		}
		catch ( AccessDeniedException e )
		{
			throw new AppException( AppExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "restart" )
	public void restart( @WebParam( name = "appID" ) String appID ) throws AppException
	{
		try
		{
			appManager.restart( appID );
		}
		catch ( AccessDeniedException e )
		{
			throw new AppException( AppExceptionTypeEnum.SECURITY, accessDenied );
		}
	}

	@WebMethod( operationName = "uninstall" )
	public void uninstall( @WebParam( name = "appID" ) String appID ) throws AppException
	{
		try
		{
			appManager.uninstall( appID );
		}
		catch ( AccessDeniedException e )
		{
			throw new AppException( AppExceptionTypeEnum.SECURITY, accessDenied );
		}
	}
}
