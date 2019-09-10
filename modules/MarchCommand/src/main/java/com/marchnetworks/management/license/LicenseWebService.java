package com.marchnetworks.management.license;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.license.LicenseService;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;
import com.marchnetworks.license.model.DeviceLicenseInfo;
import com.marchnetworks.license.model.License;
import com.marchnetworks.license.model.LicenseType;
import com.marchnetworks.license.model.ServerLicenseInfo;

import java.util.Collection;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.springframework.security.access.AccessDeniedException;

@WebService( serviceName = "LicenseService", name = "LicenseService", portName = "LicensePort" )
public class LicenseWebService
{
	private String accessDenied = "not_authorized";

	private LicenseService licenseService = ( LicenseService ) ApplicationContextSupport.getBean( "licenseServiceProxy" );
	@Resource
	WebServiceContext wsContext;

	@WebMethod( operationName = "importLicense" )
	public void importLicense( String licenseXml ) throws LicenseException
	{
		try
		{
			licenseService.importLicense( licenseXml );
		}
		catch ( AccessDeniedException e )
		{
			throw new LicenseException( accessDenied, LicenseExceptionType.NOT_AUTHORIZED );
		}
	}

	@WebMethod( operationName = "getAllServerLicense" )
	public Collection<ServerLicenseInfo> getAllServerLicense() throws LicenseException
	{
		try
		{
			return licenseService.getAllLicenseInfo();
		}
		catch ( AccessDeniedException e )
		{
			throw new LicenseException( accessDenied, LicenseExceptionType.NOT_AUTHORIZED );
		}
	}

	@WebMethod( operationName = "getAllDeviceLicense" )
	public Collection<DeviceLicenseInfo> getAllDeviceLicense( LicenseType t ) throws LicenseException
	{
		try
		{
			return licenseService.getAllDeviceLicense( t );
		}
		catch ( AccessDeniedException e )
		{
			throw new LicenseException( accessDenied, LicenseExceptionType.NOT_AUTHORIZED );
		}
	}

	@WebMethod( operationName = "setDeviceLicense" )
	public void setLicenseToDevice( Long deviceId, LicenseType t, int count ) throws LicenseException
	{
		try
		{
			licenseService.setDeviceLicense( deviceId, t, count );
		}
		catch ( AccessDeniedException e )
		{
			throw new LicenseException( accessDenied, LicenseExceptionType.NOT_AUTHORIZED );
		}
	}

	@WebMethod( operationName = "getLicense" )
	public License getLicense( @WebParam( name = "licenseId" ) String licenseId ) throws LicenseException
	{
		try
		{
			return licenseService.getLicense( licenseId );
		}
		catch ( AccessDeniedException e )
		{
			throw new LicenseException( accessDenied, LicenseExceptionType.NOT_AUTHORIZED );
		}
	}

	@WebMethod( operationName = "getLicenses" )
	public License[] getLicenses() throws LicenseException
	{
		try
		{
			return licenseService.getLicenses();
		}
		catch ( AccessDeniedException e )
		{
			throw new LicenseException( accessDenied, LicenseExceptionType.NOT_AUTHORIZED );
		}
	}

	@WebMethod( operationName = "removeLicense" )
	public void removeLicense( @WebParam( name = "licenseId" ) String licenseId ) throws LicenseException
	{
		try
		{
			licenseService.removeLicense( licenseId );
		}
		catch ( AccessDeniedException e )
		{
			throw new LicenseException( accessDenied, LicenseExceptionType.NOT_AUTHORIZED );
		}
	}

	@WebMethod( operationName = "setLicenseResources" )
	public void setLicenseResources( @WebParam( name = "licenseId" ) String licenseId, @WebParam( name = "resources" ) Long[] resources ) throws LicenseException
	{
		try
		{
			licenseService.setLicenseResources( licenseId, resources );
		}
		catch ( AccessDeniedException e )
		{
			throw new LicenseException( accessDenied, LicenseExceptionType.NOT_AUTHORIZED );
		}
	}

	@WebMethod( operationName = "getHashedServerId" )
	public String getHashedServerId() throws LicenseException
	{
		try
		{
			return licenseService.getHashedServerId();
		}
		catch ( AccessDeniedException e )
		{
			throw new LicenseException( accessDenied, LicenseExceptionType.NOT_AUTHORIZED );
		}
		catch ( Exception e )
		{
			throw new LicenseException( e.getMessage(), LicenseExceptionType.LICENSE_SERVERID_NOT_FOUND );
		}
	}
}
