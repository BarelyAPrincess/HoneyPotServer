package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.transport.data.TimeZoneInfo;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.management.instrumentation.model.CompositeDeviceMBean;
import com.marchnetworks.management.instrumentation.model.Device;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table( name = "DEVICE_RESOURCE" )
public class DeviceResourceEntity extends ResourceEntity
{
	@OneToOne( cascade = {javax.persistence.CascadeType.DETACH}, targetEntity = Device.class )
	@JoinColumn( name = "DEVICE", nullable = false, unique = true )
	private DeviceMBean device;

	public DeviceResourceEntity()
	{
	}

	public DeviceResourceEntity( DeviceResource dataObject )
	{
		super( dataObject );
	}

	public DeviceMBean getDevice()
	{
		return device;
	}

	public void setDevice( DeviceMBean device )
	{
		this.device = device;
	}

	protected Resource newDataObject()
	{
		DeviceResource ret = new DeviceResource();

		getDevice();

		DeviceView deviceView = new DeviceView();

		deviceView.setRegistrationAddress( device.getAddress() );
		deviceView.setDeviceId( device.getDeviceId() );
		deviceView.setMacAddresses( device.getMacAddresses() );
		deviceView.setManufacturer( device.getManufacturer() );
		deviceView.setManufacturerName( device.getManufacturerName() );
		deviceView.setModel( device.getModel() );
		deviceView.setModelName( device.getModelName() );
		deviceView.setSubModel( device.getSubModel() );
		deviceView.setNetworkNames( device.getNames() );
		deviceView.setIpAddresses( device.getIpAddresses() );
		deviceView.setParentDeviceId( device.getParentDeviceId() );
		deviceView.setRegistrationStatus( device.getRegistrationStatus() );
		deviceView.setSerial( device.getSerial() );
		deviceView.setSoftwareVersion( device.getSoftwareVersion() );
		deviceView.setFamily( device.getFamily() );
		deviceView.setFamilyName( device.getFamilyName() );
		deviceView.setConnectState( device.getConnectState() );
		deviceView.setChannelsInUse( device.getChannelsInUse() );
		deviceView.setChannelsMax( device.getChannelsMax() );
		deviceView.setConfigurationUrl( device.getConfigurationUrl() );
		deviceView.setRegistrationError( device.getRegistrationErrorMessage() );
		deviceView.setPatchList( device.getPatchList() );

		if ( ( device instanceof CompositeDeviceMBean ) )
		{
			CompositeDeviceMBean rootDevice = ( CompositeDeviceMBean ) device;

			TimeZoneInfo timeZoneInfo = ( TimeZoneInfo ) CoreJsonSerializer.fromJson( rootDevice.getTimeZoneInfo(), TimeZoneInfo.class );
			deviceView.setTimezoneInfo( timeZoneInfo );
			deviceView.setTimeDelta( rootDevice.getTimeDelta() );

			deviceView.setLocalAddressZone( rootDevice.getLocalZone() );
			deviceView.setUserAddressZones( rootDevice.getUserAddressZones() );
			deviceView.setEventSubscriptionId( rootDevice.getDeviceEventSubscriptionId() );
			deviceView.setDeviceEventSubscriptionPrefixes( rootDevice.getEventSubscriptionPrefixes() );
			deviceView.setAssignedDeviceId( rootDevice.getAssignedDeviceId() );
			if ( rootDevice.getLastCommunicationTimeInMillis() != null )
			{
				deviceView.setLastCommunicationTime( rootDevice.getLastCommunicationTimeInMillis().longValue() );
			}
			if ( rootDevice.getStationId() != null )
			{
				deviceView.setStationId( rootDevice.getStationId() );
			}
			deviceView.setDeviceEventSequenceId( rootDevice.getDeviceEventSequenceId() );
			deviceView.setGlobalSettings( rootDevice.getGlobalSettings() );

			deviceView.setCapabilities( rootDevice.getCapabilities() );
			deviceView.setDeviceCreationTime( device.getTimeCreatedInMillis() );
			deviceView.setNotifyInterval( rootDevice.getNotifyInterval() );
		}

		ret.setDeviceView( deviceView );
		ret.setDeviceId( device.getDeviceId() );

		return ret;
	}

	public Class<DeviceResource> getDataObjectClass()
	{
		return DeviceResource.class;
	}

	public void updateDeviceResourceName()
	{
		getDevice();

		if ( ( name == null ) || ( !( device instanceof CompositeDeviceMBean ) ) )
		{
			name = device.getName();
		}
	}
}

