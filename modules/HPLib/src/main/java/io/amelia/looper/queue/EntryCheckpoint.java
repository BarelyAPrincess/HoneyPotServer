/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.looper.queue;

import java.util.function.BiPredicate;

import javax.annotation.Nonnull;

import io.amelia.looper.AbstractLooper;

public class EntryCheckpoint extends AbstractEntry
{
	BiPredicate<AbstractLooper, Boolean> predicate;
	long when;

	EntryCheckpoint( @Nonnull DefaultQueue queue, @Nonnull BiPredicate<AbstractLooper, Boolean> predicate )
	{
		super( queue );
		this.predicate = predicate;
		when = queue.getLatestEntry() + 1L;
	}

	@Override
	public long getWhen()
	{
		return when;
	}

	@Override
	public void recycle()
	{
		// Still Does Nothing
	}

	@Override
	public boolean isSafe()
	{
		return true;
	}
}
