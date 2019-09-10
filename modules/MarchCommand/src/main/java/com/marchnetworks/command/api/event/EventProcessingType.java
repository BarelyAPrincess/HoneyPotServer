package com.marchnetworks.command.api.event;

public enum EventProcessingType
{
	SYNCHRONOUS,
	ASYNCHRONOUS,
	ASYNCHRONOUS_POOLED,
	ASYNCHRONOUS_SERIAL;

	private EventProcessingType()
	{
	}
}
