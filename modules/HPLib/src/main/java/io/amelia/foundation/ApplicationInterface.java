package io.amelia.foundation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import io.amelia.config.ConfigRegistry;
import io.amelia.env.Env;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionContext;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.Runlevel;
import io.amelia.lang.StartupException;
import io.amelia.lang.StartupInterruptException;
import io.amelia.support.IO;
import io.amelia.support.Info;
import io.amelia.support.LibEncrypt;
import io.amelia.support.Objs;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public abstract class ApplicationInterface implements VendorRegistrar, ExceptionContext
{
	public static final String PATH_APP = "__app";
	public static final String PATH_CACHE = "__cache";
	public static final String PATH_LOGS = "__logs";
	public static final String PATH_CONFIG = "__config";
	public static final String PATH_PLUGINS = "__plugins";
	public static final String PATH_UPDATES = "__updates";
	public static final String PATH_STORAGE = "__storage";
	// Main Looper runs on the main thread, i.e., the thread that started the Kernel
	private final Looper mainLooper;
	private final OptionParser optionParser = new OptionParser();
	private Env env = null;
	private OptionSet optionSet = null;

	public ApplicationInterface()
	{
		ConfigRegistry.setPath( PATH_CACHE, PATH_STORAGE, "cache" );
		ConfigRegistry.setPath( PATH_LOGS, PATH_STORAGE, "logs" );
		ConfigRegistry.setPath( PATH_CONFIG, PATH_APP, "config" );
		ConfigRegistry.setPath( PATH_PLUGINS, PATH_APP, "plugins" );
		ConfigRegistry.setPath( PATH_UPDATES, PATH_APP, "updates" );
		ConfigRegistry.setPath( PATH_STORAGE, PATH_APP, "storage" );

		mainLooper = new Looper( Looper.Flag.SYSTEM );

		optionParser.acceptsAll( Arrays.asList( "?", "h", "help" ), "Show the help" );
		optionParser.acceptsAll( Arrays.asList( "v", "version" ), "Show the version" );

		optionParser.accepts( "env-file", "The env file" ).withRequiredArg().ofType( String.class ).defaultsTo( ".env" );
		optionParser.accepts( "env", "Overrides env values" ).withRequiredArg().ofType( String.class );
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
			put( VendorMeta.NAME, Info.getProduct() );
			put( VendorMeta.DESCRIPTION, "Honey Pot Server is a modular multi-protocol networking server offering groovy scripting, plugins, ssl, events, orm, clustering, and more." );
			put( VendorMeta.AUTHORS, "Amelia DeWitt" );
			put( VendorMeta.GITHUB_BASE_URL, "https://github.com/TheAmeliaDeWitt/HoneyPotServer" );
			put( VendorMeta.VERSION, Info.getVersion() );
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
			Kernel.L.info( Info.getProductDescribe() );
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
							ConfigRegistry.setPath( optionKey.substring( 4 ), ( String ) optionSpec.value( optionSet ) );
						else if ( env.isValueSet( optionKey ) )
							env.set( optionKey, optionSpec.value( optionSet ), false );
					}

			env.computeValue( "applicationId", LibEncrypt::hash, true );

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
