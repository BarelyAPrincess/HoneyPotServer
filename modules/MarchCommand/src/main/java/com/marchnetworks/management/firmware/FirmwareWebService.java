package com.marchnetworks.management.firmware;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.firmware.data.Firmware;
import com.marchnetworks.management.firmware.data.FirmwareGroupEnum;
import com.marchnetworks.management.firmware.data.GroupFirmware;
import com.marchnetworks.management.firmware.service.FirmwareException;
import com.marchnetworks.management.firmware.service.FirmwareService;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.springframework.security.access.AccessDeniedException;

@WebService( serviceName = "FirmwareService", name = "FirmwareService", portName = "FirmwarePort" )
public class FirmwareWebService
{
	private FirmwareService firmwareService = ( FirmwareService ) ApplicationContextSupport.getBean( "firmwareServiceProxy" );
	private String accessDenied = "not_authorized";

	@WebMethod( operationName = "setDeviceFirmwares" )
	public void setDeviceFirmwares( @WebParam( name = "firmwares" ) Firmware[] firmwares ) throws FirmwareException
	{
		try
		{
			firmwareService.setDeviceFirmwares( firmwares );
		}
		catch ( AccessDeniedException e )
		{
			throw new FirmwareException( accessDenied );
		}
	}

	@WebMethod( operationName = "getDeviceFirmwares" )
	public Firmware[] getDeviceFirmwares() throws FirmwareException
	{
		try
		{
			return ConversionService.convertUpgradeStates( firmwareService.findAllDeviceFirmwares() );
		}
		catch ( AccessDeniedException e )
		{
			throw new FirmwareException( accessDenied );
		}
	}

	@WebMethod( operationName = "getDeviceFirmware" )
	public Firmware getDeviceFirmware( @WebParam( name = "deviceId" ) String deviceId ) throws FirmwareException
	{
		try
		{
			return ConversionService.convertUpgradeState( firmwareService.getDeviceFirmware( deviceId ) );
		}
		catch ( AccessDeniedException e )
		{
			throw new FirmwareException( accessDenied );
		}
	}

	@WebMethod( operationName = "setGroupFirmwares" )
	public void setGroupFirmwares( @WebParam( name = "firmwares" ) GroupFirmware[] groupFirmwares ) throws FirmwareException
	{
		try
		{
			firmwareService.setGroupFirmwares( groupFirmwares );
		}
		catch ( AccessDeniedException e )
		{
			throw new FirmwareException( accessDenied );
		}
	}

	@WebMethod( operationName = "getGroupFirmwares" )
	public GroupFirmware[] getGroupFirmwares() throws FirmwareException
	{
		try
		{
			return firmwareService.findAllGroupFirmwares();
		}
		catch ( AccessDeniedException e )
		{
			throw new FirmwareException( accessDenied );
		}
	}

	@WebMethod( operationName = "getGroupFirmware" )
	public GroupFirmware getGroupFirmware( @WebParam( name = "groupId" ) FirmwareGroupEnum groupId ) throws FirmwareException
	{
		try
		{
			return firmwareService.getGroupFirmware( groupId );
		}
		catch ( AccessDeniedException e )
		{
			throw new FirmwareException( accessDenied );
		}
	}

	@WebMethod( operationName = "isFileAssociated" )
	public boolean isFileAssociated( @WebParam( name = "fileStorageId" ) String fileStorageId ) throws FirmwareException
	{
		try
		{
			return firmwareService.isFileAssociated( fileStorageId );
		}
		catch ( AccessDeniedException e )
		{
			throw new FirmwareException( accessDenied );
		}
	}
}
