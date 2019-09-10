package com.marchnetworks.management.topology.data;

import com.marchnetworks.command.common.topology.data.AlarmSourceLinkResource;
import com.marchnetworks.command.common.topology.data.AlarmSourceResource;
import com.marchnetworks.command.common.topology.data.AudioOutputResource;
import com.marchnetworks.command.common.topology.data.ChannelLinkResource;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.GenericLinkResource;
import com.marchnetworks.command.common.topology.data.GenericResource;
import com.marchnetworks.command.common.topology.data.Group;
import com.marchnetworks.command.common.topology.data.LinkResource;
import com.marchnetworks.command.common.topology.data.MapResource;
import com.marchnetworks.command.common.topology.data.SwitchLinkResource;
import com.marchnetworks.command.common.topology.data.SwitchResource;
import com.marchnetworks.command.common.topology.data.ViewResource;

public enum ResourceType
{
	GROUP,
	ALARM_SOURCE,
	AUDIO_OUTPUT,
	CHANNEL,
	DEVICE,
	GENERIC,
	SWITCH,
	LINK,
	ALARM_SOURCE_LINK,
	AUDIO_OUTPUT_LINK,
	CHANNEL_LINK,
	MAP,
	SWITCH_LINK,
	GENERIC_LINK,
	VIEW;

	private ResourceType()
	{
	}

	public static Class<?> classFromEnum( ResourceType type )
	{
		switch ( type )
		{
			case GROUP:
				return Group.class;
			case ALARM_SOURCE:
				return AlarmSourceResource.class;
			case AUDIO_OUTPUT:
				return AudioOutputResource.class;
			case CHANNEL:
				return ChannelResource.class;
			case DEVICE:
				return DeviceResource.class;
			case GENERIC:
				return GenericResource.class;
			case SWITCH:
				return SwitchResource.class;
			case LINK:
				return LinkResource.class;
			case ALARM_SOURCE_LINK:
				return AlarmSourceLinkResource.class;
			case AUDIO_OUTPUT_LINK:
				return AudioOutputResource.class;
			case CHANNEL_LINK:
				return ChannelLinkResource.class;
			case MAP:
				return MapResource.class;
			case SWITCH_LINK:
				return SwitchLinkResource.class;
			case GENERIC_LINK:
				return GenericLinkResource.class;
			case VIEW:
				return ViewResource.class;
		}
		return null;
	}
}

