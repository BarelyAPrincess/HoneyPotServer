package com.marchnetworks.common.system;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ServerParameterStoreServiceInit implements ApplicationListener<ContextRefreshedEvent>
{
	private ServerParameterStoreServiceIF serverParameterStoreService;

	public void setServerParameterStoreService( ServerParameterStoreServiceIF serverParameterStoreService )
	{
		this.serverParameterStoreService = serverParameterStoreService;
	}

	public void onApplicationEvent( ContextRefreshedEvent ev )
	{
		serverParameterStoreService.init( ev );
	}
}
