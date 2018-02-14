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

import com.chiorichan.factory.ScriptingFactory;
import com.chiorichan.factory.api.Builtin;
import com.chiorichan.net.http.HttpRequestWrapper;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import groovy.lang.Binding;
import io.netty.buffer.ByteBufOutputStream;

/**
 * Implements the builtin API by being extent by groovy nested classes
 */
public class NestedScript extends Builtin
{
	HttpRequestWrapper request;

	@SuppressWarnings( "unchecked" )
	public NestedScript()
	{
		request = HttpRequestWrapper.getRequest();
		Binding binding = new Binding( request.getBinding().getVariables() );
		try
		{
			binding.setProperty( "out", new PrintStream( new ByteBufOutputStream( request.getScriptingFactory().getOutputStream() ), true, request.getScriptingFactory().getCharset().name() ) );
		}
		catch ( UnsupportedEncodingException e )
		{
			e.printStackTrace();
		}
		// setBinding( binding );
		getBinding().getVariables().putAll( binding.getVariables() );
		// Setting the binding directly was causing an override of variable value from last class instance.
		// Possibly a link to the groovy MetaClass, hmm, thoughts on how to make use of this...
	}

	@Override
	public Object run()
	{
		return null;
	}

	@Override
	public ScriptingFactory getScriptingFactory()
	{
		return request.getScriptingFactory();
	}
}
