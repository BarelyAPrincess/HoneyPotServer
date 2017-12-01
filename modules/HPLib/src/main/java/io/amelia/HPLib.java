package io.amelia;

public class HPLib
{
	private static boolean debugMode = false;

	public static void debug( boolean debugMode )
	{
		HPLib.debugMode = debugMode;
	}

	public static boolean isDebugModeEnabled()
	{
		return debugMode;
	}

	public static void debug( String message )
	{
		// TODO Do Class.forName() to search for common log handlers ??? Maybe

		System.out.println( "HPLib Debug: " + message );
	}

	private HPLib()
	{

	}
}
