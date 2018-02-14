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
import com.chiorichan.helpers.Triplet;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.ScriptingException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

/**
 * ScriptingEngine for handling GSP files, i.e., Embedded Groovy File a.k.a. Groovy Server Pages.
 * <p>
 * <code>This is plain html<%= print ", with a twist of groovy. Today's date is: " + date("") %>.</code>
 */
public class EmbeddedGroovyEngine implements ScriptingEngine
{
	// private static final String MARKER_START = "<%";
	// private static final String MARKER_END = "%>";

	private static final String[] DO_NOT_PREPEND = new String[] {"println", "print", "echo", "def", "import", "if", "for", "do", "while", "{", "}", "else", "//", "/*", "\n", "\r"};
	private static final List<Triplet<String, String, String>> MARKERS = new ArrayList<>();

	static
	{
		MARKERS.add( new Triplet<>( "<%", null, "%>" ) );
		MARKERS.add( new Triplet<>( "<%=", "echo", "%>" ) );

		MARKERS.add( new Triplet<>( "{{", "echo", "}}" ) );
		MARKERS.add( new Triplet<>( "{!!", "print", "!!}" ) );
		MARKERS.add( new Triplet<>( "{{--", "comment", "--}}" ) );

		if ( ConfigRegistry.i().getBoolean( "advanced.scripting.gspAllowPhpTags" ) )
		{
			MARKERS.add( new Triplet<>( "<?", null, "?>" ) );
			MARKERS.add( new Triplet<>( "<?=", "echo", "?>" ) );
		}
	}

	private Binding binding = new Binding();
	private GroovyRegistry registry;

	public EmbeddedGroovyEngine( GroovyRegistry registry )
	{
		this.registry = registry;
	}

	public String escapeFragment( StringBuilder output, String fragment )
	{
		return escapeFragment( output, fragment, null );
	}

	public String escapeFragment( StringBuilder output, String fragment, String method )
	{
		if ( fragment == null || fragment.length() == 0 )
			return "";
		if ( method == null )
			method = "print ";
		if ( !method.endsWith( " " ) )
			method = method + " ";

		String brackets = "\"\"\"";

		fragment = fragment.replace( "\\u005Cu0024", "$" );
		fragment = fragment.replace( "\\u005Cu005C", "\\" );

		fragment = fragment.replace( "\\", "\\u005Cu005C" ); // Prevent Escaping
		fragment = fragment.replace( "$", "\\u005Cu0024" ); // Prevent GString

		if ( fragment.endsWith( "\"" ) )
			brackets = "'''";

		boolean newline = false;
		String lookBack = output.toString();
		// Get last line of code
		if ( lookBack.contains( "\n" ) )
			lookBack = lookBack.substring( lookBack.lastIndexOf( "\n" ) + 1 );
		if ( lookBack.contains( "//" ) )
			newline = true;
		// TODO New other reasons previous code elements would cause exceptions and create a file line number mapping system

		return ( newline ? "\n" : "" ) + method + brackets + fragment + brackets + "; ";
	}

	@Override
	public boolean eval( ScriptingContext context ) throws Exception
	{
		try
		{
			Script script = GroovyRegistry.getCachedScript( context, binding );

			if ( script == null )
			{
				String source = context.readString();

				int fullFileIndex = 0;

				StringBuilder output = new StringBuilder();

				while ( fullFileIndex < source.length() )
				{
					Triplet<String, String, String> activeMarker = null;
					int startIndex = -1;

					// Check which marker comes closest to the current index.
					for ( Triplet<String, String, String> marker : MARKERS )
					{
						int nextIndex = source.indexOf( marker.getStart(), fullFileIndex );

						if ( nextIndex > -1 && ( startIndex == -1 || nextIndex < startIndex || nextIndex == startIndex && marker.getStart().length() > activeMarker.getStart().length() ) )
						{
							startIndex = nextIndex;
							activeMarker = marker;
						}
					}

					if ( startIndex > -1 )
					{
						// Append all the text until the marker
						String fragment = escapeFragment( output, source.substring( fullFileIndex, startIndex ) );

						if ( fragment.length() > 0 )
							output.append( fragment );

						int endIndex = source.indexOf( activeMarker.getEnd(), Math.max( startIndex, fullFileIndex ) );
						if ( endIndex == -1 )
							throw new ScriptingException( ReportingLevel.E_PARSE, String.format( "Found starting marker '%s' at line %s, expected close marker '%s' not found.", activeMarker.getStart(), StringUtils.countMatches( output.toString(), "\n" ) + 1, activeMarker.getEnd() ) );

						// Gets the entire fragment?
						fragment = source.substring( startIndex + activeMarker.getStart().length(), endIndex ).trim();

						// TODO Implement marker content type, wrap content as string, i.e., is not code.
						boolean prependMiddle = activeMarker.getMiddle() != null && activeMarker.getMiddle().length() > 0;
						boolean wrapMiddleAsString = activeMarker.getMiddle() != null && activeMarker.getMiddle().equals( "comment" );

						for ( String s : DO_NOT_PREPEND )
							if ( fragment.startsWith( s ) )
								prependMiddle = false;

						if ( prependMiddle )
						{
							StringBuilder builder = new StringBuilder();

							if ( wrapMiddleAsString )
								builder.append( escapeFragment( output, fragment, activeMarker.getMiddle() ) );
							else
							{
								builder.append( activeMarker.getMiddle() ).append( "( " ).append( fragment.contains( ";" ) ? fragment.substring( 0, fragment.indexOf( ";" ) ) : fragment ).append( " ); " );

								if ( fragment.contains( ";" ) && fragment.length() - fragment.indexOf( ";" ) > 0 )
									builder.append( fragment.substring( fragment.indexOf( ";" ) + 1 ) );
							}

							fragment = builder.toString().trim();
						}

						if ( fragment.length() > 0 )
							output.append( fragment + ( fragment.endsWith( ";" ) ? "" : ";" ) );

						// Position index after end marker
						fullFileIndex = endIndex + activeMarker.getEnd().length();
					}
					else
					{
						String fragment = escapeFragment( output, source.substring( fullFileIndex ) );

						if ( !fragment.isEmpty() )
							output.append( fragment );

						// Position index after the end of the file
						fullFileIndex = source.length() + 1;
					}
				}

				context.baseSource( output.toString() );

				GroovyShell shell = registry.getNewShell( context, binding );
				script = registry.makeScript( shell, output.toString(), context );
			}

			context.result().setScript( script );
			context.result().setObject( script.run() );
		}
		catch ( Throwable t )
		{
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
		return Arrays.asList( "embedded", "gsp", "jsp", "chi" );
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
