package com.marchnetworks.command.common;

import java.util.ArrayList;
import java.util.List;

public class ExceptionChain
{
	private List<Class<Exception>> chain;

	public ExceptionChain( Class... exceptions )
	{
		chain = new ArrayList();

		for ( Class<Exception> e : exceptions )
		{
			chain.add( e );
		}
	}

	public boolean match( Exception ex )
	{
		Throwable current = ex;
		for ( Class<Exception> e : chain )
		{
			if ( ( current == null ) || ( !e.isAssignableFrom( current.getClass() ) ) )
			{
				return false;
			}
			current = current.getCause();
		}
		return true;
	}
}
