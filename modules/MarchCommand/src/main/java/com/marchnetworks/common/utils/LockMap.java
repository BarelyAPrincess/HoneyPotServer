package com.marchnetworks.common.utils;

import java.util.HashMap;
import java.util.Map;

public class LockMap
{
	private Map<String, Object> locks = new HashMap<>();

	public Object get( String key )
	{
		synchronized ( locks )
		{
			Object result = locks.get( key );
			if ( result == null )
			{
				result = new Object();
				locks.put( key, result );
			}

			return result;
		}
	}

	public void remove( String key )
	{
		synchronized ( locks )
		{
			locks.remove( key );
		}
	}
}
