package com.marchnetworks.common.diagnostics.metrics;

public enum MetricsTypes
{
	SOAP_CONNECTION( "soap.connection" ),
	SOAP_SESSION_RENEW( "soap.session.renew" ),
	SOAP_PORT_CREATE( "soap.port.creation" ),
	DEVICE_EVENTS( "device.events" ),
	DEVICE_RESTARTS( "device.restarts" ),
	DEVICE_RESUBSCRIBE( "device.resubscribe" ),
	DEVICE_FETCH_EVENTS( "device.fetch.events" ),
	DEVICE_CONNECTION( "device.connection" ),
	DEVICE_REGISTRATION( "device.registration" ),
	DEVICE_UNREGISTRATION( "device.unregistration" ),
	DEVICE_SESSION_CREATE( "device.session.create" ),
	DEVICE_EVENT_HANDLER( "device.event.handler" ),
	USER_LOGINS( "user.logins" ),
	ALARM_ENTRIES( "alarm.entries" ),
	ALARM_STATES( "alarm.states" ),
	CHANNEL_STATES( "channel.states" ),
	ALERTS( "alerts" ),
	SWITCH_STATES( "switch.states" ),
	AUDIO_OUT_STATES( "audio.out.states" ),
	SYSTEM_CHANGED_EVENT( "system.changed" ),
	SYSTEM_CONFIGHASH_EVENT( "system.confighash" ),
	EVENTS_SYNC( "events.sync" ),
	EVENTS_ASYNC( "events.async" ),
	EVENTS_CHAINED( "events.chained" ),
	FRAGMENTATION( "fragmentation" ),
	LDAP_FALLBACK_BIND( "ldap.fallback.bind" );

	private String name;

	private MetricsTypes( String name )
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
