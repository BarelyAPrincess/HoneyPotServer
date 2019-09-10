package com.marchnetworks.health.data;

public enum HealthSummaryCategoryEnum
{
	DRIVE( "Drive" ),
	UNIT( "unit" ),
	NETWORK( "Network" ),
	VIDEO( "Video" ),
	POWER( "Power" ),
	PERIPHERAL( "peripheral" );

	private String text;

	private HealthSummaryCategoryEnum( String text )
	{
		this.text = text;
	}

	public String getText()
	{
		return text;
	}
}
