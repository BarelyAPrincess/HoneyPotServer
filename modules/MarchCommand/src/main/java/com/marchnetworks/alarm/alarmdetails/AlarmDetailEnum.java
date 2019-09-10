package com.marchnetworks.alarm.alarmdetails;

public enum AlarmDetailEnum
{
	PARTY_INVOLVED_POLICE( "a" ),
	PARTY_INVOLVED_FIRE( "b" ),
	PARTY_INVOLVED_AMBULANCE( "c" ),
	PARTY_INVOLVED_OTHER( "d" ),

	INCIDENT_SHOPLIFTING( "e" ),
	INCIDENT_LOITERING( "f" ),
	INCIDENT_PERMANENCY( "g" ),
	INCIDENT_PANIC( "h" ),
	INCIDENT_FALL( "i" ),
	INCIDENT_SUSPICIOUS( "j" ),
	INCIDENT_VANDALISM( "k" ),

	SEVERITY_UNSPECIFIED( "l" ),
	SEVERITY_CRITICAL( "m" ),
	SEVERITY_SEVERE( "n" ),
	SEVERITY_MINOR( "o" ),
	SEVERITY_FALSE( "p" ),

	VICTIM_UNSPECIFIED( "q" ),
	VICTIM_EMPLOYEE( "r" ),
	VICTIM_CUSTOMER( "s" ),
	VICTIM_OTHER( "t" ),
	VICTIM_NONE( "u" );

	private final String value;

	public String getValue()
	{
		return value;
	}

	private AlarmDetailEnum( String value )
	{
		this.value = value;
	}
}
