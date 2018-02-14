package io.amelia.scripting;

/**
 *
 */
public interface ScriptingRegistry
{
	ScriptingEngine[] makeEngines( ScriptingContext context );
}
