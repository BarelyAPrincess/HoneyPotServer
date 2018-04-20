package io.amelia.foundation;

import io.amelia.data.parcel.ParcelReceiver;
import io.amelia.lang.ApplicationException;
import io.amelia.looper.MainLooper;
import io.amelia.looper.queue.EntryAbstract;
import io.amelia.support.Runlevel;

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
			throw new ApplicationException.Runtime( entry.getClass().getSimpleName() + " can only be posted to the Application Looper at runlevel MAINLOOP and above. Current runlevel is " + Foundation.getRunlevel() );

		// TODO Check known built-in AbstractEntry sub-classes.
		return true;
	}

	@Override
	protected void quit( boolean removePendingMessages )
	{
		if ( Foundation.getRunlevel().intValue() > 100 )
			throw ApplicationException.runtime( "Application Looper is not permitted to quit." );

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
