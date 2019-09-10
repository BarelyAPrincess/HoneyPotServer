package com.marchnetworks.common.system;

import com.marchnetworks.common.device.ServerServiceException;

import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

public abstract interface ServerParameterStoreServiceIF
{
	public abstract void init( ContextRefreshedEvent paramContextRefreshedEvent );

	public abstract void storeParameter( String paramString1, String paramString2 ) throws ServerServiceException;

	public abstract String getParameterValue( String paramString );

	public abstract String getParameterValueService( String paramString ) throws ServerServiceException;

	public abstract Map<String, String> getParametersValues( String... paramVarArgs ) throws ServerServiceException;

	public abstract void removeParameter( String paramString );
}
