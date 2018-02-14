package io.amelia.scripting;

import java.util.HashMap;
import java.util.Map;

public class YieldBuffer
{
	// TODO Expand for more practical uses

	private Map<String, String> yields = new HashMap<>();

	public String get( String key )
	{
		return yields.get( key );
	}

	public void set( String key, String value )
	{
		yields.put( key, value );
	}
}
