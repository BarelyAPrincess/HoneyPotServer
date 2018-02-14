/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.groovy;

import com.chiorichan.factory.ScriptBinding;
import com.chiorichan.factory.ScriptingContext;
import com.chiorichan.factory.ScriptingEngine;
import com.chiorichan.factory.ScriptingEvents;
import com.chiorichan.net.NetworkManager;
import com.chiorichan.utils.UtilObjects;
import com.chiorichan.utils.UtilStrings;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MetaMethod;
import groovy.lang.Script;
import io.amelia.lang.HttpError;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.ScriptingException;
import io.amelia.logging.LogBuilder;
import io.amelia.support.Versioning;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Groovy Script Engine
 */
public class GroovyEngine implements ScriptingEngine
{
	private Binding binding = new Binding();
	private GroovyRegistry registry;

	public GroovyEngine( GroovyRegistry registry )
	{
		this.registry = registry;
	}

	@Override
	public boolean eval( ScriptingContext context ) throws Exception
	{
		Script script = null;
		try
		{
			script = GroovyRegistry.getCachedScript( context, binding );

			if ( script == null )
			{
				GroovyShell shell = registry.getNewShell( context, binding );
				script = registry.makeScript( shell, context );
			}

			context.result().setScript( script );

			if ( script instanceof ScriptingEvents )
				( ( ScriptingEvents ) script ).onBeforeExecute( context );

			if ( context.filename() != null && context.filename().contains( ".controller." ) )
			{
				String action = context.request().getInterpreter().getAction();
				String actionMethodName = UtilObjects.isEmpty( action ) ? "actionDefault" : "action" + UtilStrings.capitalizeWordsFully( action, '/' ).replace( "/", "" );

				MetaMethod actionMethod = script.getMetaClass().getMetaMethod( actionMethodName, new Object[0] );
				boolean foundMethod = actionMethod != null;

				if ( foundMethod )
				{
					NetworkManager.getLogger().fine( "Invoking " + actionMethodName + "() method on controller " + context.filename() );
					actionMethod.invoke( script, new Object[0] );
				}
				else
				{
					MetaMethod catchAllMethod = script.getMetaClass().getMetaMethod( "catchAll", new Object[] {String.class} );
					foundMethod = catchAllMethod != null;
					if ( foundMethod )
					{
						NetworkManager.getLogger().fine( "Invoking catchAll() method on controller " + context.filename() );
						Object result = catchAllMethod.invoke( script, new Object[] {action} );
						if ( !UtilObjects.isTrue( result ) )
						{
							if ( Versioning.isDevelopment() )
								throw new ScriptingException( ReportingLevel.E_PARSE, String.format( "Detected groovy script as being a controller, located the catchAll() method but the action went unhandled. {expectedMethod: %s, action: %s}", actionMethodName, action ) );
							else
							{
								NetworkManager.getLogger().severe( String.format( "Detected groovy script as being a controller, located the catchAll() method but the action went unhandled. {expectedMethod: %s, action: %s}", actionMethodName, action ) );
								throw new HttpError( HttpResponseStatus.NOT_FOUND );
							}
						}
					}
				}

				if ( !foundMethod )
					throw new ScriptingException( ReportingLevel.E_PARSE, String.format( "Detected groovy script as being a controller but was unable to invoke any controller methods. {expectedMethod: %s, action: %s}", actionMethodName, action ) );

				context.result().setObject( null );
			}
			else
				context.result().setObject( script.run() );

			if ( script instanceof ScriptingEvents )
				( ( ScriptingEvents ) script ).onAfterExecute( context );
		}
		catch ( Throwable t )
		{
			if ( script != null && script instanceof ScriptingEvents )
				try
				{
					( ( ScriptingEvents ) script ).onException( context, t );
				}
				catch ( Throwable tt )
				{
					LogBuilder.get( "Scripting" ).severe( "We had a problem passing an scripting exception to the causing Script, which implements the ScriptingEvents interface.", tt );
				}

			// Clear the input source code and replace it with the exception stack trace
			// context.resetAndWrite( ExceptionUtils.getStackTrace( t ) );
			context.reset();
			throw t;
		}

		return true;
	}

	@Override
	public List<String> getTypes()
	{
		return Arrays.asList( "groovy" );
	}

	@Override
	public void setBinding( ScriptBinding binding )
	{
		// Groovy Binding will keep the original EvalBinding map updated automatically. YAY!
		this.binding = new Binding( binding.getVariables() );
	}

	@Override
	public void setOutput( ByteBuf buffer, Charset charset )
	{
		try
		{
			binding.setProperty( "out", new PrintStream( new ByteBufOutputStream( buffer ), true, charset.name() ) );
		}
		catch ( UnsupportedEncodingException e )
		{
			e.printStackTrace();
		}
	}
}
