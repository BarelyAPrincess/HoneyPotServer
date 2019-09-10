package com.marchnetworks.server.devicecomms.idcp;

import java.util.Map;

public abstract interface IDCPClient
{
	public static final String MODEL_FIELD_INFO = "modelInfo";
	public static final String MODEL_NAME_FIELD_INFO = "modelNameInfo";
	public static final String SUBMODEL_FIELD_INFO = "submodelInfo";
	public static final String SUBMODEL_NAME_FIELD_INFO = "submodelNameInfo";
	public static final String NAME_FIELD_INFO = "nameInfo";
	public static final String NET_IP_ADDRESS_FIELD_INFO = "netIPAddressInfo";
	public static final String NET_MAC_ADDRESS_FIELD_INFO = "netMacAddressInfo";
	public static final String VERSION_FIELD_INFO = "versionInfo";
	public static final String DEFAULT_MARCH_NETWORKS_MANUFACTURER = "March Networks";

	public abstract Map<String, IDCPDeviceInfo> discoverDevices();

	public abstract void setIdcpPortNumber( int paramInt );

	public abstract void setIdcpBroadcastAddress( String paramString );

	public abstract void setReceiveTimeout( int paramInt );
}

