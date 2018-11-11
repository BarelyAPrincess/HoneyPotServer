/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.tasks;

import io.amelia.foundation.RegistrarBase;

class AsyncTaskDebugger
{
	private final Class<? extends CallableTask> clazz;
	private final RegistrarBase creator;
	private final long expiry;
	private AsyncTaskDebugger next = null;

	AsyncTaskDebugger( final long expiry, final RegistrarBase creator, final Class<? extends CallableTask> clazz )
	{
		this.expiry = expiry;
		this.creator = creator;
		this.clazz = clazz;

	}

	StringBuilder debugTo( final StringBuilder string )
	{
		for ( AsyncTaskDebugger next = this; next != null; next = next.next )
		{
			string.append( next.creator.getName() ).append( ':' ).append( next.clazz.getName() ).append( '@' ).append( next.expiry ).append( ',' );
		}
		return string;
	}

	final AsyncTaskDebugger getNextHead( final long time )
	{
		AsyncTaskDebugger next, current = this;
		while ( time > current.expiry && ( next = current.next ) != null )
			current = next;
		return current;
	}

	final AsyncTaskDebugger setNext( final AsyncTaskDebugger next )
	{
		return this.next = next;
	}
}
