package io.amelia.events;

import io.amelia.events.application.ApplicationEvent;
import io.amelia.foundation.App;
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

		EventDispatcher.listen( App.getApplication(), ApplicationEvent.class, ( e ) ->
		{

		} );
	}
}
