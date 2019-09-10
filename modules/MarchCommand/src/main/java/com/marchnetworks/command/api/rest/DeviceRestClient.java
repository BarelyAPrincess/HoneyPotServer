package com.marchnetworks.command.api.rest;

import com.marchnetworks.command.api.metrics.ApiMetricsTypes;
import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.api.metrics.MetricsUtils;
import com.marchnetworks.command.api.security.DeviceSessionCoreService;
import com.marchnetworks.command.api.security.DeviceSessionException;
import com.marchnetworks.command.common.Base64;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.HttpUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class DeviceRestClient
{
	protected static final Logger LOG = LoggerFactory.getLogger( DeviceRestClient.class );

	private int connectTimeout = 5000;
	private int requestTimeout = 15000;
	private int maxRetry = 2;

	private String address;
	private String username;
	private String password;
	private String sessionId;
	private String deviceId;
	private boolean doAuthentication = true;
	private boolean doPeerCertValidation = true;
	private String httpProtocol = "https";

	private DeviceSessionCoreService deviceSessionCoreService;

	private MetricsCoreService metricsService;
	private int code = 0;

	public DeviceRestClient( String address, MetricsCoreService metricsService )
	{
		this.address = address;
		this.metricsService = metricsService;
	}

	public DeviceRestClient( String address, String sessionId, MetricsCoreService metricsService )
	{
		this.address = address;
		this.sessionId = sessionId;
		this.metricsService = metricsService;
		if ( sessionId == null )
		{
			doAuthentication = false;
		}
	}

	public DeviceRestClient( String address, String deviceId, DeviceSessionCoreService deviceSessionCoreService, MetricsCoreService metricsService )
	{
		this.deviceId = deviceId;
		this.address = address;
		this.deviceSessionCoreService = deviceSessionCoreService;
		this.metricsService = metricsService;
	}

	public void setAuthenticationCredentials( String login, String password )
	{
		username = login;
		this.password = password;
	}

	public DeviceResponse httpRequest( String path, String method ) throws DeviceRestException
	{
		return httpRequest( path, method, null );
	}

	public DeviceResponse httpRequest( String path, String method, String data ) throws DeviceRestException
	{
		return httpRequest( path, method, data, DeviceRestContentType.APPLICATION_JSON );
	}

	public DeviceResponse httpRequest( String path, String method, String data, DeviceRestContentType contentType ) throws DeviceRestException
	{
		return httpRequestInternal( path, method, data, contentType, null );
	}

	public DeviceResponse httpRequest( String path, String method, String data, SSLSocketFactory socketFactory ) throws DeviceRestException
	{
		return httpRequestInternal( path, method, data, DeviceRestContentType.APPLICATION_JSON, socketFactory );
	}

	public DeviceResponse httpRequest( String path, String method, InputStream data, DeviceRestContentType contentType, SSLSocketFactory socketFactory ) throws DeviceRestException
	{
		return httpRequestInternal( path, method, data, contentType, socketFactory );
	}

	private DeviceResponse httpRequestInternal( String path, String method, Object data, DeviceRestContentType contentType, SSLSocketFactory socketFactory ) throws DeviceRestException
	{
		HttpURLConnection httpConn = null;
		int retryCount = 0;
		DeviceResponse response = new DeviceResponse();
		Exception cause = null;
		code = 0;

		boolean sessionRetried = false;

		String requestURL = httpProtocol + "://" + address + path;
		String pathShort = MetricsUtils.getUrlPathShort( path );
		LOG.debug( "Agent requestURL: {} Method: {}", requestURL, method );
		for ( ; ; )
		{
			long start = System.currentTimeMillis();
			try
			{
				URL url = new URL( requestURL );
				httpConn = ( HttpURLConnection ) url.openConnection();

				if ( httpProtocol.equals( "https" ) )
				{
					HttpsURLConnection httpsConn = ( HttpsURLConnection ) httpConn;
					if ( socketFactory != null )
					{
						httpsConn.setSSLSocketFactory( socketFactory );
					}
					else if ( !doPeerCertValidation )
					{
						httpsConn.setSSLSocketFactory( HttpUtils.SOCKET_FACTORY );
					}
					httpsConn.setHostnameVerifier( HttpUtils.DO_NOT_VERIFY );
					if ( doAuthentication )
					{
						setAuthentication( httpsConn );
					}
				}

				httpConn.setRequestMethod( method );
				httpConn.setConnectTimeout( connectTimeout );
				httpConn.setReadTimeout( requestTimeout );

				if ( contentType != null )
				{
					httpConn.setRequestProperty( "Content-Type", contentType.getType() );
				}

				if ( data != null )
				{
					httpConn.setDoOutput( true );
					if ( ( data instanceof String ) )
					{
						PrintWriter out = new PrintWriter( httpConn.getOutputStream() );
						out.print( ( String ) data );
						out.close();
					}
					else if ( ( data instanceof CommandUpgradeInputStream ) )
					{
						CommandUpgradeInputStream commandInputStream = ( CommandUpgradeInputStream ) data;
						httpConn.setFixedLengthStreamingMode( commandInputStream.getContentSize() );
						CommonAppUtils.writeToOutputStream( commandInputStream, httpConn.getOutputStream() );
					}
					else
					{
						CommonAppUtils.writeToOutputStream( ( InputStream ) data, httpConn.getOutputStream() );
					}
				}

				InputStream is = null;
				try
				{
					is = httpConn.getInputStream();
					Map<String, List<String>> headers = httpConn.getHeaderFields();
					response.setResponseHeaders( headers );

					List<String> encoding = headers.get( "Content-Encoding" );
					if ( ( encoding != null ) && ( encoding.contains( "gzip" ) ) )
						is = new GZIPInputStream( is );

					byte[] byteArray = CommonAppUtils.readInputStream( is, httpConn.getContentLength() );
					response.setResponse( byteArray );
					code = httpConn.getResponseCode();
				}
				finally
				{
					if ( is != null )
					{
						is.close();
					}
				}
			}
			catch ( SocketTimeoutException ste )
			{
				metricsService.addRetryActionFailure( ApiMetricsTypes.REST_CONNECTION.getName(), pathShort, System.currentTimeMillis() - start );

				retryCount++;
				metricsService.addRetryAction( ApiMetricsTypes.REST_CONNECTION.getName(), retryCount );

				if ( retryCount >= maxRetry )
				{
					throw new DeviceRestException( "Too many retries after getting socket timeouts while waiting for data", ste );
				}
				continue;
			}
			catch ( IOException e )
			{
				try
				{
					if ( httpConn != null )
					{
						code = httpConn.getResponseCode();
					}
				}
				catch ( IOException ex )
				{
					LOG.debug( "Error while reading status code from {}, Exception: {}", requestURL, ex.getMessage() );
				}
				cause = e;
			}

			if ( code == 200 )
			{
				metricsService.addRetryActionSuccess( ApiMetricsTypes.REST_CONNECTION.getName(), pathShort, System.currentTimeMillis() - start );
				break;
			}

			metricsService.addRetryActionFailure( ApiMetricsTypes.REST_CONNECTION.getName(), pathShort, System.currentTimeMillis() - start );

			Map<String, List<String>> headers = httpConn != null ? httpConn.getHeaderFields() : null;
			String reasonHeader = httpConn != null ? httpConn.getHeaderField( "x-reason" ) : null;

			if ( code == 503 )
			{
				retryCount++;
				metricsService.addRetryAction( ApiMetricsTypes.REST_CONNECTION.getName(), retryCount );
				if ( retryCount > maxRetry )
				{
					throw new DeviceRestException( "Service Unavailable while making request to " + requestURL + " after " + maxRetry + " retries, reason: " + reasonHeader, code, headers, cause );
				}
				int retryInterval = 10000;
				String retryString = httpConn.getHeaderField( "Retry-After" );
				if ( retryString != null )
				{
					retryInterval = Integer.parseInt( retryString );
					retryInterval *= 1000;
				}
				try
				{
					Thread.sleep( retryInterval );
				}
				catch ( InterruptedException e )
				{
					LOG.warn( "Interrupted while waiting for retry: " + e.getMessage(), e.getCause() );
				}
			}
			else if ( code == 401 )
			{
				if ( deviceSessionCoreService != null )
				{
					if ( sessionRetried )
					{
						throw new DeviceRestException( "Error while making request to " + requestURL + ", session renewal already attempted, status: " + code + ", reason: " + reasonHeader, code, headers, cause );
					}
					metricsService.addCounter( ApiMetricsTypes.REST_SESSION_RENEW.getName() );
					try
					{
						sessionId = deviceSessionCoreService.getNewSessionFromDevice( address, deviceId );
					}
					catch ( DeviceSessionException e )
					{
						throw new DeviceRestException( "Unable to obtain new session while making request to " + requestURL + ", status: " + code + ", reason: " + reasonHeader, e, code );
					}
					sessionRetried = true;
				}
				else
				{
					sessionId = null;
					throw new DeviceRestException( "Error while making request to " + requestURL + " unauthorized request, status: " + code + ", reason: " + reasonHeader, code, headers, cause );
				}
			}
			else
			{
				throw new DeviceRestException( "Error while making request to " + requestURL + ", status: " + code + ", reason: " + reasonHeader, code, headers, cause );
			}
		}

		if ( sessionId == null )
		{
			String cookieString = httpConn.getHeaderField( "Set-Cookie" );
			if ( cookieString != null )
			{
				sessionId = getSessionId( cookieString, "sessionId" );
			}
		}
		return response;
	}

	public static String getSessionId( String cookie, String name )
	{
		if ( cookie == null )
		{
			return null;
		}
		String[] attributeValuePairs = cookie.split( ";" );
		for ( String attribute : attributeValuePairs )
		{
			if ( attribute.contains( name + "=" ) )
			{
				return attribute.replaceFirst( name + "=", "" );
			}
		}
		return null;
	}

	private void setAuthentication( HttpsURLConnection conn ) throws DeviceRestException
	{
		if ( deviceSessionCoreService != null )
		{
			try
			{
				sessionId = deviceSessionCoreService.getSessionFromDevice( address, deviceId );
			}
			catch ( DeviceSessionException e )
			{
				throw new DeviceRestException( "Unable to obtain session with device", e, 500 );
			}
		}
		if ( sessionId != null )
		{
			conn.setRequestProperty( "Authorization", "Session" );
			String cookie = "sessionId=" + sessionId;
			conn.setRequestProperty( "Cookie", cookie );
		}
		else if ( ( username != null ) && ( password != null ) )
		{
			String authString = username + ":" + password;
			String authEncBytes = Base64.encodeBytes( authString.getBytes() );
			conn.setRequestProperty( "Authorization", "Basic " + authEncBytes );
		}
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress( String address )
	{
		this.address = address;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public void setSessionId( String sessionId )
	{
		this.sessionId = sessionId;
	}

	public String getHttpProtocol()
	{
		return httpProtocol;
	}

	public void setHttpProtocol( String protocol )
	{
		if ( ( protocol.equals( "https" ) ) || ( protocol.equals( "http" ) ) )
		{
			httpProtocol = protocol;
		}
		else
		{
			LOG.info( "setHttpProtocol with bad parameter: " + protocol );
		}
	}

	public int getConnectionTimeout()
	{
		return connectTimeout;
	}

	public void setConnectionTimeout( int duration )
	{
		connectTimeout = duration;
	}

	public int getRequestTimeout()
	{
		return requestTimeout;
	}

	public void setRequestTimeout( int duration )
	{
		requestTimeout = duration;
	}

	public int getMaxRetry()
	{
		return maxRetry;
	}

	public void setMaxRetry( int maxRetry )
	{
		this.maxRetry = maxRetry;
	}

	public void setSessionService( DeviceSessionCoreService deviceSessionCoreService )
	{
		this.deviceSessionCoreService = deviceSessionCoreService;
	}

	public void setMetricsService( MetricsCoreService metricsService )
	{
		this.metricsService = metricsService;
	}

	public int getResponseCode()
	{
		return code;
	}

	public boolean getDoAuthentication()
	{
		return doAuthentication;
	}

	public void setDoAuthentication( boolean authentication )
	{
		doAuthentication = authentication;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public boolean getDoPeerCertValidation()
	{
		return doPeerCertValidation;
	}

	public void setDoPeerCertValidation( boolean doPeerCertValidation )
	{
		this.doPeerCertValidation = doPeerCertValidation;
	}
}
