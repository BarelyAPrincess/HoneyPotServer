package com.marchnetworks.server.communications.http;

import com.marchnetworks.command.api.rest.DeviceManagementConstants;
import com.marchnetworks.command.api.rest.DeviceResponse;
import com.marchnetworks.command.api.rest.DeviceRestClient;
import com.marchnetworks.command.api.rest.DeviceRestContentType;
import com.marchnetworks.command.api.rest.DeviceRestException;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.HttpUtils;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.diagnostics.DiagnosticSettings;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.types.DeviceExceptionTypes;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.server.communications.transport.datamodel.ConfigurationEnvelope;
import com.marchnetworks.server.communications.transport.datamodel.DeviceInfo;
import com.marchnetworks.server.communications.transport.datamodel.DeviceSession;
import com.marchnetworks.shared.config.CommonConfiguration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class CommandRestClient extends DeviceRestClient
{
	public CommandRestClient( String address )
	{
		super( address, MetricsHelper.metrics );
	}

	public CommandRestClient( String deviceId, String address, String sessionId )
	{
		super( DiagnosticSettings.onGetAddress( address ), deviceId, null, MetricsHelper.metrics );
		setSessionId( sessionId );
		setHttpProtocol( DiagnosticSettings.onGetTransport( getHttpProtocol() ) );

		CommonConfiguration configuration = ( CommonConfiguration ) ApplicationContextSupport.getBean( "commonConfiguration" );
		setConnectionTimeout( configuration.getIntProperty( ConfigProperty.HTTP_CLIENT_SOCKET_CONNECTION_TIMEOUT, 5000 ) );
		setRequestTimeout( configuration.getIntProperty( ConfigProperty.HTTP_CLIENT_SOCKET_DATA_TIMEOUT, 15000 ) );
		setMaxRetry( configuration.getIntProperty( ConfigProperty.HTTP_CLIENT_MAX_RETRIES, 2 ) );
	}

	public DeviceInfo getDeviceInfo( boolean https ) throws DeviceException
	{
		String path = "/info";

		if ( !https )
		{
			setHttpProtocol( "http" );
		}

		String response = null;
		try
		{
			response = httpRequest( path, "GET" ).getResponseAsString();
			if ( CommonAppUtils.isNullOrEmptyString( response ) )
			{
				return null;
			}
		}
		catch ( DeviceRestException e )
		{
			DeviceException dex = convertToDeviceException( e );
			throw dex;
		}
		return new DeviceInfo( response );
	}

	public long getDeviceTime() throws DeviceException
	{
		String path = "/Ping";

		try
		{
			DeviceResponse response = httpRequest( path, "GET" );
			String date = response.getFirstHeader( "Date" );
			if ( date != null )
			{
				return DateUtils.getUTCTimeFromDateString( date );
			}
			return 0L;
		}
		catch ( DeviceRestException e )
		{
			DeviceException dex = convertToDeviceException( e );
			throw dex;
		}
	}

	public String getDeviceSessionWithESMToken( String esmSecurityToken ) throws DeviceException
	{
		String tokenContent = "<AccessToken type=\"R5ESM\">".concat( esmSecurityToken ).concat( "</AccessToken>" );
		String path = "/tokens/local";
		String method = "POST";

		setDoAuthentication( false );
		setDoPeerCertValidation( false );
		try
		{
			httpRequest( path, method, tokenContent, DeviceRestContentType.TEXT_PLAIN );
		}
		catch ( DeviceRestException e )
		{
			DeviceException dex = convertToDeviceException( e );
			throw dex;
		}
		return getSessionId();
	}

	public DeviceSession getDeviceSession( String cesSecurityToken ) throws DeviceRestException
	{
		DeviceResponse response = httpRequest( "/tokens/local", "POST", cesSecurityToken, DeviceRestContentType.APPLICATION_XML );
		String deviceSession = DeviceManagementConstants.parseDeviceCookieHeader( response.getFirstHeader( "Set-Cookie" ) );
		LOG.debug( "Acquired new session {} for device {}", deviceSession, getAddress() );
		int timeout = 300;
		String timeoutHeader = response.getFirstHeader( "x-session-duration" );
		if ( !CommonAppUtils.isNullOrEmptyString( timeoutHeader ) )
		{
			timeout = Integer.parseInt( timeoutHeader );
		}
		return new DeviceSession( deviceSession, timeout );
	}

	public ConfigurationEnvelope getDeviceConfiguration() throws DeviceException
	{
		return getDeviceConfiguration( null );
	}

	public ConfigurationEnvelope getDeviceConfiguration( String childDeviceId ) throws DeviceException
	{
		String url = null;
		if ( CommonAppUtils.isNullOrEmptyString( childDeviceId ) )
		{
			url = "/device/config";
		}
		else
		{
			url = "/device/" + childDeviceId + "/" + "config";
		}
		try
		{
			DeviceResponse response = httpRequest( url, "GET", null, BandwidthCappedSSLSocketFactory.FACTORY_INSTANCE );
			ConfigurationEnvelope configEnvelope = new ConfigurationEnvelope();
			configEnvelope.setHash( response.getFirstHeader( "x-hash" ) );
			configEnvelope.setDocument( response.getResponse() );
			if ( childDeviceId != null )
			{
				configEnvelope.setDeviceId( childDeviceId );
			}
			else
			{
				configEnvelope.setDeviceId( getDeviceId() );
			}
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "retrieved Configuration from device: {} length: {} bytes.", childDeviceId != null ? childDeviceId : getDeviceId(), Integer.valueOf( configEnvelope.getDocument().length ) );
			}
			if ( LOG.isTraceEnabled() )
			{
				LOG.trace( new String( configEnvelope.getDocument() ) );
			}
			return configEnvelope;
		}
		catch ( DeviceRestException e )
		{
			DeviceException dex = convertToDeviceException( e );
			throw dex;
		}
	}

	public String setDeviceConfiguration( byte[] configuration ) throws DeviceException
	{
		return setDeviceConfiguration( null, configuration );
	}

	public String setDeviceConfiguration( String childDeviceId, byte[] configuration ) throws DeviceException
	{
		String url = null;
		if ( CommonAppUtils.isNullOrEmptyString( childDeviceId ) )
		{
			url = "/device/config";
		}
		else
		{
			url = "/device/" + childDeviceId + "/" + "config";
		}
		try
		{
			if ( LOG.isTraceEnabled() )
			{
				LOG.trace( new String( configuration ) );
			}

			DeviceResponse response = httpRequest( url, "POST", new ByteArrayInputStream( configuration ), DeviceRestContentType.APPLICATION_OCTET_STREAM, BandwidthCappedSSLSocketFactory.FACTORY_INSTANCE );
			return response.getFirstHeader( "x-id" );
		}
		catch ( DeviceRestException e )
		{
			LOG.debug( "CommandRestClient setDeviceConfiguration DeviceRestException: {}  and error code: {} ", e.getMessage(), e.getError() );
			DeviceException dex = convertToDeviceException( e );
			LOG.debug( "CommandRestClient setDeviceConfiguration DeviceException IsCommunicationError: {} ", Boolean.valueOf( dex.isCommunicationError() ) );
			throw dex;
		}
	}

	public String sendDeviceUpgrade( String fileName, InputStream fileContent, String key ) throws DeviceException
	{
		String url = "/device/upgrade?name=" + fileName;
		if ( !CommonAppUtils.isNullOrEmptyString( key ) )
		{
			url = url + "&key=" + HttpUtils.encodeUrlParam( key );
		}

		try
		{
			DeviceResponse response = httpRequest( url, "POST", fileContent, DeviceRestContentType.APPLICATION_OCTET_STREAM, BandwidthCappedSSLSocketFactory.FACTORY_INSTANCE );
			return response.getFirstHeader( "x-id" );
		}
		catch ( DeviceRestException e )
		{
			DeviceException dex = convertToDeviceException( e );
			throw dex;
		}
	}

	public String sendDeviceUpgrade( String childDeviceId, String fileName, InputStream fileContent, String key ) throws DeviceException
	{
		String url = "/device/" + childDeviceId + "/" + "upgrade" + "?" + "name" + "=" + fileName;

		if ( !CommonAppUtils.isNullOrEmptyString( key ) )
		{
			url = url + "&key=" + HttpUtils.encodeUrlParam( key );
		}

		try
		{
			DeviceResponse response = httpRequest( url, "POST", fileContent, DeviceRestContentType.APPLICATION_OCTET_STREAM, BandwidthCappedSSLSocketFactory.FACTORY_INSTANCE );
			return response.getFirstHeader( "x-id" );
		}
		catch ( DeviceRestException e )
		{
			DeviceException dex = convertToDeviceException( e );
			throw dex;
		}
	}

	public String sendDeviceUpgrade( List<String> childDeviceIds, String fileName, InputStream fileContent, String key ) throws DeviceException
	{
		String channelIdsStr = CollectionUtils.collectionToString( childDeviceIds, "," );
		String url = "/device/channel/upgrade?channels=" + channelIdsStr + "&" + "name" + "=" + fileName;

		try
		{
			DeviceResponse response = httpRequest( url, "POST", fileContent, DeviceRestContentType.APPLICATION_OCTET_STREAM, BandwidthCappedSSLSocketFactory.FACTORY_INSTANCE );
			return response.getFirstHeader( "x-id" );
		}
		catch ( DeviceRestException e )
		{
			DeviceException dex = convertToDeviceException( e );
			throw dex;
		}
	}

	public void closeDeviceSession() throws DeviceException
	{
		String url = "/Logout";
		try
		{
			httpRequest( url, "GET" );
		}
		catch ( DeviceRestException e )
		{
			DeviceException dex = convertToDeviceException( e );
			throw dex;
		}
	}

	private DeviceException convertToDeviceException( DeviceRestException e )
	{
		DeviceException dex = new DeviceException( e.getMessage(), e );
		switch ( e.getError() )
		{
			case ERROR_UNAUTHORIZED:
				dex.setCommunicationError( false );
				dex.setDetailedErrorType( DeviceExceptionTypes.NOT_AUTHORIZED );
				break;
			case ERROR_SOCKET_TIMEOUT:
				dex.setCommunicationError( true );
				dex.setDetailedErrorType( DeviceExceptionTypes.COMMUNICATION_TIMEOUT );
				break;
			case ERROR_SSL_HANDSHAKE:
				dex.setCommunicationError( true );
				dex.setDetailedErrorType( DeviceExceptionTypes.DEVICE_CERTIFICATE_NOT_TRUSTED );
				break;
			default:
				dex.setCommunicationError( false );
				dex.setDetailedErrorType( DeviceExceptionTypes.UNKNOWN );
		}
		return dex;
	}
}

