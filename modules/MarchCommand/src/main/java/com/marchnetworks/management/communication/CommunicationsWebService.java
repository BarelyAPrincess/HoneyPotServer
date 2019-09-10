package com.marchnetworks.management.communication;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.communications.CommunicationsException;
import com.marchnetworks.management.communications.CommunicationsService;
import com.marchnetworks.management.communications.DeviceDiscoverView;
import com.marchnetworks.management.communications.NetworkConfiguration;

import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.WebServiceContext;

import org.springframework.security.access.AccessDeniedException;

@WebService( serviceName = "CommunicationsService", name = "CommunicationsService", portName = "CommunicationsPort" )
@XmlSeeAlso( {NetworkConfiguration.class} )
public class CommunicationsWebService
{
	private CommunicationsService communicationsService = ( CommunicationsService ) ApplicationContextSupport.getBean( "communicationsServiceProxy" );

	private String accessDenied = "not_authorized";

	@Resource
	WebServiceContext wsContext;

	@WebMethod( operationName = "discoverDevices" )
	public DeviceDiscoverView[] discoverDevices( @WebParam( name = "responseTimeout" ) int responseTimeout ) throws CommunicationsException
	{
		try
		{
			List<DeviceDiscoverView> discoveredDeviceList = communicationsService.discoverDevices( responseTimeout );
			return ( DeviceDiscoverView[] ) discoveredDeviceList.toArray( new DeviceDiscoverView[discoveredDeviceList.size()] );
		}
		catch ( AccessDeniedException e )
		{
			throw new CommunicationsException( accessDenied );
		}
	}
}
