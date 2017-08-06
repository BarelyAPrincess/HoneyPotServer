package io.amelia.foundation;

import io.amelia.config.ConfigRegistry;
import io.amelia.env.Env;
import io.amelia.events.EventDispatcher;
import io.amelia.events.application.RunlevelEvent;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.RunLevel;
import io.amelia.lang.StartupException;
import io.amelia.logcompat.DefaultLogFormatter;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.support.Info;
import io.amelia.support.LibEncrypt;
import io.amelia.support.LibIO;
import io.amelia.support.NoRtnFunction;
import io.amelia.support.Objs;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.security.auth.callback.Callback;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Application extends ApplicationInterface
{
	public static final EventDispatcher EVENT_DISPATCHER = new EventDispatcher();

	static
	{
		System.setProperty( "file.encoding", "utf-8" );
	}

	private final Logger L = LogBuilder.get( Application.class );
	private final Map<String, Callback> actions = new HashMap<>();
	private final Env env = new Env();
	private final OptionParser p = new OptionParser();
	private final RunlevelEvent runlevel = new RunlevelEvent();

	public Application()
	{
		Map<String, Object> defs = new HashMap<>();

		// Specify the general argument options
		p.acceptsAll( Arrays.asList( "?", "h", "help" ), "Show the help" );
		p.acceptsAll( Arrays.asList( "v", "version" ), "Show the version" );

		p.accepts( "env-file", "The env file" ).withRequiredArg().ofType( String.class ).describedAs( "file" ).defaultsTo( ".env" );
		defs.put( "app-dir", "" );
		p.accepts( "app-dir", "The application directory" ).withRequiredArg().ofType( String.class ).describedAs( "directory" );
		defs.put( "config-dir", "__app/config" );
		p.accepts( "config-dir", "The configuration directory" ).withRequiredArg().ofType( String.class ).describedAs( "directory" );
		defs.put( "plugins-dir", "__app/plugins" );
		p.accepts( "plugins-dir", "The plugin directory" ).withRequiredArg().ofType( String.class ).describedAs( "directory" );
		defs.put( "updates-dir", "__app/updates" );
		p.accepts( "updates-dir", "The updates directory" ).withRequiredArg().ofType( String.class ).describedAs( "directory" );
		defs.put( "webroot-dir", "__app/webroot" );
		p.accepts( "webroot-dir", "The webroot directory" ).withRequiredArg().ofType( String.class ).describedAs( "directory" );
		defs.put( "database-dir", "__app/database" );
		p.accepts( "database-dir", "The database directory" ).withRequiredArg().ofType( String.class ).describedAs( "directory" );
		defs.put( "storage-dir", "__app/storage" );
		p.accepts( "storage-dir", "The storage directory" ).withRequiredArg().ofType( String.class ).describedAs( "directory" );
		defs.put( "sessions-dir", "__storage/sessions" );
		p.accepts( "sessions-dir", "The sessions directory" ).withRequiredArg().ofType( String.class ).describedAs( "directory" );
		defs.put( "cache-dir", "__storage/cache" );
		p.accepts( "cache-dir", "The cache directory" ).withRequiredArg().ofType( String.class ).describedAs( "directory" );
		defs.put( "logs-dir", "__storage/logs" );
		p.accepts( "logs-dir", "The logs directory" ).withRequiredArg().ofType( String.class ).describedAs( "directory" );

		defs.put( "console-fancy", "true" );
		p.accepts( "console-fancy", "Specifies if control characters are written with console output to stylize it, e.g., fgcolor, bgcolor, bold, or inverted." );
		defs.put( "cluster-id", "honeypot" );
		p.accepts( "cluster-id", "Specifies the cluster unique identity" ).withRequiredArg().ofType( String.class );
		// instance-id is random, if not set
		p.accepts( "instance-id", "Specifies the instance unique identity" ).withRequiredArg().ofType( String.class );

		/* Set Defaults */
		env.defs( defs );
	}

	public RunLevel getLastRunLevel()
	{
		return runlevel.getLastRunLevel();
	}

	public RunLevel getRunLevel()
	{
		return runlevel.getRunLevel();
	}

	private void setRunLevel( RunLevel level ) throws ApplicationException
	{
		runlevel.setRunLevel( level );
		// TODO Throw runlevel change events
	}

	public void onArg( String arg, String desc, Callback func )
	{
		p.accepts( arg, desc );
		actions.put( arg, func );
	}

	/**
	 * Starts the application and blocks until the main loop is terminated
	 *
	 * @param args The console arguments
	 */
	public void start( String[] args )
	{
		LogBuilder.get().info( "Starting " + Info.getProduct() + " (" + Info.getVersion() + ")" );

		OptionSet optionSet;
		try
		{
			optionSet = p.parse( args );

			/* Load env file -- Can be set with arg `--env-file=.env` */
			File envFile = LibIO.buildFile( true, ( String ) optionSet.valueOf( "env-file" ) );
			env.load( new FileInputStream( envFile ) );

			/* Override defaults and env with command args */
			for ( OptionSpec<?> optionSpec : optionSet.specs() )
				for ( String optionKey : optionSpec.options() )
					if ( env.isValueSet( optionKey ) && !Objs.isNull( optionSpec.value( optionSet ) ) )
						env.set( optionKey, optionSpec.value( optionSet ) );

			if ( env.isValueSet( "instance-id" ) )
			{
				String instanceId = LibEncrypt.uuid();
				env.set( "instance-id", instanceId );

				/* Replace this with a better way of setting and saving .env file changes. Should the .env file even contain values that might need dynamic changing? */
				Properties prop = new Properties();
				prop.load( new FileInputStream( envFile ) );
				prop.setProperty( "instance-id", instanceId );
				prop.store( new FileOutputStream( envFile ), "" );
			}

			if ( optionSet.has( "help" ) )
			{
				p.printHelpOn( System.out );
				return;
			}

			if ( optionSet.has( "version" ) )
			{
				L.info( "Running " + Info.getProduct() + " version " + Info.getVersion() );
				return;
			}
		}
		catch ( OptionException ex )
		{
			throw new StartupException( "Failed to parse arguments", ex );
		}
		catch ( Exception ex )
		{
			throw new StartupException( "Failed to start application", ex );
		}

		LogBuilder.setConsoleFormatter( new DefaultLogFormatter( env.getBoolean( "console-fancy" ) ) );

		ConfigRegistry.init( env );
	}

	@Override
	public void tick( int currentTick, float averageTick )
	{

	}
}
