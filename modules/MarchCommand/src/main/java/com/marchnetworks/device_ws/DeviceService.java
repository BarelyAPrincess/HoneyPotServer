package com.marchnetworks.device_ws;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

@WebServiceClient( name = "DeviceService", targetNamespace = "http://marchnetworks.com/device_ws/", wsdlLocation = "https://localhost/device_ws/" )
public class DeviceService extends Service
{
	private static final URL DEVICESERVICE_WSDL_LOCATION;
	private static final WebServiceException DEVICESERVICE_EXCEPTION;
	private static final QName DEVICESERVICE_QNAME = new QName( "http://marchnetworks.com/device_ws/", "DeviceService" );

	static
	{
		URL url = null;
		WebServiceException e = null;
		try
		{
			url = new URL( "https://localhost/device_ws/" );
		}
		catch ( MalformedURLException ex )
		{
			e = new WebServiceException( ex );
		}
		DEVICESERVICE_WSDL_LOCATION = url;
		DEVICESERVICE_EXCEPTION = e;
	}

	public DeviceService()
	{
		super( __getWsdlLocation(), DEVICESERVICE_QNAME );
	}

	public DeviceService( WebServiceFeature... features )
	{
		super( __getWsdlLocation(), DEVICESERVICE_QNAME, features );
	}

	public DeviceService( URL wsdlLocation )
	{
		super( wsdlLocation, DEVICESERVICE_QNAME );
	}

	public DeviceService( URL wsdlLocation, WebServiceFeature... features )
	{
		super( wsdlLocation, DEVICESERVICE_QNAME, features );
	}

	public DeviceService( URL wsdlLocation, QName serviceName )
	{
		super( wsdlLocation, serviceName );
	}

	public DeviceService( URL wsdlLocation, QName serviceName, WebServiceFeature... features )
	{
		super( wsdlLocation, serviceName, features );
	}

	@WebEndpoint( name = "DeviceServiceSoap" )
	public DeviceServiceSoap getDeviceServiceSoap()
	{
		return ( DeviceServiceSoap ) super.getPort( new QName( "http://marchnetworks.com/device_ws/", "DeviceServiceSoap" ), DeviceServiceSoap.class );
	}

	@WebEndpoint( name = "DeviceServiceSoap" )
	public DeviceServiceSoap getDeviceServiceSoap( WebServiceFeature... features )
	{
		return ( DeviceServiceSoap ) super.getPort( new QName( "http://marchnetworks.com/device_ws/", "DeviceServiceSoap" ), DeviceServiceSoap.class, features );
	}

	private static URL __getWsdlLocation()
	{
		if ( DEVICESERVICE_EXCEPTION != null )
		{
			throw DEVICESERVICE_EXCEPTION;
		}
		return DEVICESERVICE_WSDL_LOCATION;
	}
}
