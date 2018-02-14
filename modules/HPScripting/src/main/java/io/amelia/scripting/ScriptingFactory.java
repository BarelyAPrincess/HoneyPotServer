package io.amelia.scripting;

import com.google.common.collect.Maps;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.ReportingLevel;
import io.amelia.scripting.groovy.GroovyRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ScriptingFactory
{
	private static volatile List<ScriptingRegistry> scripting = new ArrayList<>();

	static
	{
		new GroovyRegistry();

		/*
		 * Register Pre-Processors
		 */
		register( new PreLinksParserWrapper() );
		register( new PreIncludesParserWrapper() );
		if ( ConfigRegistry.config.getBoolean( ConfigKeys.PROCESSORS_COFFEE ).orElse( true ) )
			register( new PreCoffeeProcessor() );
		if ( ConfigRegistry.config.getBoolean( ConfigKeys.PROCESSORS_LESS ).orElse( true ) )
			register( new PreLessProcessor() );
		// register( new SassPreProcessor() );

		/*
		 * Register Post-Processors
		 */
		if ( ConfigRegistry.config.getBoolean( ConfigKeys.PROCESSORS_MINIFY_JS ).orElse( true ) )
			register( new PostJSMinProcessor() );
		if ( ConfigRegistry.config.getBoolean( ConfigKeys.PROCESSORS_IMAGES ).orElse( true ) )
			register( new PostImageProcessor() );
	}

	// For Web Use
	public static ScriptingFactory create( BindingProvider provider )
	{
		return new ScriptingFactory( provider.getBinding() );
	}

	// For General Use
	public static ScriptingFactory create( Map<String, Object> rawBinding )
	{
		return new ScriptingFactory( new ScriptBinding( rawBinding ) );
	}

	// For General Use
	public static ScriptingFactory create( ScriptBinding binding )
	{
		return new ScriptingFactory( binding );
	}

	public static void register( Listener listener )
	{
		EventDispatcher.i().registerEvents( listener, new RegistrarContext( AppLoader.instances().get( 0 ) ) );
	}

	/**
	 * Registers the provided ScriptingProcessing with the EvalFactory
	 *
	 * @param registry The {@link ScriptingRegistry} instance to handle provided types
	 */
	public static void register( ScriptingRegistry registry )
	{
		if ( !scripting.contains( registry ) )
			scripting.add( registry );
	}

	private final ScriptBinding binding;
	private final List<Pair<ByteBuf, StackType>> bufferStack = new LinkedList<>();
	private final Map<ScriptingEngine, List<String>> engines = Maps.newLinkedHashMap();
	private final ByteBuf output = Unpooled.buffer();
	private final StackFactory stackFactory = new StackFactory();
	private Charset charset = Charsets.toCharset( ConfigRegistry.config.getString( "server.defaultEncoding", "UTF-8" ) );
	private YieldBuffer yieldBuffer = null;

	private ScriptingFactory( ScriptBinding binding )
	{
		Validate.notNull( binding, "The EvalBinding can't be null" );
		this.binding = binding;
	}

	public ScriptBinding binding()
	{
		return binding;
	}

	/**
	 * Returns the output buffer to it's last state
	 */
	private void bufferPop( int level )
	{
		if ( bufferStack.size() == 0 )
			throw new IllegalStateException( "Buffer stack is empty." );

		if ( bufferStack.size() - 1 < level )
			throw new IllegalStateException( "Buffer stack size was too low." );

		// Check for possible forgotten obEnd()'s. Could loop as each detection will move up one next level.
		if ( bufferStack.size() > level + 1 && bufferStack.get( level + 1 ).getValue() == StackType.OB )
			obFlush( level + 1 );

		// Determines if the buffer was not push'd or pop'd in the correct order, often indicating outside manipulation of the bufferStack.
		if ( bufferStack.size() - 1 > level )
			throw new IllegalStateException( "Buffer stack size was too high." );

		output.clear();
		output.writeBytes( bufferStack.remove( level ).getKey() );
	}

	/**
	 * Stores the current output buffer for the stacked capture
	 */
	private int bufferPush( StackType type )
	{
		bufferStack.add( new Pair<>( output.copy(), type ) );
		output.clear();
		return bufferStack.size() - 1;
	}

	public Charset charset()
	{
		return charset;
	}

	private void compileEngines( ScriptingContext context )
	{
		for ( ScriptingRegistry registry : scripting )
			for ( ScriptingEngine engine : registry.makeEngines( context ) )
				if ( !contains( engine ) )
				{
					engine.setBinding( binding );
					engine.setOutput( output, charset );
					engines.put( engine, engine.getTypes() );
				}
	}

	private boolean contains( ScriptingEngine engine2 )
	{
		for ( ScriptingEngine engine1 : engines.keySet() )
			if ( engine1.getClass() == engine2.getClass() )
				return true;
		return false;
	}

	public ScriptingResult eval( ScriptingContext context )
	{
		final ScriptingResult result = context.result();

		context.factory( this );
		context.charset( charset );
		context.baseSource( new String( context.readBytes(), charset ) );
		binding.setVariable( "__FILE__", context.filename() == null ? "<no file>" : context.filename() );

		if ( result.hasNonIgnorableExceptions() )
			return result;

		try
		{
			String name;
			if ( context.isVirtual() )
				name = "EvalScript" + UtilEncryption.rand( 8 ) + ".chi";
			else
			{
				String rel = UtilIO.relPath( context.file().getParentFile(), context.site().directory() ).replace( '\\', '.' ).replace( '/', '.' );
				context.cacheDirectory( new File( context.cacheDirectory(), rel.contains( "." ) ? rel.substring( 0, rel.indexOf( "." ) ) : rel ) );
				context.scriptPackage( rel.contains( "." ) ? rel.substring( rel.indexOf( "." ) + 1 ) : "" );
				name = context.file().getName();
			}

			context.scriptName( name );
			stackFactory.stack( name, context );

			PreEvalEvent preEvent = new PreEvalEvent( context );
			try
			{
				EventDispatcher.i().callEventWithException( preEvent );
			}
			catch ( Exception e )
			{
				if ( result.handleException( e.getCause() == null ? e : e.getCause(), context ) )
					return result;
			}

			if ( preEvent.isCancelled() )
				if ( result.handleException( new ScriptingException( ReportingLevel.E_ERROR, "Evaluation was cancelled by an internal event" ), context ) )
					return result;

			if ( engines.size() == 0 )
				compileEngines( context );

			if ( engines.size() > 0 )
				for ( Entry<ScriptingEngine, List<String>> entry : engines.entrySet() )
					if ( entry.getValue() == null || entry.getValue().size() == 0 || entry.getValue().contains( context.shell().toLowerCase() ) )
					{
						int level = bufferPush( StackType.SCRIPT );
						try
						{
							// Determine if data was written to the context during the eval(). Indicating data was either written directly or a sub-eval was called.
							String hash = context.bufferHash();
							entry.getKey().eval( context );
							if ( context.bufferHash().equals( hash ) )
								context.resetAndWrite( output );
							else
								context.write( output );
							break;
						}
						catch ( Throwable cause )
						{
							// On return true, it was a severe problem and the execution should be stopped.
							if ( result.handleException( cause, context ) )
								return result;
						}
						finally
						{
							bufferPop( level );
						}
					}

			PostEvalEvent postEvent = new PostEvalEvent( context );
			try
			{
				EventDispatcher.i().callEventWithException( postEvent );
			}
			catch ( EventException e )
			{
				if ( result.handleException( e.getCause() == null ? e : e.getCause(), context ) )
					return result;
			}
		}
		finally
		{
			stackFactory.unstack();
		}

		return result.success( true );
	}

	public Charset getCharset()
	{
		return charset;
	}

	public String getFileName()
	{
		List<ScriptTraceElement> scriptTrace = getScriptTrace();

		if ( scriptTrace.size() < 1 )
			return "<unknown>";

		String fileName = scriptTrace.get( scriptTrace.size() - 1 ).context().filename();

		if ( fileName == null || fileName.isEmpty() )
			return "<unknown>";

		return fileName;
	}

	/**
	 * Attempts to find the current line number for the current groovy script.
	 *
	 * @return The current line number. Returns -1 if no there was a problem getting the current line number.
	 */
	public int getLineNumber()
	{
		List<ScriptTraceElement> scriptTrace = getScriptTrace();

		if ( scriptTrace.size() < 1 )
			return -1;

		return scriptTrace.get( scriptTrace.size() - 1 ).getLineNumber();
	}

	public ByteBuf getOutputStream()
	{
		return output;
	}

	public List<ScriptTraceElement> getScriptTrace()
	{
		return stackFactory.examineStackTrace( Thread.currentThread().getStackTrace() );
	}

	public YieldBuffer getYieldBuffer()
	{
		if ( yieldBuffer == null )
			yieldBuffer = new YieldBuffer();
		return yieldBuffer;
	}

	public String obEnd( int stackLevel )
	{
		if ( bufferStack.get( stackLevel ).getValue() != StackType.OB )
			throw new IllegalStateException( "The stack level was not an Output Buffer." );

		String content = output.toString( charset );

		bufferPop( stackLevel );

		return content;
	}

	public void obFlush( int stackLevel )
	{
		// Forward the output buffer content into the last buffer
		String content = obEnd( stackLevel );
		print( content );
	}

	public int obStart()
	{
		return bufferPush( StackType.OB );
	}

	/**
	 * Gives externals subroutines access to the current output stream via print()
	 *
	 * @param text The text to output
	 */
	public void print( String text )
	{
		output.writeBytes( text.getBytes( charset ) );
	}

	/**
	 * Gives externals subroutines access to the current output stream via println()
	 *
	 * @param text The text to output
	 */
	public void println( String text )
	{
		output.writeBytes( ( text + "\n" ).getBytes( charset ) );
	}

	public void setEncoding( Charset charset )
	{
		this.charset = charset;
	}

	public void setVariable( String key, Object val )
	{
		binding.setVariable( key, val );
	}

	public StackFactory stack()
	{
		return stackFactory;
	}

	private enum StackType
	{
		SCRIPT,
		// Indicates script output stack
		OB // Indicates output buffer stack
	}

	public static class ConfigKeys
	{
		public static final String SCRIPTING_BASE = "scripting";
		public static final String PROCESSORS_BASE = SCRIPTING_BASE + ".processors";
		public static final String PROCESSORS_COFFEE = PROCESSORS_BASE + ".coffeeEnabled";
		public static final String PROCESSORS_LESS = PROCESSORS_BASE + ".lessEnabled";
		public static final String PROCESSORS_MINIFY_JS = PROCESSORS_BASE + ".minifyJSEnabled";
		public static final String PROCESSORS_IMAGES = PROCESSORS_BASE + ".imagesEnabled";
	}
}
