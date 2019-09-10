package com.marchnetworks.management.topology.model;

import com.marchnetworks.alarm.service.AlarmService;
import com.marchnetworks.command.api.security.DeviceSessionException;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.health.service.HealthServiceIF;
import com.marchnetworks.license.LicenseService;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.model.CompositeDeviceMBean;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;
import com.marchnetworks.security.device.DeviceSessionHolderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceResourceFactory extends AbstractResourceFactory
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceResourceFactory.class );

	private DeviceRegistry deviceRegistry;

	private DeviceService deviceService;

	private HealthServiceIF healthService;

	private AlarmService alarmService;

	private LicenseService licenseService;
	private DeviceSessionHolderService deviceSessionHolderService;

	public ResourceEntity newResource( Resource resourceData ) throws TopologyException
	{
		DeviceResourceEntity resource = new DeviceResourceEntity( ( DeviceResource ) resourceData );

		String deviceId = ( ( DeviceResource ) resourceData ).getDeviceId();

		if ( deviceId != null )
		{
			DeviceMBean device = deviceRegistry.getDevice( deviceId );
			if ( device == null )
				throw new TopologyException( TopologyExceptionTypeEnum.DEVICE_NOT_FOUND );
			resource.setDevice( device );
			if ( CommonAppUtils.isNullOrEmptyString( resource.getName() ) )
			{
				resource.setName( device.getName() );
			}
		}
		else
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Null device id." );
		}
		return resource;
	}

	public void onRemove( Resource resource ) throws TopologyException
	{
		DeviceResource deviceResource = ( DeviceResource ) resource;

		healthService.processDeviceUnregistered( deviceResource.getDeviceId() );
		alarmService.processDeviceUnregistered( deviceResource.getDeviceId() );
		getLicenseService().processDeviceUnregistered( deviceResource.getDeviceId() );

		if ( deviceResource.isRootDevice() )
		{
			try
			{
				getDeviceSessionHolderService().getSessionFromDevice( deviceResource.getDeviceView().getRegistrationAddress(), deviceResource.getDeviceId() );
			}
			catch ( DeviceSessionException e )
			{
				LOG.error( "Could not obtain session for device " + deviceResource.getDeviceId() + " on unregistration" );
			}
			deviceService.scheduleDeviceUnregistration( deviceResource );
		}
	}

	public void onCreateAssociation( ResourceEntity resource, ResourceEntity parentResource ) throws TopologyException
	{
		DeviceResourceEntity deviceResource = ( DeviceResourceEntity ) resource;
		if ( ( deviceResource.getDevice() instanceof CompositeDeviceMBean ) )
		{
			if ( !( parentResource instanceof GroupEntity ) )
			{
				throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Root device " + resource.getIdAsString() + " cannot be put under " + parentResource.getName() );
			}
		}
		else if ( !( parentResource instanceof DeviceResourceEntity ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Child device " + resource.getIdAsString() + " cannot be put under " + parentResource.getName() );
		}
	}

	public LicenseService getLicenseService()
	{
		if ( licenseService == null )
		{
			licenseService = ( ( LicenseService ) ApplicationContextSupport.getBean( "licenseService_internal" ) );
		}
		return licenseService;
	}

	public void setDeviceRegistry( DeviceRegistry deviceRegistry )
	{
		this.deviceRegistry = deviceRegistry;
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}

	public void setHealthService( HealthServiceIF healthService )
	{
		this.healthService = healthService;
	}

	public void setAlarmService( AlarmService alarmService )
	{
		this.alarmService = alarmService;
	}

	public DeviceSessionHolderService getDeviceSessionHolderService()
	{
		if ( deviceSessionHolderService == null )
		{
			deviceSessionHolderService = ( ( DeviceSessionHolderService ) ApplicationContextSupport.getBean( "deviceSessionHolderService" ) );
		}
		return deviceSessionHolderService;
	}
}

