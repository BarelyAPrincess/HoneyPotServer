package io.amelia.march;

import com.marchnetworks.command.api.app.AppCoreService;
import com.marchnetworks.command.api.initialization.BeforeInitializationListener;
import com.marchnetworks.command.api.initialization.InitializationListener;

import org.reflections.Reflections;

import io.amelia.events.EventHandler;
import io.amelia.events.Events;
import io.amelia.events.RunlevelEvent;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.RegistrarBase;
import io.amelia.foundation.Runlevel;
import io.amelia.lang.ApplicationException;

public class MarchInitializer implements RegistrarBase
{
	public static final Reflections REFLECTIONS = Foundation.getReflections();

	@EventHandler
	public static void onRunlevelEvent( RunlevelEvent runlevelEvent ) throws ApplicationException.Error
	{
		if ( runlevelEvent.getRunLevel() == Runlevel.INITIALIZATION )
		{
			for ( Class<? extends BeforeInitializationListener> result : REFLECTIONS.getSubTypesOf( BeforeInitializationListener.class ) )
				Foundation.make( result ).orElseThrow( () -> new ApplicationException.Error( "Failed to instigate class \"" + result.getName() + "\"." ) );
		}
		if ( runlevelEvent.getRunLevel() == Runlevel.STARTED )
		{
			for ( Class<? extends InitializationListener> result : REFLECTIONS.getSubTypesOf( InitializationListener.class ) )
				Foundation.make( result ).orElseThrow( () -> new ApplicationException.Error( "Failed to instigate class \"" + result.getName() + "\"." ) );
		}
	}
	private AppCoreService appCoreService;
	private String appId;

	@Override
	public String getName()
	{
		return "MarchInit";
	}

	public void init()
	{
		Events.getInstance().listen( this, this );
	}

	public void onApplicationEvent()
	{
		if ( appCoreService != null )
			appCoreService.appInitializationComplete( appId, true );
	}

	public void setAppCoreService( AppCoreService appCoreService )
	{
		this.appCoreService = appCoreService;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}
}
