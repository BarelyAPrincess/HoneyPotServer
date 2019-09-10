package com.marchnetworks.license.task;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.license.DeviceLicenseBO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendDeviceLicenseTask implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( SendDeviceLicenseTask.class );

	protected Long m_DeviceId;
	protected DeviceLicenseBO m_DeviceLicenseBO;

	public SendDeviceLicenseTask( Long deviceId )
	{
		m_DeviceId = deviceId;
		m_DeviceLicenseBO = ( ( DeviceLicenseBO ) ApplicationContextSupport.getBean( "deviceLicenseBO" ) );
	}

	public void run()
	{
		LOG.debug( "ReSyncing deviceId=" + m_DeviceId + " license" );

		if ( !m_DeviceLicenseBO.resendDeviceLicense( m_DeviceId ) )
		{
			try
			{
				Thread.sleep( 1000L );
			}
			catch ( InterruptedException localInterruptedException )
			{
			}

			if ( !m_DeviceLicenseBO.resendDeviceLicense( m_DeviceId ) )
			{
				LOG.warn( "Couldn't ReSyncDeviceLicense for deviceId=" + m_DeviceId );
			}
		}
	}
}
