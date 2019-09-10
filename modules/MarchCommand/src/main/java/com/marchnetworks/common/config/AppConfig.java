package com.marchnetworks.common.config;

public abstract interface AppConfig
{
	public abstract String getProperty( String paramString );

	public abstract String getProperty( ConfigProperty paramConfigProperty );

	public abstract String setProperty( String paramString1, String paramString2 );

	public abstract String setProperty( ConfigProperty paramConfigProperty, String paramString );
}
