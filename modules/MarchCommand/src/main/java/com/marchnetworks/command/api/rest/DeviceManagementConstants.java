package com.marchnetworks.command.api.rest;

import com.marchnetworks.command.common.CommonAppUtils;

public class DeviceManagementConstants
{
	public static final int DEFAULT_HTTP_CLIENT_SOCKET_CONNECTION_TIMEOUT = 5000;
	public static final int DEFAULT_HTTP_CLIENT_SOCKET_DATA_TIMEOUT = 15000;
	public static final int DEFAULT_RETRY_AFTER_PERIOD = 10000;
	public static final int DEFAULT_MAX_RETRY = 2;
	public static final int DEFAULT_DEVICE_SESSION_TIMEOUT = 300;
	public static final String DEVICE_ADMIN_KEY = "admin";
	public static final String DEVICE_ADMIN_PASSWORD_KEY = "adminPassword";
	public static final String DEVICE_USE_TRUSTED_COMMUNICATION_KEY = "useTrusted";
	public static final String DEVICE_USE_SAME_PROXY_INSTANCE_KEY = "useSameProxyInstance";
	public static final String DEVICE_ADDRESS_KEY = "deviceAdress";
	public static final String DEVICE_SESSION = "securityToken";
	public static final String SECURITY_TOKEN_SESSION_ID = "sessionId=";
	public static final String SECURITY_TOKEN_COOKIE_NAME = "sessionId";
	public static final String COOKIE_HEADER_NAME = "Cookie";
	public static final String CES_VERSION_HEADER = "X-Client-Version";
	public static final String NETTUNO_FAMILY_ID = "0";
	public static final String R4_FAMILY_ID = "256";
	public static final String R5_FAMILY_ID = "257";
	public static final String R5_MODEL_ID = "1";
	public static final String NVR_FAMILY_ID = "6";
	public static final String NVR_MODEL_ID = "1";
	public static final String VMS_FAMILY_ID = "4";
	public static final String EXTRACTOR_FAMILY_ID = "1001";
	public static final String MARCH_MANUFACTURER_ID = "1";
	public static final String DEVICE_SUBSCRIPTION_ERROR_MESSAGE = "not_found";
	public static final String DEVICE_HTTP_RETRY_AFTER_HEADER = "Retry-After";
	public static final String DEVICE_HTTP_KEEP_ALIVE_TIMEOUT = "keepAliveTimeout";
	public static final String DEFAULT_HTTP_KEEP_ALIVE_TIMEOUT = "300";
	public static final String DEVICE_HTTP_KEEP_ALIVE_HEADER = "X-Keep-Alive";
	public static final String DEVICE_SESSION_RENEWAL_ATTEMPTED = "attempted";
	public static final String DEVICE_REQUEST_REMOTE_ADDRESS = "deviceRemoteAddress";
	public static final String DEVICE_RESPONSE_COOKIE_HEADER = "Set-Cookie";
	public static final String DEVICE_FUNCTION_NOT_IMPLEMENTED = "not_implemented";
	public static final String DEVICE_OUTDATED_WSDL_VERSION = "method name or namespace not recognized";
	public static final String DEVICE_DEFAULT_PORT = "443";
	public static final String DEVICE_TIME_SYNC_HEADER = "x-sync-time";
	public static final String DEVICE_GLOBAL_SECURITY_TOKEN = "globalSecurityToken";
	public static final String IS_MASS_REGISTER = "isMassRegister";
	public static final String AUTO_DETECTED_DEVICE_ADDRESS = "autodetectedaddress";
	public static final String DEVICE_HTTP_DATE_HEADER = "Date";
	public static final String DEVICE_REQUEST_REGISTRATION_TIMESTAMP = "deviceTimestamp";
	public static final Long DEVICE_EVENT_MIN_SEQUENCE_ID = Long.valueOf( -1L );

	public static final Long DEVICE_EVENT_START_SEQUENCE_ID = Long.valueOf( -1L );

	public static final String R5_MODEL_VIDEOSPHERE = "1";

	public static final String R5_MODEL_8732R = "2";

	public static final String R5_MODEL_8532R = "3";

	public static final String R5_MODEL_8532S = "4";

	public static final String R5_MODEL_8516S_ = "5";

	public static final String R5_MODEL_8516R = "6";

	public static final String R5_MODEL_8708S = "7";

	public static final String R5_MODEL_8704S = "8";

	public static final String R5_MODEL_8508S = "9";

	public static final String R5_MODEL_GT08A = "10";

	public static final String R5_MODEL_GT08 = "11";

	public static final String R5_MODEL_GT12 = "12";

	public static final String R5_MODEL_GT16 = "13";

	public static final String R5_MODEL_GT20 = "14";

	public static final String R5_MODEL_MT04 = "15";

	public static final String R5_MODEL_MT08 = "16";
	public static final String R5_MODEL_RT20 = "17";
	public static final String R5_MODEL_8716P = "18";
	public static final String R5_MODEL_VNVR = "19";
	public static final String R5_MODEL_9132 = "21";
	public static final String R5_MODEL_9248 = "22";
	public static final String R5_MODEL_9264 = "23";
	public static final String R5_MODEL_MT06 = "24";
	public static final String R5_MODEL_MT20 = "25";
	public static final String R5_MODEL_RT04E = "26";
	public static final String R5_MODEL_RT06E = "27";
	public static final String R5_MODEL_RT08E = "28";
	public static final String R5_MODEL_RT20E = "29";
	public static final String R5_MODEL_SVR24 = "20";
	public static final String R5_MODEL_8724V = "31";
	public static final String R5_MODEL_RT20EP = "32";
	public static final String R4_MODEL_5308 = "2";
	public static final String R4_MODEL_5412 = "3";
	public static final String DEVICE_STATION_ID = "stationId";
	public static final String R5_MARK_FOR_REPLACEMENT_MINIMUM_VERSION = "5.7.10.0107";
	public static final String R4_MARK_FOR_REPLACEMENT_MINIMUM_VERSION = "4.9.5";
	public static final String THIRD_PARTY_IDENTIFIER = "thirdparty";
	public static final String COMMAND_CAMERA_MODEL_NAME = "Command Camera";
	public static final String X_REGISTRATION_STATUS = "x-registration-status";
	public static final String PLACEHOLDER_MAC_ADDRESS = "00:00:00:00:00:00";

	public static boolean isReplaceableDevice( String familyId )
	{
		return "257".equals( familyId );
	}

	public static boolean isR5_VideoSphere_Device( String familyId, String modelId )
	{
		return ( "257".equals( familyId ) ) && ( "1".equals( modelId ) );
	}

	public static boolean isR5_Gen6_Device( String familyId, String modelId )
	{
		if ( ( "257".equals( familyId ) ) && ( ( "21".equals( modelId ) ) || ( "22".equals( modelId ) ) || ( "23".equals( modelId ) ) ) )
		{

			return true;
		}
		return false;
	}

	public static boolean isR5_Gen5Fixed_Device( String familyId, String modelId )
	{
		if ( ( "257".equals( familyId ) ) && ( ( "2".equals( modelId ) ) || ( "3".equals( modelId ) ) || ( "4".equals( modelId ) ) || ( "5".equals( modelId ) ) || ( "6".equals( modelId ) ) || ( "7".equals( modelId ) ) || ( "8".equals( modelId ) ) || ( "9".equals( modelId ) ) || ( "18".equals( modelId ) ) || ( "19".equals( modelId ) ) ) )
		{

			return true;
		}
		return false;
	}

	public static boolean isR5_GT_Device( String familyId, String modelId )
	{
		if ( ( "257".equals( familyId ) ) && ( ( "10".equals( modelId ) ) || ( "11".equals( modelId ) ) || ( "12".equals( modelId ) ) || ( "13".equals( modelId ) ) || ( "14".equals( modelId ) ) ) )
		{

			return true;
		}
		return false;
	}

	public static boolean isR5_MT_Device( String familyId, String modelId )
	{
		if ( ( "257".equals( familyId ) ) && ( ( "15".equals( modelId ) ) || ( "16".equals( modelId ) ) || ( "24".equals( modelId ) ) || ( "25".equals( modelId ) ) ) )
		{

			return true;
		}
		return false;
	}

	public static boolean isR5_RT_Device( String familyId, String modelId )
	{
		if ( ( "257".equals( familyId ) ) && ( ( "17".equals( modelId ) ) || ( "26".equals( modelId ) ) || ( "27".equals( modelId ) ) || ( "28".equals( modelId ) ) || ( "29".equals( modelId ) ) || ( "32".equals( modelId ) ) ) )
		{

			return true;
		}
		return false;
	}

	public static boolean isR5_Sandtrap_Device( String familyId, String modelId )
	{
		if ( ( "257".equals( familyId ) ) && ( ( "20".equals( modelId ) ) || ( "31".equals( modelId ) ) ) )
		{
			return true;
		}
		return false;
	}

	public static boolean isR5Device( String manufacturerId, String familyId )
	{
		return ( "257".equals( familyId ) ) && ( "1".equals( manufacturerId ) );
	}

	public static boolean isR4Device( String manufacturerId, String familyId )
	{
		return ( "256".equals( familyId ) ) && ( "1".equals( manufacturerId ) );
	}

	public static boolean isR5OrR4Device( String manufacturerId, String familyId )
	{
		return ( isR5Device( manufacturerId, familyId ) ) || ( isR4Device( manufacturerId, familyId ) );
	}

	public static boolean isNVRDevice( String manufacturerId, String familyId )
	{
		return ( "6".equals( familyId ) ) && ( "1".equals( manufacturerId ) );
	}

	public static boolean isVMSDevice( String manufacturerId, String familyId )
	{
		return ( "4".equals( familyId ) ) && ( "1".equals( manufacturerId ) );
	}

	public static boolean is7532Device( String familyId, String modelId )
	{
		return ( "6".equals( familyId ) ) && ( "1".equals( modelId ) );
	}

	public static boolean isMobileDevice( String familyId, String modelId )
	{
		if ( ( isR4_5000_Device( familyId, modelId ) ) || ( isR5_GT_Device( familyId, modelId ) ) || ( isR5_MT_Device( familyId, modelId ) ) || ( isR5_RT_Device( familyId, modelId ) ) )
		{

			return true;
		}
		return false;
	}

	public static boolean isR4_5000_Device( String familyId, String modelId )
	{
		if ( ( "256".equals( familyId ) ) && ( ( "2".equals( modelId ) ) || ( "3".equals( modelId ) ) ) )
		{
			return true;
		}
		return false;
	}

	public static boolean replacementDeviceModelCheck( String falimyIdFromDeviceDetails, String modelIdFromdeviceDetails, String familyIdFromSnapShot, String modelIdFromSnapShot )
	{
		if ( isR5_GT_Device( familyIdFromSnapShot, modelIdFromSnapShot ) )
		{
			if ( isR5_GT_Device( falimyIdFromDeviceDetails, modelIdFromdeviceDetails ) )
			{
				return true;
			}
		}
		else if ( ( isR5_RT_Device( familyIdFromSnapShot, modelIdFromSnapShot ) ) || ( isR5_MT_Device( familyIdFromSnapShot, modelIdFromSnapShot ) ) )
		{

			if ( ( ( isR5_RT_Device( falimyIdFromDeviceDetails, modelIdFromdeviceDetails ) ) || ( isR5_MT_Device( falimyIdFromDeviceDetails, modelIdFromdeviceDetails ) ) ) && ( modelIdFromSnapShot.equals( modelIdFromdeviceDetails ) ) )
			{

				return true;
			}
		}
		else if ( isR5_Gen5Fixed_Device( familyIdFromSnapShot, modelIdFromSnapShot ) )
		{
			if ( isR5_Gen5Fixed_Device( falimyIdFromDeviceDetails, modelIdFromdeviceDetails ) )
			{
				return true;
			}
		}
		else if ( isR5_VideoSphere_Device( familyIdFromSnapShot, modelIdFromSnapShot ) )
		{
			if ( ( isR5_Gen5Fixed_Device( falimyIdFromDeviceDetails, modelIdFromdeviceDetails ) ) || ( isR5_VideoSphere_Device( familyIdFromSnapShot, modelIdFromSnapShot ) ) )
			{
				return true;
			}
		}
		else if ( isR4_5000_Device( familyIdFromSnapShot, modelIdFromSnapShot ) )
		{
			if ( ( isR4_5000_Device( falimyIdFromDeviceDetails, modelIdFromdeviceDetails ) ) && ( modelIdFromdeviceDetails.equalsIgnoreCase( modelIdFromSnapShot ) ) )
			{
				return true;
			}
		}
		else if ( isR5_Gen6_Device( familyIdFromSnapShot, modelIdFromSnapShot ) )
		{
			if ( ( isR5_Gen6_Device( falimyIdFromDeviceDetails, modelIdFromdeviceDetails ) ) && ( Integer.valueOf( modelIdFromSnapShot ).intValue() <= Integer.valueOf( modelIdFromdeviceDetails ).intValue() ) )
			{
				return true;
			}
		}
		else if ( isR5_Sandtrap_Device( familyIdFromSnapShot, modelIdFromSnapShot ) )
		{
			if ( ( isR5_Sandtrap_Device( falimyIdFromDeviceDetails, modelIdFromdeviceDetails ) ) && ( modelIdFromSnapShot.equals( modelIdFromdeviceDetails ) ) )
			{
				return true;
			}
		}

		return false;
	}

	public static boolean isExtractorDevice( String manufacturerId, String familyId )
	{
		return ( "1001".equals( familyId ) ) && ( "1".equals( manufacturerId ) );
	}

	public static boolean isRegistrationAllowed( String manufacturerId, String familyId )
	{
		return ( !"0".equals( familyId ) ) || ( !"1".equals( manufacturerId ) );
	}

	public static boolean isThirdPartyCamera( String manufacturerId, String modelName )
	{
		return ( !"1".equals( manufacturerId ) ) && ( !"Command Camera".equals( modelName ) );
	}

	public static boolean isThirdPartyIdentifier( String codecPrivateData )
	{
		return "thirdparty".equals( codecPrivateData );
	}

	public static String parseDeviceCookieHeader( String cookieHeader )
	{
		if ( CommonAppUtils.isNullOrEmptyString( cookieHeader ) )
		{
			return null;
		}

		String[] attributeValuePairs = cookieHeader.split( ";" );
		for ( String attribute : attributeValuePairs )
		{
			if ( attribute.contains( "sessionId=" ) )
				return attribute.replaceFirst( "sessionId=", "" );
		}
		return null;
	}

	public static boolean hasPlaceHolderMAC( String[] macAddresses )
	{
		if ( macAddresses != null )
		{
			for ( String macString : macAddresses )
			{
				if ( "00:00:00:00:00:00".equals( macString ) )
				{
					return true;
				}
			}
		}
		return false;
	}
}
