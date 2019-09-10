package com.marchnetworks.command.common.device.data;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.topology.TopologyException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MassRegistrationInfo
{
	private String address;
	private String user;
	private String password;
	private String stationId;
	private List<String> folderPath;
	private Long parentId;
	private String securityToken;
	private String deviceId;
	private Long resourceId;
	private TopologyException exception;
	private Object data;
	private Map<String, Object> deviceRegistrationInfo;
	private String displayName = null;

	public MassRegistrationInfo()
	{
	}

	public MassRegistrationInfo( String address, Long parentId )
	{
		this.address = address;
		this.parentId = parentId;
	}

	public MassRegistrationInfo( String address, String user, String password, Long parentId )
	{
		this.address = address;
		this.user = user;
		this.password = password;
		this.parentId = parentId;
	}

	public MassRegistrationInfo( String address, String user, String password )
	{
		this.address = address;
		this.user = user;
		this.password = password;
	}

	public void initializeRegistrationInfo()
	{
		deviceRegistrationInfo = new HashMap();

		if ( !address.contains( ":" ) )
		{
			StringBuilder sb = new StringBuilder( address );
			sb.append( ":443" );
			address = sb.toString();
		}

		deviceRegistrationInfo.put( "deviceAdress", address );

		if ( !CommonAppUtils.isNullOrEmptyString( user ) )
		{
			deviceRegistrationInfo.put( "admin", user );
			deviceRegistrationInfo.put( "adminPassword", password );
		}

		if ( !CommonAppUtils.isNullOrEmptyString( securityToken ) )
		{
			deviceRegistrationInfo.put( "globalSecurityToken", securityToken );
		}

		String upperCaseStationId = null;

		if ( !CommonAppUtils.isNullOrEmptyString( stationId ) )
		{
			upperCaseStationId = stationId.toUpperCase();
			deviceRegistrationInfo.put( "stationId", upperCaseStationId );
		}

		deviceRegistrationInfo.put( "isMassRegister", Boolean.valueOf( true ) );
	}

	public void addRegistrationInfo( String key, Object value )
	{
		if ( deviceRegistrationInfo == null )
		{
			deviceRegistrationInfo = new HashMap();
		}
		deviceRegistrationInfo.put( key, value );
	}

	public Map<String, Object> getDeviceRegistrationInfo()
	{
		return deviceRegistrationInfo;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress( String address )
	{
		this.address = address;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser( String user )
	{
		this.user = user;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword( String password )
	{
		this.password = password;
	}

	public String getStationId()
	{
		return stationId;
	}

	public void setStationId( String stationId )
	{
		this.stationId = stationId;
	}

	public Long getParentId()
	{
		return parentId;
	}

	public void setParentId( Long parentId )
	{
		this.parentId = parentId;
	}

	public String getSecurityToken()
	{
		return securityToken;
	}

	public void setSecurityToken( String securityToken )
	{
		this.securityToken = securityToken;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public TopologyException getException()
	{
		return exception;
	}

	public void setException( TopologyException exception )
	{
		this.exception = exception;
	}

	public Object getData()
	{
		return data;
	}

	public void setData( Object data )
	{
		this.data = data;
	}

	public Long getResourceId()
	{
		return resourceId;
	}

	public void setResourceId( Long resourceId )
	{
		this.resourceId = resourceId;
	}

	public List<String> getFolderPath()
	{
		return folderPath;
	}

	public void setFolderPath( List<String> folderPath )
	{
		this.folderPath = folderPath;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName( String displayName )
	{
		this.displayName = displayName;
	}
}
