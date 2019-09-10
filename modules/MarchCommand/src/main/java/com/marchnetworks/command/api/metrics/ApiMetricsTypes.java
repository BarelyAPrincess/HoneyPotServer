package com.marchnetworks.command.api.metrics;

public enum ApiMetricsTypes
{
	REST_CONNECTION( "rest.connection" ),
	REST_SESSION_RENEW( "rest.session.renew" ),
	TRANSACTION( "transaction" ),
	DATABASE_FAILURE( "database.failure" ),
	DEVICE_TASKS( "device.tasks" ),
	SERIAL_TASK( "serial.task" ),
	SERIAL_TASK_QUEUE( "serial.task.queue" );

	private String name;

	private ApiMetricsTypes( String name )
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}

