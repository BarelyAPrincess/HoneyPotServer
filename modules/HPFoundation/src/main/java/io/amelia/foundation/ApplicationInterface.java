/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import io.amelia.data.parcel.ParcelInterface;
import io.amelia.data.parcel.ParcelReceiver;
import io.amelia.foundation.bindings.BindingException;
import io.amelia.foundation.bindings.Bindings;
import io.amelia.foundation.bindings.FacadeBinding;
import io.amelia.foundation.bindings.FacadePriority;
import io.amelia.foundation.bindings.WritableBinding;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionRegistrar;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.StartupException;
import io.amelia.lang.StartupInterruptException;
import io.amelia.logcompat.Logger;
import io.amelia.looper.LooperRouter;
import io.amelia.support.ConfigStorageLoader;
import io.amelia.support.Encrypt;
import io.amelia.support.EnumColor;
import io.amelia.support.Objs;
import io.amelia.support.Runlevel;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * When a {@link ApplicationInterface} is instigated, its main thread is dedicated to
 * running a looper that takes care of managing the top-level application tasks and parcels.
 */
public abstract class ApplicationInterface implements VendorRegistrar, ExceptionRegistrar, ParcelInterface, ParcelReceiver
{
	public final Thread primaryThread = Thread.currentThread();
	private final OptionParser optionParser = new OptionParser();
	private Env env = null;
	private OptionSet optionSet = null;

	public ApplicationInterface()
	{
		optionParser.acceptsAll( Arrays.asList( "?", "h", "help" ), "Show the help" );
		optionParser.acceptsAll( Arrays.asList( "v", "version" ), "Show the version" );

		optionParser.accepts( "env-file", "The env file" ).withRequiredArg().ofType( String.class ).defaultsTo( ".env" );
		optionParser.accepts( "env", "Overrides env values" ).withRequiredArg().ofType( String.class );

		optionParser.accepts( "no-banner", "Disables the banner" );

		for ( String pathKey : Kernel.getPathSlugs() )
			optionParser.accepts( "dir-" + pathKey, "Sets the " + pathKey + " directory." ).withRequiredArg().ofType( String.class );
	}

	public void addArgument( String arg, String desc )
	{
		optionParser.accepts( arg, desc );
	}

	public void addIntegerArgument( String arg, String desc )
	{
		optionParser.accepts( arg, desc ).withRequiredArg().ofType( Integer.class );
	}

	public void addStringArgument( String arg, String desc )
	{
		optionParser.accepts( arg, desc ).withRequiredArg().ofType( String.class );
	}

	public void checkOptionSet()
	{
		if ( optionSet == null )
			throw new ApplicationException.Runtime( ReportingLevel.E_ERROR, "parse( String[] ) was never called." );
	}

	void dispose()
	{
		LooperRouter.dispose();
	}

	@Override
	public void fatalError( ExceptionReport report, boolean crashOnError )
	{
		if ( crashOnError )
			Foundation.setRunlevel( Runlevel.CRASHED, "The Application has crashed!" );
	}

	public Env getEnv()
	{
		checkOptionSet();
		return env;
	}

	public String getId()
	{
		return env.getString( "instance-id" ).orElse( null );
	}

	public Optional<Integer> getIntegerArgument( String arg )
	{
		return Optional.ofNullable( optionSet.valuesOf( arg ) ).filter( l -> l.size() > 0 ).map( l -> ( Integer ) l.get( 0 ) );
	}

	public OptionParser getOptionParser()
	{
		return optionParser;
	}

	public OptionSet getOptionSet()
	{
		checkOptionSet();
		return optionSet;
	}

	public Optional<String> getStringArgument( String arg )
	{
		return Optional.ofNullable( optionSet.valuesOf( arg ) ).filter( l -> l.size() > 0 ).map( l -> ( String ) l.get( 0 ) );
	}

	public Optional<List<String>> getStringListArgument( String arg )
	{
		return Optional.ofNullable( ( List<String> ) optionSet.valuesOf( arg ) );
	}

	public VendorMeta getVendorMeta()
	{
		return new VendorMeta( new HashMap<String, String>()
		{{
			put( VendorMeta.NAME, Kernel.getDevMeta().getProductName() );
			put( VendorMeta.DESCRIPTION, Kernel.getDevMeta().getProductDescription() );
			put( VendorMeta.AUTHORS, Kernel.getDevMeta().getDeveloperName() );
			put( VendorMeta.GITHUB_BASE_URL, Kernel.getDevMeta().getGitRepoUrl() );
			put( VendorMeta.VERSION, Kernel.getDevMeta().getVersionDescribe() );
		}} );
	}

	public boolean hasArgument( String arg )
	{
		return optionSet.hasArgument( arg );
	}

	public boolean isPrimaryThread()
	{
		return primaryThread == Thread.currentThread();
	}

	@Override
	public final boolean isRemote()
	{
		return false;
	}

	public abstract void onRunlevelChange( Runlevel previousRunlevel, Runlevel currentRunlevel ) throws ApplicationException.Error;

	/**
	 * Handles internal argument options and triggers, such as
	 *
	 * @throws StartupInterruptException
	 */
	public void parse( String[] args ) throws StartupInterruptException
	{
		optionSet = optionParser.parse( args );

		if ( optionSet.has( "help" ) )
		{
			try
			{
				optionParser.printHelpOn( System.out );
			}
			catch ( IOException e )
			{
				throw new StartupException( e );
			}
			throw new StartupInterruptException();
		}

		if ( optionSet.has( "version" ) )
		{
			Foundation.L.info( Kernel.getDevMeta().getProductDescribed() );
			throw new StartupInterruptException();
		}

		try
		{
			/* Load env file -- Can be set with arg `--env-file=.env` */
			Path envFile = Paths.get( ( String ) optionSet.valueOf( "env-file" ) );
			env = new Env( envFile );

			/* Override defaults and env with command args */
			for ( OptionSpec<?> optionSpec : optionSet.specs() )
				for ( String optionKey : optionSpec.options() )
					if ( !Objs.isNull( optionSpec.value( optionSet ) ) )
					{
						if ( optionKey.startsWith( "dir-" ) )
							Kernel.setPath( optionKey.substring( 4 ), ( String ) optionSpec.value( optionSet ) );
						else if ( env.isValueSet( optionKey ) )
							env.set( optionKey, optionSpec.value( optionSet ), false );
					}

			// XXX Use Encrypt::hash as an alternative to Encrypt::uuid
			env.computeValue( "instance-id", Encrypt::uuid, true );

			ConfigRegistry.init( env, new ConfigStorageLoader() );
			Bindings.init();

			/*
			 * Register facades from configuration:
			 *
			 * {
			 *   class: "io.amelia.facades.permissionBinding",
			 *   namespace: "io.amelia.permissions.facade",
			 *   priority: NORMAL
			 * }
			 */
			ConfigMap facades = ConfigRegistry.config.getChild( Foundation.Config.BINDINGS_FACADES );
			if ( facades != null )
				facades.getChildren().forEach( each -> {
					if ( each.hasChild( "class" ) )
					{
						Class<FacadeBinding> facadeClass = each.getStringAsClass( "class", FacadeBinding.class ).orElse( null );
						FacadePriority priority = each.getEnum( "priority", FacadePriority.class ).orElse( FacadePriority.NORMAL );

						if ( facadeClass == null )
							Foundation.L.warning( "We found malformed arguments in the facade config for key -> " + each.getName() );
						else
						{
							WritableBinding binding;
							if ( each.hasChild( "namespace" ) && each.isType( "namespace", String.class ) )
								binding = Bindings.getNamespace( each.getString( "namespace" ).orElseThrow( RuntimeException::new ) ).writable();
							else
								binding = Bindings.getSystemNamespace( facadeClass ).writable();

							try
							{
								binding.registerFacadeBinding( facadeClass, () -> Objs.initClass( facadeClass ), priority );
							}
							catch ( BindingException.Error e )
							{
								Foundation.L.severe( "Failed to register facade from config for key -> " + each.getName(), e );
							}
						}
					}
					else
						Foundation.L.warning( "We found malformed arguments in the facade config for key -> " + each.getName() );
				} );
		}
		catch ( Exception e )
		{
			throw new StartupException( e );
		}
	}

	void quitSafely()
	{
		LooperRouter.quitSafely();
	}

	void quitUnsafe()
	{
		LooperRouter.quitUnsafely();
	}

	public void showBanner( Logger logger )
	{
		logger.info( EnumColor.NEGATIVE + "" + EnumColor.GOLD + "Starting " + Kernel.getDevMeta().getProductName() + " version " + Kernel.getDevMeta().getVersionDescribe() );
		logger.info( EnumColor.NEGATIVE + "" + EnumColor.GOLD + Kernel.getDevMeta().getProductCopyright() );
	}

	public void throwStartupException( Exception e ) throws StartupException
	{
		throw new StartupException( "There was a problem starting the application", e );
	}
}
