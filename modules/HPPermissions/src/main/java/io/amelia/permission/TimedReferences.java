/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission;

import io.amelia.tasks.Timings;

public class TimedReferences extends References
{
	long lifeTime;

	public TimedReferences( int lifeTime )
	{
		if ( lifeTime < 1 )
			this.lifeTime = -1;
		else
			this.lifeTime = Timings.epoch() + lifeTime;
	}

	@Override
	public TimedReferences add( References refs )
	{
		super.add( refs );
		return this;
	}

	@Override
	public TimedReferences add( String... refs )
	{
		super.add( refs );
		return this;
	}

	@Override
	public TimedReferences remove( References refs )
	{
		super.remove( refs );
		return this;
	}

	@Override
	public TimedReferences remove( String... refs )
	{
		super.remove( refs );
		return this;
	}

	public boolean isExpired()
	{
		return lifeTime > 0 && ( lifeTime - Timings.epoch() < 0 );
	}
}
