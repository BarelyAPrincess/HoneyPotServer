package io.amelia.node;

import io.amelia.foundation.Application;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.binding.BindingRegistry;
import io.amelia.lang.StartupException;
import io.amelia.networking.NetworkLoader;

public class EntryPoint
{
	public static void main( String... args ) throws Exception
	{
		/* Prepare the environment by downloading and applying the builtin libraries required */
		Kernel.prepare();

		BindingRegistry.extend( Application.class, ServerKernel.class );

		/* Finally load the application */
		Kernel.start( new Application(), args );

		NetworkLoader.UDP().start();

		if ( NetworkLoader.UDP.isActive() )
		{
			NetworkLoader.UDP.sendPacket( new PacketRequestInfo() );
		}
		else
			throw new StartupException( "The UDP service failed to start for unknown reasons." );
	}
}
