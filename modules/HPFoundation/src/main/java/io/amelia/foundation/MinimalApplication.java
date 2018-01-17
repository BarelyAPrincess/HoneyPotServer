package io.amelia.foundation;

import io.amelia.foundation.parcel.ParcelCarrier;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.ParcelException;
import io.amelia.support.Runlevel;

public class MinimalApplication extends ApplicationInterface
{
	@Override
	public void fatalError( ExceptionReport report, boolean crashOnError )
	{

	}

	@Override
	public void handleParcel( ParcelCarrier parcelCarrier ) throws ParcelException.Error
	{

	}

	@Override
	public void onRunlevelChange( Runlevel previousRunlevel, Runlevel currentRunlevel ) throws ApplicationException.Error
	{

	}

	@Override
	public void sendToAll( ParcelCarrier parcel )
	{

	}
}
