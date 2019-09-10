package com.marchnetworks.common.scheduling.asynchronous;

import java.util.UUID;

public class GenericAsynchronousEvent extends AsynchronousEvent
{
	public GenericAsynchronousEvent( UUID uuid, Object payload, boolean isLastEvent )
	{
		super( payload );
		setUUID( uuid );
		setIsLastEvent( isLastEvent );
	}
}
