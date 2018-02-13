/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.foundation.Kernel;
import io.amelia.support.Objs;

/**
 * This class is used to analyze and report exceptions
 */
public class ExceptionReport
{
	private static final Kernel.Logger L = Kernel.getLogger( ExceptionReport.class );
	private static final Map<Class<? extends Throwable>, ExceptionCallback> registered = new ConcurrentHashMap<>();

	public static String printExceptions( ExceptionContext... exceptions )
	{
		return printExceptions( Arrays.stream( exceptions ) );
	}

	public static String printExceptions( Stream<ExceptionContext> exceptions )
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

	public static void throwExceptions( ExceptionContext... exceptions ) throws Exception
	{
		List<ExceptionContext> exps = new ArrayList<>();

		for ( ExceptionContext e : exceptions )
		{
			if ( !e.getReportingLevel().isIgnorable() )
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

	protected final List<ExceptionContext> contexts = new ArrayList<>();
	private boolean hasErrored = false;

	public ExceptionReport addException( ExceptionContext exception )
	{
		if ( exception != null )
			contexts.add( exception );
		return this;
	}

	public ExceptionReport addException( ReportingLevel level, String msg, Throwable throwable )
	{
		if ( throwable != null )
			if ( throwable instanceof UncaughtException )
			{
				( ( UncaughtException ) throwable ).setReportingLevel( level );
				contexts.add( ( ExceptionContext ) throwable );
			}
			else
				contexts.add( new UncaughtException( level, msg, throwable ) );
		return this;
	}

	public ExceptionReport addException( ReportingLevel level, Throwable throwable )
	{
		if ( throwable != null )
			if ( throwable instanceof UncaughtException )
			{
				( ( UncaughtException ) throwable ).setReportingLevel( level );
				contexts.add( ( ExceptionContext ) throwable );
			}
			else
				contexts.add( new UncaughtException( level, throwable ) );
		return this;
	}

	public Stream<ExceptionContext> getIgnorableExceptions()
	{
		return contexts.stream().filter( e -> e.getReportingLevel().isIgnorable() );
	}

	public Stream<ExceptionContext> getNotIgnorableExceptions()
	{
		return contexts.stream().filter( e -> !e.getReportingLevel().isIgnorable() );
	}

	/**
	 * Processes and appends the throwable to the context provided.
	 *
	 * @param cause   The exception thrown
	 * @param context The EvalContext associated with the eval request
	 *
	 * @return True if we should abort any further execution of code
	 */
	public final void handleException( @Nonnull Throwable cause, @Nonnull ExceptionRegistrar context )
	{
		if ( Objs.isNull( cause, context ) )
			return;

		/* Give an IException a chance to self-handle the exception report */
		if ( cause instanceof ExceptionContext )
		{
			// TODO Might not be desirable if a handle method was to return severe but did not provide any exception or debug information to the ExceptionReport. How can we force this behavior?
			ReportingLevel reportingLevel = ( ( ExceptionContext ) cause ).handle( this, context );
			if ( reportingLevel != null )
			{
				hasErrored = !reportingLevel.isIgnorable();
				return;
			}
		}

		/* Parse each IException and set hasErrored if one or more IExceptions produced Non-Ignorable Exceptions */
		if ( cause instanceof MultipleException )
		{
			for ( ExceptionContext e : ( ( MultipleException ) cause ).getExceptions() )
				handleException( ( Throwable ) e, context );
			return;
		}

		Map<Class<? extends Throwable>, ExceptionCallback> assignable = new HashMap<>();

		for ( Entry<Class<? extends Throwable>, ExceptionCallback> entry : registered.entrySet() )
			if ( cause.getClass().equals( entry.getKey() ) )
			{
				ReportingLevel e = entry.getValue().callback( cause, this, context );
				if ( e != null )
				{
					hasErrored = !e.isIgnorable();
					return;
				}
			}
			else if ( entry.getKey().isAssignableFrom( cause.getClass() ) )
				assignable.put( entry.getKey(), entry.getValue() );

		if ( assignable.size() == 1 )
		{
			ReportingLevel e = assignable.values().toArray( new ExceptionCallback[0] )[0].callback( cause, this, context );
			if ( e != null )
			{
				hasErrored = !e.isIgnorable();
				return;
			}
		}
		else if ( assignable.size() > 1 )
			for ( Entry<Class<? extends Throwable>, ExceptionCallback> entry : assignable.entrySet() )
			{
				for ( Class<?> iface : cause.getClass().getInterfaces() )
					if ( iface.equals( entry.getKey() ) )
					{
						ReportingLevel e = entry.getValue().callback( cause, this, context );
						if ( e != null )
						{
							hasErrored = !e.isIgnorable();
							return;
						}
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
							{
								hasErrored = !e.isIgnorable();
								return;
							}
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
		addException( ReportingLevel.E_UNHANDLED, cause );
	}

	public boolean hasErrored()
	{
		return hasErrored;
	}

	/**
	 * Checks if exception is present by class name
	 *
	 * @param clz The exception to check for
	 *
	 * @return Is it present
	 */
	public boolean hasException( Class<? extends Throwable> clz )
	{
		Objs.notNull( clz );

		for ( ExceptionContext context : contexts )
		{
			Throwable throwable = context.getThrowable();

			if ( throwable.getCause() != null && clz.isAssignableFrom( context.getThrowable().getClass() ) )
				return true;

			if ( clz.isAssignableFrom( throwable.getClass() ) )
				return true;
		}

		return false;
	}

	public boolean hasExceptions()
	{
		return !contexts.isEmpty();
	}

	public boolean hasIgnorableExceptions()
	{
		for ( ExceptionContext e : contexts )
			if ( e.getReportingLevel().isIgnorable() )
				return true;
		return false;
	}

	public boolean hasNonIgnorableExceptions()
	{
		for ( ExceptionContext e : contexts )
			if ( !e.getReportingLevel().isIgnorable() )
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
