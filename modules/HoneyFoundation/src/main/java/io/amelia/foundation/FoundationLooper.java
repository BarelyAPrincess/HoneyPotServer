/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import io.amelia.data.parcel.ParcelReceiver;
import io.amelia.lang.ApplicationException;
import io.amelia.looper.MainLooper;
import io.amelia.looper.queue.EntryAbstract;

public class FoundationLooper extends MainLooper
{
	private final ParcelReceiver parcelReceiver;

	public FoundationLooper( ParcelReceiver parcelReceiver )
	{
		this.parcelReceiver = parcelReceiver;
	}

	@Override
	public ParcelReceiver getParcelReceiver()
	{
		return parcelReceiver;
	}

	public boolean isDisposed()
	{
		return Foundation.isRunlevel( Runlevel.DISPOSED );
	}

	@Override
	public boolean isPermitted( EntryAbstract entry )
	{
		if ( Foundation.getRunlevel().intValue() < Runlevel.MAINLOOP.intValue() && entry instanceof TaskEntry )
			throw new ApplicationException.Runtime( entry.getClass().getSimpleName() + " can only be posted to the FoundationLooper at runlevel MAINLOOP and above. Current runlevel is " + Foundation.getRunlevel() );

		// TODO Check known built-in AbstractEntry sub-classes.
		return true;
	}

	protected boolean canQuit()
	{
		return Foundation.getRunlevel().intValue() <= 100;
	}

	@Override
	protected void quit( boolean removePendingMessages )
	{
		if ( !canQuit() )
			throw ApplicationException.runtime( "FoundationLooper is not permitted to quit." );

		super.quit( removePendingMessages );
	}

	@Override
	protected void quitFinal()
	{
		// Nothing
	}

	@Override
	protected void signalInfallibleStartup()
	{
		super.signalInfallibleStartup();

		// As soon as the looper gets started, we set the runlevel appropriately.
		Foundation.setRunlevel( Runlevel.MAINLOOP );
	}
}
