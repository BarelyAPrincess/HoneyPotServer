package io.amelia.foundation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionContext;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.Runlevel;
import io.amelia.lang.StartupException;
import io.amelia.lang.StartupInterruptException;
import io.amelia.support.Encrypt;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public abstract class ApplicationInterface implements VendorRegistrar, ExceptionContext
{
	// Main Looper runs on the main thread, i.e., the thread that started the Kernel
	private final Looper mainLooper;
	private final OptionParser optionParser = new OptionParser();
	private Env env = null;
	private OptionSet optionSet = null;

	public ApplicationInterface()
	{
		mainLooper = new Looper( Looper.Flag.SYSTEM );

		optionParser.acceptsAll( Arrays.asList( "?", "h", "help" ), "Show the help" );
		optionParser.acceptsAll( Arrays.asList( "v", "version" ), "Show the version" );

		optionParser.accepts( "env-file", "The env file" ).withRequiredArg().ofType( String.class ).defaultsTo( ".env" );
		optionParser.accepts( "env", "Overrides env values" ).withRequiredArg().ofType( String.class );

		for ( String pathKey : App.getPathSlugs() )
			optionParser.accepts( "dir-" + pathKey, "Sets the " + pathKey + " directory." ).withRequiredArg().ofType( String.class );
	}

	public void addArgument( String arg, String desc )
	{
		optionParser.accepts( arg, desc );
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

	}

	public Env getEnv()
	{
		checkOptionSet();
		return env;
	}

	public String getId()
	{
		return env.getString( "applicationId" );
	}

	public Looper getMainLooper()
	{
		return mainLooper;
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
		return Optional.ofNullable( optionSet.valuesOf( arg ) ).map( l -> ( String ) l.get( 0 ) );
	}

	public Optional<List<String>> getStringListArgument( String arg )
	{
		return Optional.ofNullable( ( List<String> ) optionSet.valuesOf( arg ) );
	}

	public VendorMeta getVendorMeta()
	{
		return new VendorMeta( new HashMap<String, String>()
		{{
			put( VendorMeta.NAME, App.getDevMeta().getProductName() );
			put( VendorMeta.DESCRIPTION, App.getDevMeta().getProductDescription() );
			put( VendorMeta.AUTHORS, App.getDevMeta().getDeveloperName() );
			put( VendorMeta.GITHUB_BASE_URL, App.getDevMeta().getGitRepoUrl() );
			put( VendorMeta.VERSION, App.getDevMeta().getVersionDescribe() );
		}} );
	}

	public boolean hasArgument( String arg )
	{
		return optionSet.hasArgument( arg );
	}

	public boolean isMainThread()
	{
		return mainLooper.isCurrentThread();
	}

	public abstract void onRunlevelChange( Runlevel previousRunlevel, Runlevel currentRunlevel ) throws ApplicationException;

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
			Kernel.L.info( App.getDevMeta().getProductDescribed() );
			throw new StartupInterruptException();
		}

		try
		{
			/* Load env file -- Can be set with arg `--env-file=.env` */
			File envFile = IO.buildFile( true, ( String ) optionSet.valueOf( "env-file" ) );
			env = new Env( envFile );

			/* Override defaults and env with command args */
			for ( OptionSpec<?> optionSpec : optionSet.specs() )
				for ( String optionKey : optionSpec.options() )
					if ( !Objs.isNull( optionSpec.value( optionSet ) ) )
					{
						if ( optionKey.startsWith( "dir-" ) )
							App.setPath( optionKey.substring( 4 ), ( String ) optionSpec.value( optionSet ) );
						else if ( env.isValueSet( optionKey ) )
							env.set( optionKey, optionSpec.value( optionSet ), false );
					}

			env.computeValue( "applicationId", Encrypt::hash, true );

			ConfigRegistry.init( env );
		}
		catch ( Exception e )
		{
			throw new StartupException( e );
		}
	}

	void shutdown()
	{
		mainLooper.quitSafely();
	}

	public void throwStartupException( Exception e ) throws StartupException
	{
		throw new StartupException( "There was a problem starting the application", e );
	}
}
