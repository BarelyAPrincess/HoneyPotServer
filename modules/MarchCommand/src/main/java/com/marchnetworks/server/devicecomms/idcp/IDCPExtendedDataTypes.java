package com.marchnetworks.server.devicecomms.idcp;

import java.util.HashMap;
import java.util.Map;

enum IDCPExtendedDataTypes
{
	IDCP_EXTTYPE_MODEL( 3 ),
	IDCP_EXTTYPE_VERSION( 0 ),
	IDCP_EXTTYPE_NAME_EXT_FRIENDLY( 8193 ),
	IDCP_EXTTYPE_NETCFG( 2 ),
	IDCP_EXTTYPE_NAME( 1 ),
	IDCP_EXTTYPE_NETCFG_EXT( 4098 ),
	IDCP_EXTTYPE_NETCFG_EXT_IP_LIST( 8194 );

	private static final Map<Integer, IDCPExtendedDataTypes> integerToEnum = new HashMap<>();

	static
	{
		for ( IDCPExtendedDataTypes idcpExtendedDataTypes : values() )
			integerToEnum.put( Integer.valueOf( idcpExtendedDataTypes.getTypeValue() ), idcpExtendedDataTypes );
	}

	public static IDCPExtendedDataTypes fromTypeValue( int typeValue )
	{
		return ( IDCPExtendedDataTypes ) integerToEnum.get( Integer.valueOf( typeValue ) );
	}
	private final int typeValue;

	private IDCPExtendedDataTypes( int typeValue )
	{
		this.typeValue = typeValue;
	}

	public int getTypeValue()
	{
		return typeValue;
	}
}

