package com.marchnetworks.license;

import com.marchnetworks.common.scheduling.PeriodicTransactionalTask;
import com.marchnetworks.license.service.AppLicenseService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicensePollHandler implements PeriodicTransactionalTask
{
	private static final Logger LOG = LoggerFactory.getLogger( LicensePollHandler.class );
	private ServerLicenseBO serverLicenseBO;
	private AppLicenseService appLicenseService;
	private boolean firstPoll = true;

	public void handlePeriodicTransactionalTask()
	{
		try
		{
			serverLicenseBO.checkLicenseStateExpire( firstPoll );
			firstPoll = false;
		}
		catch ( Exception e )
		{
			LOG.error( "Error checking for expired license state: ", e );
		}

		serverLicenseBO.checkForLicenseExpiredTask();

		appLicenseService.checkGraceExpire();
		appLicenseService.checkExpiredLicenses();
	}

	public void setServerLicenseBO( ServerLicenseBO serverLicenseBO )
	{
		this.serverLicenseBO = serverLicenseBO;
	}

	public void setAppLicenseService( AppLicenseService appLicenseService )
	{
		this.appLicenseService = appLicenseService;
	}
}
