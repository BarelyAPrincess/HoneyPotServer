package io.amelia.events;

import io.amelia.events.application.ApplicationEvent;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.RegistrarBase;

public class Events
{
	private Events()
	{
		// Static Class
	}

	public void fireAuthorNag( RegistrarBase registrarBase, String message )
	{
		// TODO Implement

		EventDispatcher.listen( Kernel.getApplication(), ApplicationEvent.class, ( e ) ->
		{

		} );
	}
}
