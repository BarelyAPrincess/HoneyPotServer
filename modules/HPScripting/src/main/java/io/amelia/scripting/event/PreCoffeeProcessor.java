/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.event;

import com.chiorichan.event.EventHandler;
import com.chiorichan.event.Listener;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class PreCoffeeProcessor implements Listener
{
	@EventHandler( )
	public void onEvent( PreEvalEvent event )
	{
		if ( !event.context().contentType().endsWith( "coffee" ) && !event.context().contentType().endsWith( "litcoffee" ) && !event.context().contentType().endsWith( "coffee.md" ) )
			return;
		
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream( "com/chiorichan/coffee-script.js" );
		
		try
		{
			Reader reader = new InputStreamReader( inputStream, "UTF-8" );
			
			Context context = Context.enter();
			context.setOptimizationLevel( -1 ); // Without this, Rhino hits a 64K bytecode limit and fails
			
			try
			{
				Scriptable globalScope = context.initStandardObjects();
				context.evaluateReader( globalScope, reader, "coffee-script.js", 0, null );
				
				Scriptable compileScope = context.newObject( globalScope );
				compileScope.setParentScope( globalScope );
				compileScope.put( "coffeeScriptSource", compileScope, event.context().readString() );
				
				event.context().resetAndWrite( ( ( String ) context.evaluateString( compileScope, String.format( "CoffeeScript.compile(coffeeScriptSource, %s);", String.format( "{bare: %s, filename: '%s'}", true, event.context().filename() ) ), "CoffeeScriptCompiler-" + event.context().filename(), 0, null ) ).getBytes() );
			}
			finally
			{
				reader.close();
				Context.exit();
			}
		}
		catch ( JavaScriptException e )
		{
			return;
		}
		catch ( IOException e )
		{
			return;
		}
	}
}
