package com.marchnetworks.command.common.device.data;

public enum ChannelState
{
	ONLINE,
	OFFLINE,
	UNKNOWN,
	DISABLED;

	public static ChannelState stateFromString( String stateString )
	{
		for ( ChannelState state : values() )
		{
			if ( state.name().equalsIgnoreCase( stateString ) )
			{
				return state;
			}
		}
		return null;
	}
}
