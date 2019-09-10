package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.transport.data.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GenericDeviceAuditEvent extends AbstractDeviceEvent
{
	private static final List<String> auditEventsList = new ArrayList();
	private String pathName;
	private String value;
	private Pair[] details;
	private String source;
	private String username;
	private String remoteIpAddress;
	private String auditEntryId;

	static
	{
		Collections.addAll( auditEventsList, new String[] {DeviceEventsEnum.CHANNEL_LIVEREQUEST.getPath(), DeviceEventsEnum.CHANNEL_PTZ_CONTROL.getPath(), DeviceEventsEnum.CHANNEL_PTZCONTROL.getPath(), DeviceEventsEnum.CHANNEL_PTZ_PRESET.getPath(), DeviceEventsEnum.CHANNEL_PTZ_TOUR.getPath(), DeviceEventsEnum.CHANNEL_ARCHIVEREQUEST.getPath(), DeviceEventsEnum.CHANNEL_EXPORTREQUEST.getPath(), DeviceEventsEnum.SYSTEM_AUDIT_ENTRY.getPath(), DeviceEventsEnum.AUDIO_OUT_STATE.getPath(), DeviceEventsEnum.SWITCH_STATE.getPath(), DeviceEventsEnum.ALARM_STATE.getPath(), DeviceEventsEnum.SYSTEM_LOGDOWNLOAD.getPath(), DeviceEventsEnum.ALERT_CLOSED.getPath()} );
	}

	public static boolean isDeviceAuditEvent( String eventName )
	{
		return auditEventsList.contains( eventName );
	}

	public GenericDeviceAuditEvent( String deviceId, String pathName, String source, String value, Pair[] infoPairs, long timestamp )
	{
		super( GenericDeviceAuditEvent.class.getName(), deviceId, timestamp );
		this.pathName = pathName;
		this.value = value;

		details = infoPairs;
		this.source = source;
		findUsernameAndAddress( infoPairs );
		findAuditEntryId( infoPairs );
	}

	public boolean hasPair( String name )
	{
		boolean hasPair = false;
		if ( details != null )
		{
			for ( int i = 0; i < details.length; i++ )
			{
				Pair pair = details[i];
				if ( pair.getName().equalsIgnoreCase( name ) )
				{
					hasPair = true;
					break;
				}
			}
		}
		return hasPair;
	}

	public String getPairValue( String name )
	{
		String value = null;
		if ( details != null )
		{
			for ( int i = 0; i < details.length; i++ )
			{
				Pair pair = details[i];
				if ( pair.getName().equalsIgnoreCase( name ) )
				{
					return pair.getValue();
				}
			}
		}
		return value;
	}

	private void findUsernameAndAddress( Pair[] pairs )
	{
		if ( pairs != null )
		{
			for ( int i = 0; i < pairs.length; i++ )
			{
				Pair pair = pairs[i];
				boolean updateDetails = false;
				if ( pair.getName().equalsIgnoreCase( "details" ) )
				{
					String[] userAndIP = pair.getValue().split( "@" );
					if ( userAndIP.length < 2 )
						continue;
					username = userAndIP[0];
					remoteIpAddress = userAndIP[1];
					updateDetails = true;
				}
				else if ( ( pair.getName().equalsIgnoreCase( "username" ) ) || ( pair.getName().equalsIgnoreCase( "user" ) ) )
				{
					username = pair.getValue();
					updateDetails = true;
				}
				else if ( ( pair.getName().equalsIgnoreCase( "ipaddress" ) ) || ( pair.getName().equalsIgnoreCase( "address" ) ) )
				{
					remoteIpAddress = pair.getValue();
					updateDetails = true;
				}
				if ( updateDetails )
				{
					List<Pair> list = CollectionUtils.difference( Arrays.asList( details ), Collections.singletonList( pair ) );
					details = ( ( Pair[] ) list.toArray( new Pair[list.size()] ) );
				}
			}
		}
	}

	private void findAuditEntryId( Pair[] pairs )
	{
		if ( pairs != null )
		{
			for ( int i = 0; i < pairs.length; i++ )
			{
				Pair pair = pairs[i];
				if ( pair.getName().equalsIgnoreCase( "audit_entry_id" ) )
				{
					auditEntryId = pair.getValue();
					List<Pair> list = CollectionUtils.difference( Arrays.asList( details ), Collections.singletonList( pair ) );
					details = ( ( Pair[] ) list.toArray( new Pair[list.size()] ) );
					break;
				}
			}
		}
	}

	public String getPathName()
	{
		return pathName;
	}

	public String getValue()
	{
		return value;
	}

	public Pair[] getDetails()
	{
		return details;
	}

	public String getSource()
	{
		return source;
	}

	public String getUsername()
	{
		return username;
	}

	public String getRemoteIpAddress()
	{
		return remoteIpAddress;
	}

	public String getAuditEntryId()
	{
		return auditEntryId;
	}
}

