package com.marchnetworks.server.event;

public enum EventProcessingType
{
	SYNCHRONOUS,
	SYNCHRONOUS_CHAINED,
	ASYNC_PARALLEL_PER_LISTENER,
	ASYNC_SERIAL_PER_LISTENER,
	ASYNC_PARALLEL;

	private EventProcessingType()
	{
	}
}

