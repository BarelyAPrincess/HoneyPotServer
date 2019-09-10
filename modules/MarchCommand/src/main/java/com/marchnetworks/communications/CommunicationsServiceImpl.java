package com.marchnetworks.communications;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.communications.CommunicationsService;
import com.marchnetworks.management.communications.DeviceDiscoverView;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.devicecomms.idcp.IDCPClient;
import com.marchnetworks.server.devicecomms.idcp.IDCPDeviceInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommunicationsServiceImpl implements CommunicationsService
{
	private static final Logger LOG = LoggerFactory.getLogger( CommunicationsServiceImpl.class );

	private IDCPClient idcpClient;

	private ResourceTopologyServiceIF topologyService;

	public List<DeviceDiscoverView> discoverDevices( int responseTimeout )
	{
		List<DeviceDiscoverView> discoveredDeviceList = new ArrayList();

		idcpClient.setReceiveTimeout( responseTimeout );
		Map<String, IDCPDeviceInfo> deviceDiscoverData = idcpClient.discoverDevices();

		topologyService = getTopologyService();
		List<DeviceResource> devResources = topologyService.getAllDeviceResources();
		LOG.debug( "Number of registered devices: " + devResources.size() );

		for ( String deviceDataKey : deviceDiscoverData.keySet() )
		{
			IDCPDeviceInfo discoverData = ( IDCPDeviceInfo ) deviceDiscoverData.get( deviceDataKey );

			DeviceDiscoverView discoverView = new DeviceDiscoverView();
			discoverView.setDeviceFamilyId( ( String ) discoverData.getModelInfoMap().get( "modelInfo" ) );
			discoverView.setDeviceFamily( ( String ) discoverData.getModelInfoMap().get( "modelNameInfo" ) );
			discoverView.setDeviceModelId( ( String ) discoverData.getModelInfoMap().get( "submodelInfo" ) );
			discoverView.setDeviceModel( ( String ) discoverData.getModelInfoMap().get( "submodelNameInfo" ) );
			discoverView.setDeviceName( ( String ) discoverData.getNameInfoMap().get( "nameInfo" ) );
			discoverView.setDeviceMacAddress( ( String ) discoverData.getNetConfigInfoMap().get( "netMacAddressInfo" ) );
			discoverView.setDeviceIpAddress( ( String ) discoverData.getNetConfigInfoMap().get( "netIPAddressInfo" ) );
			discoverView.setDeviceSoftwareVersion( ( String ) discoverData.getVersionInfoMap().get( "versionInfo" ) );
			discoverView.setDeviceManufacturer( "March Networks" );
			discoverView.setExtendedNetworkConfiguration( discoverData.getExtendedNetConfigList() );
			discoverView.setIsManaged( matchMacAddress( devResources, discoverView.getDeviceMacAddress() ) );
			discoveredDeviceList.add( discoverView );
		}
		return discoveredDeviceList;
	}

	private boolean matchMacAddress( List<DeviceResource> devResources, String macAddress )
	{
		if ( ( devResources != null ) && ( devResources.size() > 0 ) && ( !CommonAppUtils.isNullOrEmptyString( macAddress ) ) )
		{
			for ( DeviceResource device : devResources )
			{
				String[] macAddressArr = device.getDeviceView().getMacAddresses();
				for ( String mac : macAddressArr )
				{
					if ( mac.equalsIgnoreCase( macAddress ) )
					{
						LOG.debug( "macAddress matches - registered" );
						return true;
					}
				}
			}
		}

		return false;
	}

	private ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" ) );
		}
		return topologyService;
	}

	public void setIdcpClient( IDCPClient idcpClient )
	{
		this.idcpClient = idcpClient;
	}
}
