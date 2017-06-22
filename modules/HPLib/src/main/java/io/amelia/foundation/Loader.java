package io.amelia.foundation;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.RunLevel;
import joptsimple.OptionParser;

public interface Loader
{
	/**
	 * Initialize the application modules
	 *
	 * @param moduleDispatcher The ModuleDispatcher instance
	 */
	void initModules( ModuleDispatcher moduleDispatcher );

	/**
	 * Signals a RunLevel change to the implementing loader
	 *
	 * @param level The new runlevel
	 * @throws ApplicationException Thrown for direct application exception
	 */
	void onRunlevelChange( RunLevel level ) throws ApplicationException;

	/**
	 * Adds options to the deployment OptionParser
	 *
	 * @param parser The OptionParser to populate
	 */
	void populateOptionParser( OptionParser parser );
}
