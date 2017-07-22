/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

import io.amelia.logcompat.LogBuilder;
import io.amelia.support.Objs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


/**
 * This class is used to analyze and report exceptions
 */
public class ExceptionReport
{
	private static final Map<Class<? extends Throwable>, ExceptionCallback> registered = new ConcurrentHashMap<>();

	public static String printExceptions( IException... exceptions )
	{
		return printExceptions( Arrays.stream( exceptions ) );
	}

	public static String printExceptions( Stream<IException> exceptions )
	{
		// Might need some better handling for this!
		StringBuilder sb = new StringBuilder();
		exceptions.forEach( e -> sb.append( e.getMessage() ).append( "\n" ) );
		return sb.toString();
	}

	/**
	 * Registers an expected exception to be thrown
	 *
	 * @param callback The Callback to call when such exception is thrown
	 * @param clzs     Classes to be registered
	 */
	@SafeVarargs
	public static void registerException( ExceptionCallback callback, Class<? extends Throwable>... clzs )
	{
		for ( Class<? extends Throwable> clz : clzs )
			registered.put( clz, callback );
	}

	public static void throwExceptions( IException... exceptions ) throws Exception
	{
		List<IException> exps = new ArrayList<>();

		for ( IException e : exceptions )
		{
			IException.check( e );
			if ( !e.reportingLevel().isIgnorable() )
				exps.add( e );
		}

		if ( exps.size() == 1 )
			if ( exps.get( 0 ) instanceof Exception )
				throw ( Exception ) exps.get( 0 );
			else
				throw new UncaughtException( ( Throwable ) exps.get( 0 ) );
		else if ( exps.size() > 0 )
			throw new MultipleException( exps );
	}

	protected final List<IException> caughtExceptions = new ArrayList<>();

	public ExceptionReport addException( IException exception )
	{
		IException.check( exception );
		if ( exception != null )
			caughtExceptions.add( exception );
		return this;
	}

	public ExceptionReport addException( ReportingLevel level, String msg, Throwable throwable )
	{
		if ( throwable != null )
			if ( throwable instanceof UncaughtException )
			{
				( ( UncaughtException ) throwable ).setReportingLevel( level );
				caughtExceptions.add( ( IException ) throwable );
			}
			else
				caughtExceptions.add( new UncaughtException( level, msg, throwable ) );
		return this;
	}

	public ExceptionReport addException( ReportingLevel level, Throwable throwable )
	{
		if ( throwable != null )
			if ( throwable instanceof UncaughtException )
			{
				( ( UncaughtException ) throwable ).setReportingLevel( level );
				caughtExceptions.add( ( IException ) throwable );
			}
			else
				caughtExceptions.add( new UncaughtException( level, throwable ) );
		return this;
	}

	public Stream<IException> getIgnorableExceptions()
	{
		return caughtExceptions.stream().filter( e -> e.reportingLevel().isIgnorable() );
	}

	public Stream<IException> getNotIgnorableExceptions()
	{
		return caughtExceptions.stream().filter( e -> !e.reportingLevel().isIgnorable() );
	}

	/**
	 * Processes and appends the throwable to the context provided.
	 *
	 * @param cause   The exception thrown
	 * @param context The EvalContext associated with the eval request
	 * @return True if we should abort any further execution of code
	 */
	public final boolean handleException( Throwable cause, ExceptionContext context )
	{
		if ( Objs.isNull( cause ) )
			return false;

		/* Give an IException a chance to self-handle the exception report */
		if ( cause instanceof IException )
		{
			// TODO Might not be desirable if a handle method was to return severe but not provide any exception or debug information to the ExceptionReport
			ReportingLevel reportingLevel = ( ( IException ) cause ).handle( this, context );
			if ( reportingLevel != null )
				return !reportingLevel.isIgnorable();
		}

		/* Parse each IException and return true if one or more IExceptions produced NonIgnorableExceptions */
		if ( cause instanceof MultipleException )
		{
			boolean abort = false;
			for ( IException e : ( ( MultipleException ) cause ).getExceptions() )
			{
				IException.check( e );
				if ( handleException( ( Throwable ) e, context ) )
					abort = true;
			}
			return abort;
		}

		Map<Class<? extends Throwable>, ExceptionCallback> assignable = new HashMap<>();

		for ( Entry<Class<? extends Throwable>, ExceptionCallback> entry : registered.entrySet() )
			if ( cause.getClass().equals( entry.getKey() ) )
			{
				ReportingLevel e = entry.getValue().callback( cause, this, context );
				if ( e != null )
					return !e.isIgnorable();
			}
			else if ( entry.getKey().isAssignableFrom( cause.getClass() ) )
				assignable.put( entry.getKey(), entry.getValue() );

		if ( assignable.size() == 1 )
		{
			ReportingLevel e = assignable.values().toArray( new ExceptionCallback[0] )[0].callback( cause, this, context );
			if ( e != null )
				return !e.isIgnorable();
		}
		else if ( assignable.size() > 1 )
			for ( Entry<Class<? extends Throwable>, ExceptionCallback> entry : assignable.entrySet() )
			{
				for ( Class<?> iface : cause.getClass().getInterfaces() )
					if ( iface.equals( entry.getKey() ) )
					{
						ReportingLevel e = entry.getValue().callback( cause, this, context );
						if ( e != null )
							return !e.isIgnorable();
						break;
					}

				Class<?> superClass = cause.getClass();
				if ( superClass != null )
					do
					{
						if ( superClass.equals( entry.getKey() ) )
						{
							ReportingLevel e = entry.getValue().callback( cause, this, context );
							if ( e != null )
								return !e.isIgnorable();
							break;
						}
						superClass = cause.getClass();
					}
					while ( superClass != null );
			}

		/*
		 * Handle the remainder unhandled run of the mill exceptions
		 * NullPointerException, ArrayIndexOutOfBoundsException, IOException, StackOverflowError, ClassFormatError
		 */
		LogBuilder.get().severe( String.format( "The exception %s went unhandled in the ScriptingFactory.", cause.getClass().getName() ), cause );
		addException( ReportingLevel.E_UNHANDLED, cause );
		return true;
	}

	/**
	 * Checks if exception is present by class name
	 *
	 * @param clz The exception to check for
	 * @return Is it present
	 */
	public boolean hasException( Class<? extends Throwable> clz )
	{
		Objs.notNull( clz );

		for ( IException e : caughtExceptions )
		{
			if ( e.getCause() != null && clz.isAssignableFrom( e.getCause().getClass() ) )
				return true;

			if ( clz.isAssignableFrom( e.getClass() ) )
				return true;
		}

		return false;
	}

	public boolean hasExceptions()
	{
		return !caughtExceptions.isEmpty();
	}

	public boolean hasIgnorableExceptions()
	{
		for ( IException e : caughtExceptions )
			if ( e.reportingLevel().isIgnorable() )
				return true;
		return false;
	}

	public boolean hasNonIgnorableExceptions()
	{
		for ( IException e : caughtExceptions )
			if ( !e.reportingLevel().isIgnorable() )
				return true;
		return false;
	}

	/*
	 * private static final ThreadLocal<Yaml> YAML_INSTANCE = new ThreadLocal<Yaml>()
	 * {
	 *
	 * @Override
	 * protected Yaml initialValue()
	 * {
	 * DumperOptions opts = new DumperOptions();
	 * opts.setDefaultFlowStyle( DumperOptions.FlowStyle.FLOW );
	 * opts.setDefaultScalarStyle( DumperOptions.ScalarStyle.DOUBLE_QUOTED );
	 * opts.setPrettyFlow( true );
	 * opts.setWidth( Integer.MAX_VALUE ); // Don't wrap scalars -- json no like
	 * return new Yaml( opts );
	 * }
	 * };
	 */
	//private static final URL GIST_POST_URL;
	/*
	 * static
	 * {
	 * try
	 * {
	 * GIST_POST_URL = new URL( "https://api.github.com/gists" );
	 * }
	 * catch ( MalformedURLException e )
	 * {
	 * throw new ExceptionInInitializerError( e );
	 * }
	 * }
	 */
}
