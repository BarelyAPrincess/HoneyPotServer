package com.marchnetworks.app.service;

import java.util.List;

public abstract interface OsgiService
{
	public abstract <T> T getService( Class<T> paramClass, String paramString );

	public abstract <T> List<T> getServices( Class<T> paramClass );
}
