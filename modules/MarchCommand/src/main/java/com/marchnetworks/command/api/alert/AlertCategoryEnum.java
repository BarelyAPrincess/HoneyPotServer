package com.marchnetworks.command.api.alert;

public enum AlertCategoryEnum
{
	VIDEO( "Video" ),
	NOT_RECORDING( "Not Recording" ),
	STORAGE( "Storage" ),
	NETWORK( "Network" ),
	POWER( "Power" ),
	SW( "Software" ),
	HW( "Hardware" ),
	UNKNOWN( "Unknown" ),
	DEVICE( "Device" ),
	AUDIO( "Audio" ),
	LICENSE( "License" );

	private String m_Text;

	private AlertCategoryEnum( String a_Text )
	{
		m_Text = a_Text;
	}

	public String getText()
	{
		return m_Text;
	}
}
