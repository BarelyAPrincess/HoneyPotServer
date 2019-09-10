package com.marchnetworks.security.device;

import com.marchnetworks.command.api.rest.DeviceRestErrorEnum;
import com.marchnetworks.command.api.rest.DeviceRestException;
import com.marchnetworks.command.api.security.DeviceSessionCoreService;
import com.marchnetworks.command.api.security.DeviceSessionException;
import com.marchnetworks.command.api.security.SamlException;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.diagnostics.metrics.MetricsTypes;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.common.utils.LockMap;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.security.saml.SecurityTokenService;
import com.marchnetworks.server.communications.http.CommandRestClient;
import com.marchnetworks.server.communications.transport.datamodel.DeviceSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceSessionHolderServiceImpl implements DeviceSessionHolderService, DeviceSessionCoreService
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceSessionHolderServiceImpl.class );

	private Map<String, DeviceSessionInfo> tokenMap = new ConcurrentHashMap();
	private LockMap deviceLocks = new LockMap();

	private SecurityTokenService securityTokenService;

	public String getNewSessionFromDevice( String deviceAddress, String deviceId ) throws DeviceSessionException
	{
		long start = System.currentTimeMillis();
		LOG.debug( "Getting new session from device {} ...", deviceAddress );

		synchronized ( deviceLocks.get( deviceId ) )
		{
			try
			{
				String tokenContent = null;
				try
				{
					tokenContent = securityTokenService.getServerSecurityToken( deviceId );
				}
				catch ( SamlException se )
				{
					LOG.warn( "Error when trying to assemble server token for device {} Details: {}", deviceId, se );
					throw new DeviceSessionException( se );
				}

				CommandRestClient restClient = new CommandRestClient( deviceId, deviceAddress, null );
				DeviceSession deviceSession = restClient.getDeviceSession( tokenContent );
				if ( deviceSession != null )
				{
					storeDeviceSessionIdForDevice( deviceId, deviceSession.getSessionId(), deviceSession.getTimeout() );
					MetricsHelper.metrics.addMinMaxAvg( MetricsTypes.DEVICE_SESSION_CREATE.getName(), System.currentTimeMillis() - start );
					return deviceSession.getSessionId();
				}
				return null;
			}
			catch ( DeviceRestException e )
			{
				LOG.warn( "Error when posting token to device Url={}. Exception: {}", deviceAddress, e.getMessage() );
				if ( e.getError() == DeviceRestErrorEnum.ERROR_UNAUTHORIZED )
				{
					String date = e.getFirstHeader( "Date" );
					long timeDelta = DateUtils.getUTCTimeFromDateString( date ) - System.currentTimeMillis();
					DeviceService deviceService = ( DeviceService ) ApplicationContextSupport.getBean( "deviceService" );
					if ( deviceService.updateDeviceTimeDelta( deviceId, timeDelta ) )
					{
						String result = getNewSessionFromDevice( deviceAddress, deviceId );
						LOG.info( "Adjusted " + deviceAddress + " time delta to " + timeDelta / 1000L + " seconds and re-posted token." );
						return result;
					}
				}
				throw new DeviceSessionException( e );
			}
		}
	}

	public String getSessionFromDevice( String deviceAddress, String deviceId ) throws DeviceSessionException
	{
		String deviceSessionId = null;

		synchronized ( deviceLocks.get( deviceId ) )
		{
			DeviceSessionInfo sessionInfo = ( DeviceSessionInfo ) tokenMap.get( deviceId );

			if ( ( sessionInfo == null ) || ( sessionInfo.getSessionExpirationTime() < System.currentTimeMillis() ) )
			{
				deviceSessionId = getNewSessionFromDevice( deviceAddress, deviceId );
			}
			else
			{
				deviceSessionId = sessionInfo.getSessionId();

				sessionInfo.extendSessionExpirationTime();
			}
		}
		return deviceSessionId;
	}

	public void extendSessionForDevice( String deviceId )
	{
		synchronized ( deviceLocks.get( deviceId ) )
		{
			DeviceSessionInfo sessionInfo = ( DeviceSessionInfo ) tokenMap.get( deviceId );
			if ( sessionInfo != null )
			{
				sessionInfo.extendSessionExpirationTime();
			}
		}
	}

	public boolean hasValidSession( String deviceId )
	{
		synchronized ( deviceLocks.get( deviceId ) )
		{
			DeviceSessionInfo sessionInfo = ( DeviceSessionInfo ) tokenMap.get( deviceId );
			return ( sessionInfo != null ) && ( sessionInfo.getSessionExpirationTime() > System.currentTimeMillis() );
		}
	}

	public void invalidateAllDeviceSessions( String deviceId )
	{
		tokenMap.remove( deviceId );
	}

	public void processDeviceUnregistered( String deviceId )
	{
		invalidateAllDeviceSessions( deviceId );
		deviceLocks.remove( deviceId );
	}

	private void storeDeviceSessionIdForDevice( String deviceId, String sessionId, int sessionTimeout )
	{
		DeviceSessionInfo sessionInfo = new DeviceSessionInfo();
		sessionInfo.setDeviceId( deviceId );
		sessionInfo.setSessionId( sessionId );
		sessionInfo.setSessionTimeoutInSeconds( sessionTimeout );

		Calendar expiration = Calendar.getInstance();
		expiration.add( 13, sessionTimeout );
		sessionInfo.setSessionExpirationTime( expiration.getTimeInMillis() );
		tokenMap.put( sessionInfo.getDeviceId(), sessionInfo );
	}

	public void setSecurityTokenService( SecurityTokenService securityTokenService )
	{
		this.securityTokenService = securityTokenService;
	}
}

