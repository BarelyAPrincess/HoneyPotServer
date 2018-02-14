package io.amelia.scripting;

/**
 * Provides an interface for which the Scripting Engine to notify Scripts of events, such as exception or before execution.
 */
public interface ScriptingEvents
{
	void onAfterExecute( ScriptingContext context );

	void onBeforeExecute( ScriptingContext context );

	void onException( ScriptingContext context, Throwable throwable );
}
