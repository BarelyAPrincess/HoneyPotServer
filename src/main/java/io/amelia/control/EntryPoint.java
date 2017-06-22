package io.amelia.control;

import io.amelia.foundation.Deployment;
import io.amelia.foundation.binding.BindingRegistry;

import java.util.ArrayList;
import java.util.List;

public class EntryPoint
{
	public static void main( String... args ) throws Exception
	{
		/* Statically sets the deployment libraries required to load the application */
		List<String> libraries = new ArrayList<>();
		libraries.add( "org.codehaus.groovy:groovy-all:2.4.7" );
		libraries.add( "io.netty:netty-all:5.0.0.Alpha2" );
		libraries.add( "com.google.javascript:closure-compiler:r2388" );
		libraries.add( "org.mozilla:rhino:1.7R4" );
		libraries.add( "com.asual.lesscss:lesscss-engine:1.3.0" );
		ConfigRegistry.i().set( "deploy.libraries", libraries );

		/* Prepare the environment by downloading and applying the builtin libraries required */
		Deployment.prepare();

		BindingRegistry.extend( Application.class, ServerKernel.class );

		/* Finally load the application */
		Deployment.start( new Application(), args );
	}
}
