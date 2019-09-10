package com.marchnetworks.server.communications.soap;

import com.marchnetworks.command.api.security.DeviceSessionException;
import com.marchnetworks.command.common.HttpUtils;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.diagnostics.DiagnosticSettings;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.diagnostics.metrics.MetricsTypes;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.device_ws.DeviceService;
import com.marchnetworks.device_ws.DeviceServiceSoap;
import com.marchnetworks.security.device.DeviceSessionHolderService;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.ws.BindingProvider;

public class SoapProxyInvocationHandler implements InvocationHandler
{
	private static final Logger LOG = LoggerFactory.getLogger( SoapProxyInvocationHandler.class );

	static final String CONFIGURE_METHOD = "configureTransport";

	static final long PORT_EVICTION_TIME = 600000L;

	static final long PORT_EVICTABLE_TIME = 1800000L;

	public static final DeviceService SERVICE = new DeviceService();
	private static final GenericKeyedObjectPool PORT_POOL;

	static
	{
		PoolableServicePortFactory portFactory = new PoolableServicePortFactory();
		PORT_POOL = new GenericKeyedObjectPool( portFactory );
		PORT_POOL.setMaxActive( -1 );
		PORT_POOL.setMaxIdle( 1 );
		PORT_POOL.setTimeBetweenEvictionRunsMillis( 600000L );
		PORT_POOL.setMinEvictableIdleTimeMillis( 1800000L );
	}

	private static final URL WSDL_URL = SERVICE.getWSDLDocumentLocation();
	private URL serviceURL;
	private String deviceId;

	public static void initializePorts( List<String> deviceUrls )
	{
		long start = System.currentTimeMillis();
		for ( String url : deviceUrls )
		{
			try
			{
				PortHolder port = ( PortHolder ) PORT_POOL.borrowObject( url );
				port.getPort();
				PORT_POOL.returnObject( url, port );
			}
			catch ( Exception e )
			{
				LOG.error( "Error while initializing soap ports, Exception:" + e.getMessage() );
			}
		}
		LOG.info( "Initialized soap ports in " + ( System.currentTimeMillis() - start ) + " ms." );
	}

	private int maxRetry = 0;
	private Map<String, Object> configuration;

	public SoapProxyInvocationHandler( URL deviceURL, String deviceId, int maxRetry, Map<String, Object> additionalConfigMap )
	{
		configureTransport( deviceURL, deviceId, maxRetry, additionalConfigMap );
	}

	private void configureSoapClient( DeviceServiceSoap port )
	{
		BindingProvider bindingProvider = ( BindingProvider ) port;

		Map<String, Object> req_ctx = bindingProvider.getRequestContext();

		req_ctx.put( "com.sun.xml.ws.connect.timeout", configuration.get( ConfigProperty.HTTP_CLIENT_SOCKET_CONNECTION_TIMEOUT.getXmlName() ) );
		req_ctx.put( "com.sun.xml.ws.request.timeout", configuration.get( ConfigProperty.HTTP_CLIENT_SOCKET_DATA_TIMEOUT.getXmlName() ) );

		req_ctx.put( "com.sun.xml.ws.transport.https.client.hostname.verifier", HttpUtils.DO_NOT_VERIFY );

		String username = ( String ) configuration.get( "admin" );
		req_ctx.put( "javax.xml.ws.security.auth.username", username );

		String password = ( String ) configuration.get( "adminPassword" );
		req_ctx.put( "javax.xml.ws.security.auth.password", password );

		boolean useTrustedCommunication = isUsingTrustedCommunication();

		SSLSocketFactory socketFactoryBind = ( SSLSocketFactory ) req_ctx.get( "com.sun.xml.ws.transport.https.client.SSLSocketFactory" );
		if ( useTrustedCommunication )
		{

			if ( socketFactoryBind == null )
			{
				req_ctx.put( "com.sun.xml.ws.transport.https.client.SSLSocketFactory", HttpUtils.SOCKET_FACTORY );
			}

		}
		else if ( socketFactoryBind != null )
		{
			req_ctx.put( "com.sun.xml.ws.transport.https.client.SSLSocketFactory", null );
		}

		Map<String, List<String>> map = new HashMap();

		String tokenContent = ( String ) configuration.get( "securityToken" );
		if ( tokenContent != null )
		{
			String cookie = "sessionId=" + tokenContent;
			map.put( "Cookie", Collections.singletonList( cookie ) );
		}

		String keepAliveTimeout = ( String ) configuration.get( "keepAliveTimeout" );
		if ( keepAliveTimeout == null )
		{
			keepAliveTimeout = "300";
		}

		map.put( "X-Keep-Alive", Collections.singletonList( keepAliveTimeout ) );
		req_ctx.put( "javax.xml.ws.http.request.headers", map );

		String url = DiagnosticSettings.onGetSoapAddress( serviceURL.toString() );
		req_ctx.put( "javax.xml.ws.service.endpoint.address", url );
	}

	private void configureTransport( URL deviceURL, String deviceId, int maxRetry, Map<String, Object> configuration )
	{
		this.maxRetry = maxRetry;
		this.configuration = configuration;
		this.deviceId = deviceId;

		try
		{
			serviceURL = new URL( deviceURL.getProtocol(), deviceURL.getHost(), deviceURL.getPort(), WSDL_URL.getPath() );
		}
		catch ( MalformedURLException e )
		{
			LOG.warn( "Invalid service URL", e );
		}
	}

	private static Class<?> translatePrimitiveClass( Class<?> clazz )
	{
		if ( clazz == Integer.class )
		{
			return Integer.TYPE;
		}
		if ( clazz == Double.class )
		{
			return Double.TYPE;
		}
		if ( clazz == Boolean.class )
		{
			return Boolean.TYPE;
		}
		return clazz;
	}

	private int getHeaderIntValue( List<String> headerList, String key )
	{
		int result = -1;
		try
		{
			for ( String header : headerList )
			{
				if ( header.contains( key ) )
				{
					int index = header.indexOf( ":" );
					if ( index != -1 )
					{
						String value = header.substring( index + 2 );
						result = Integer.parseInt( value );
					}
				}
			}
		}
		catch ( NumberFormatException localNumberFormatException )
		{
		}
		return result;
	}

	private boolean isUnauthorizedException( Throwable e )
	{
		if ( e == null )
		{
			return false;
		}
		return e.getClass().isAssignableFrom( SocketException.class );
	}

	private void resetSessionForRetry( DeviceServiceSoap soapClientProxy ) throws DeviceSessionException
	{
		LOG.info( "Renewing session since {} was unauthorized with Device {} to retry Soap Request...", configuration.get( "securityToken" ), serviceURL.toExternalForm() );

		MetricsHelper.metrics.addCounter( MetricsTypes.SOAP_SESSION_RENEW.getName() );

		DeviceSessionHolderService deviceSessionHolder = ( DeviceSessionHolderService ) ApplicationContextSupport.getBean( "deviceSessionHolderService" );
		String sessionId = null;

		String deviceAddress = getAddress();
		LOG.debug( "DeviceAddress for session renewal: {}", deviceAddress );

		sessionId = deviceSessionHolder.getNewSessionFromDevice( deviceAddress, deviceId );

		configuration.put( "securityToken", sessionId );
		configuration.put( "attempted", Boolean.TRUE );
		configureSoapClient( soapClientProxy );
	}

	private String getResponseCookie( DeviceServiceSoap port )
	{
		return getFirstHeader( port, "Set-Cookie" );
	}

	private String getFirstHeader( DeviceServiceSoap port, String headerName )
	{
		BindingProvider bindingProvider = ( BindingProvider ) port;
		Map<String, Object> resp_ctx = bindingProvider.getResponseContext();

		Map<String, List<String>> headers = ( Map ) resp_ctx.get( "javax.xml.ws.http.response.headers" );
		List<String> headerValues = ( List ) headers.get( headerName );
		if ( ( headerValues != null ) && ( !headerValues.isEmpty() ) )
		{
			return ( String ) headerValues.get( 0 );
		}
		return null;
	}

	private boolean isUsingTrustedCommunication()
	{
		boolean result = false;

		if ( configuration.get( "useTrusted" ) != null )
		{
			result = ( ( Boolean ) configuration.get( "useTrusted" ) ).booleanValue();
		}
		return result;
	}

	private String getAddress()
	{
		return serviceURL.getHost() + ":" + serviceURL.getPort();
	}

	public int getMaxRetry()
	{
		return maxRetry;
	}

	public void setMaxRetry( int maxRetry )
	{
		this.maxRetry = maxRetry;
	}

	/* Error */
	public Object invoke( Object proxy, java.lang.reflect.Method method, Object[] args ) throws Throwable
	{
		throw new IllegalStateException( "NOT DECOMPILED!" );

		// Byte code:
		//   0: iconst_0
		//   1: istore 4
		//   3: aconst_null
		//   4: astore 5
		//   6: aconst_null
		//   7: astore 6
		//   9: aload_0
		//   10: invokespecial 61	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:getAddress	()Ljava/lang/String;
		//   13: astore 7
		//   15: aload_2
		//   16: invokevirtual 62	java/lang/reflect/Method:getName	()Ljava/lang/String;
		//   19: ldc 63
		//   21: invokevirtual 64	java/lang/String:equals	(Ljava/lang/Object;)Z
		//   24: ifeq +36 -> 60
		//   27: aload_0
		//   28: aload_3
		//   29: iconst_0
		//   30: aaload
		//   31: checkcast 65	java/net/URL
		//   34: aload_3
		//   35: iconst_1
		//   36: aaload
		//   37: checkcast 5	java/lang/String
		//   40: aload_3
		//   41: iconst_2
		//   42: aaload
		//   43: checkcast 66	java/lang/Integer
		//   46: invokevirtual 67	java/lang/Integer:intValue	()I
		//   49: aload_3
		//   50: iconst_3
		//   51: aaload
		//   52: checkcast 68	java/util/Map
		//   55: invokespecial 26	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:configureTransport	(Ljava/net/URL;Ljava/lang/String;ILjava/util/Map;)V
		//   58: aconst_null
		//   59: areturn
		//   60: getstatic 12	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:LOG	Lorg/slf4j/Logger;
		//   63: new 13	java/lang/StringBuilder
		//   66: dup
		//   67: invokespecial 14	java/lang/StringBuilder:<init>	()V
		//   70: ldc 69
		//   72: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   75: aload_2
		//   76: invokevirtual 62	java/lang/reflect/Method:getName	()Ljava/lang/String;
		//   79: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   82: ldc 70
		//   84: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   87: aload_0
		//   88: getfield 57	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:serviceURL	Ljava/net/URL;
		//   91: invokevirtual 71	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
		//   94: invokevirtual 18	java/lang/StringBuilder:toString	()Ljava/lang/String;
		//   97: invokeinterface 72 2 0
		//   102: getstatic 6	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:PORT_POOL	Lorg/apache/commons/pool/impl/GenericKeyedObjectPool;
		//   105: aload 7
		//   107: invokevirtual 7	org/apache/commons/pool/impl/GenericKeyedObjectPool:borrowObject	(Ljava/lang/Object;)Ljava/lang/Object;
		//   110: checkcast 8	com/marchnetworks/server/communications/soap/PortHolder
		//   113: astore 6
		//   115: aload 6
		//   117: invokevirtual 73	com/marchnetworks/server/communications/soap/PortHolder:isInitialized	()Z
		//   120: ifne +17 -> 137
		//   123: getstatic 74	com/marchnetworks/common/diagnostics/metrics/MetricsHelper:metrics	Lcom/marchnetworks/command/api/metrics/MetricsCoreService;
		//   126: getstatic 75	com/marchnetworks/common/diagnostics/metrics/MetricsTypes:SOAP_PORT_CREATE	Lcom/marchnetworks/common/diagnostics/metrics/MetricsTypes;
		//   129: invokevirtual 76	com/marchnetworks/common/diagnostics/metrics/MetricsTypes:getName	()Ljava/lang/String;
		//   132: invokeinterface 77 2 0
		//   137: aload 6
		//   139: invokevirtual 9	com/marchnetworks/server/communications/soap/PortHolder:getPort	()Lcom/marchnetworks/device_ws/DeviceServiceSoap;
		//   142: astore 5
		//   144: goto +51 -> 195
		//   147: astore 8
		//   149: getstatic 6	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:PORT_POOL	Lorg/apache/commons/pool/impl/GenericKeyedObjectPool;
		//   152: aload 7
		//   154: aload 6
		//   156: invokevirtual 78	org/apache/commons/pool/impl/GenericKeyedObjectPool:invalidateObject	(Ljava/lang/Object;Ljava/lang/Object;)V
		//   159: aconst_null
		//   160: astore 6
		//   162: new 79	com/marchnetworks/common/transport/datamodel/DeviceException
		//   165: dup
		//   166: new 13	java/lang/StringBuilder
		//   169: dup
		//   170: invokespecial 14	java/lang/StringBuilder:<init>	()V
		//   173: ldc 80
		//   175: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   178: aload 8
		//   180: invokevirtual 17	java/lang/Exception:getMessage	()Ljava/lang/String;
		//   183: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   186: invokevirtual 18	java/lang/StringBuilder:toString	()Ljava/lang/String;
		//   189: aload 8
		//   191: invokespecial 81	com/marchnetworks/common/transport/datamodel/DeviceException:<init>	(Ljava/lang/String;Ljava/lang/Throwable;)V
		//   194: athrow
		//   195: aload_0
		//   196: aload 5
		//   198: invokespecial 82	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:configureSoapClient	(Lcom/marchnetworks/device_ws/DeviceServiceSoap;)V
		//   201: lconst_0
		//   202: lstore 8
		//   204: iconst_0
		//   205: istore 10
		//   207: aload_3
		//   208: ifnull +7 -> 215
		//   211: aload_3
		//   212: arraylength
		//   213: istore 10
		//   215: iload 10
		//   217: anewarray 83	java/lang/Object
		//   220: astore 11
		//   222: iload 10
		//   224: anewarray 84	java/lang/Class
		//   227: astore 12
		//   229: iconst_0
		//   230: istore 13
		//   232: iload 13
		//   234: iload 10
		//   236: if_icmpge +37 -> 273
		//   239: aload 11
		//   241: iload 13
		//   243: aload_3
		//   244: iload 13
		//   246: aaload
		//   247: invokestatic 85	com/marchnetworks/server/communications/soap/SoapTransportConverter:convertToSOAP	(Ljava/lang/Object;)Ljava/lang/Object;
		//   250: aastore
		//   251: aload 12
		//   253: iload 13
		//   255: aload 11
		//   257: iload 13
		//   259: aaload
		//   260: invokevirtual 86	java/lang/Object:getClass	()Ljava/lang/Class;
		//   263: invokestatic 87	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:translatePrimitiveClass	(Ljava/lang/Class;)Ljava/lang/Class;
		//   266: aastore
		//   267: iinc 13 1
		//   270: goto -38 -> 232
		//   273: ldc_w 88
		//   276: aload_2
		//   277: invokevirtual 62	java/lang/reflect/Method:getName	()Ljava/lang/String;
		//   280: aload 12
		//   282: invokevirtual 89	java/lang/Class:getMethod	(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
		//   285: astore 13
		//   287: invokestatic 1	java/lang/System:currentTimeMillis	()J
		//   290: lstore 8
		//   292: aload 13
		//   294: aload 5
		//   296: aload 11
		//   298: invokevirtual 90	java/lang/reflect/Method:invoke	(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
		//   301: astore 14
		//   303: aload_0
		//   304: invokespecial 43	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:isUsingTrustedCommunication	()Z
		//   307: ifeq +37 -> 344
		//   310: aload_0
		//   311: aload 5
		//   313: invokespecial 91	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:getResponseCookie	(Lcom/marchnetworks/device_ws/DeviceServiceSoap;)Ljava/lang/String;
		//   316: astore 15
		//   318: aload 15
		//   320: invokestatic 92	com/marchnetworks/command/api/rest/DeviceManagementConstants:parseDeviceCookieHeader	(Ljava/lang/String;)Ljava/lang/String;
		//   323: astore 16
		//   325: aload 16
		//   327: ifnull +17 -> 344
		//   330: aload_0
		//   331: getfield 30	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:configuration	Ljava/util/Map;
		//   334: ldc 49
		//   336: aload 16
		//   338: invokeinterface 34 3 0
		//   343: pop
		//   344: aload 14
		//   346: invokestatic 93	com/marchnetworks/server/communications/soap/SoapTransportConverter:convertToModel	(Ljava/lang/Object;)Ljava/lang/Object;
		//   349: astore 15
		//   351: getstatic 74	com/marchnetworks/common/diagnostics/metrics/MetricsHelper:metrics	Lcom/marchnetworks/command/api/metrics/MetricsCoreService;
		//   354: getstatic 94	com/marchnetworks/common/diagnostics/metrics/MetricsTypes:SOAP_CONNECTION	Lcom/marchnetworks/common/diagnostics/metrics/MetricsTypes;
		//   357: invokevirtual 76	com/marchnetworks/common/diagnostics/metrics/MetricsTypes:getName	()Ljava/lang/String;
		//   360: aload_2
		//   361: invokevirtual 62	java/lang/reflect/Method:getName	()Ljava/lang/String;
		//   364: invokestatic 1	java/lang/System:currentTimeMillis	()J
		//   367: lload 8
		//   369: lsub
		//   370: invokeinterface 95 5 0
		//   375: aload 15
		//   377: astore 16
		//   379: aconst_null
		//   380: aload 6
		//   382: if_acmpeq +13 -> 395
		//   385: getstatic 6	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:PORT_POOL	Lorg/apache/commons/pool/impl/GenericKeyedObjectPool;
		//   388: aload 7
		//   390: aload 6
		//   392: invokevirtual 10	org/apache/commons/pool/impl/GenericKeyedObjectPool:returnObject	(Ljava/lang/Object;Ljava/lang/Object;)V
		//   395: aload 16
		//   397: areturn
		//   398: astore 10
		//   400: aload 10
		//   402: invokevirtual 97	java/lang/reflect/InvocationTargetException:getCause	()Ljava/lang/Throwable;
		//   405: astore 11
		//   407: new 79	com/marchnetworks/common/transport/datamodel/DeviceException
		//   410: dup
		//   411: aload 11
		//   413: invokevirtual 98	java/lang/Throwable:getMessage	()Ljava/lang/String;
		//   416: aload 11
		//   418: invokespecial 81	com/marchnetworks/common/transport/datamodel/DeviceException:<init>	(Ljava/lang/String;Ljava/lang/Throwable;)V
		//   421: astore 12
		//   423: getstatic 74	com/marchnetworks/common/diagnostics/metrics/MetricsHelper:metrics	Lcom/marchnetworks/command/api/metrics/MetricsCoreService;
		//   426: getstatic 94	com/marchnetworks/common/diagnostics/metrics/MetricsTypes:SOAP_CONNECTION	Lcom/marchnetworks/common/diagnostics/metrics/MetricsTypes;
		//   429: invokevirtual 76	com/marchnetworks/common/diagnostics/metrics/MetricsTypes:getName	()Ljava/lang/String;
		//   432: aload_2
		//   433: invokevirtual 62	java/lang/reflect/Method:getName	()Ljava/lang/String;
		//   436: invokestatic 1	java/lang/System:currentTimeMillis	()J
		//   439: lload 8
		//   441: lsub
		//   442: invokeinterface 99 5 0
		//   447: aload 11
		//   449: instanceof 100
		//   452: ifeq +6 -> 458
		//   455: aload 12
		//   457: athrow
		//   458: aload 11
		//   460: instanceof 101
		//   463: ifne +11 -> 474
		//   466: aload 11
		//   468: instanceof 102
		//   471: ifeq +108 -> 579
		//   474: aload 11
		//   476: invokevirtual 103	java/lang/Throwable:getCause	()Ljava/lang/Throwable;
		//   479: astore 13
		//   481: aload 13
		//   483: instanceof 104
		//   486: ifeq +59 -> 545
		//   489: iinc 4 1
		//   492: getstatic 74	com/marchnetworks/common/diagnostics/metrics/MetricsHelper:metrics	Lcom/marchnetworks/command/api/metrics/MetricsCoreService;
		//   495: getstatic 94	com/marchnetworks/common/diagnostics/metrics/MetricsTypes:SOAP_CONNECTION	Lcom/marchnetworks/common/diagnostics/metrics/MetricsTypes;
		//   498: invokevirtual 76	com/marchnetworks/common/diagnostics/metrics/MetricsTypes:getName	()Ljava/lang/String;
		//   501: iload 4
		//   503: i2l
		//   504: invokeinterface 105 4 0
		//   509: iload 4
		//   511: aload_0
		//   512: getfield 25	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:maxRetry	I
		//   515: if_icmplt +27 -> 542
		//   518: aload 12
		//   520: ldc 106
		//   522: invokevirtual 107	com/marchnetworks/common/transport/datamodel/DeviceException:setDetailedErrorMessage	(Ljava/lang/String;)V
		//   525: aload 12
		//   527: getstatic 108	com/marchnetworks/common/types/DeviceExceptionTypes:COMMUNICATION_TIMEOUT	Lcom/marchnetworks/common/types/DeviceExceptionTypes;
		//   530: invokevirtual 109	com/marchnetworks/common/transport/datamodel/DeviceException:setDetailedErrorType	(Lcom/marchnetworks/common/types/DeviceExceptionTypes;)V
		//   533: aload 12
		//   535: iconst_1
		//   536: invokevirtual 110	com/marchnetworks/common/transport/datamodel/DeviceException:setCommunicationError	(Z)V
		//   539: aload 12
		//   541: athrow
		//   542: goto -341 -> 201
		//   545: aload 13
		//   547: instanceof 111
		//   550: ifeq +29 -> 579
		//   553: aload_0
		//   554: aload 13
		//   556: invokespecial 112	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:isUnauthorizedException	(Ljava/lang/Throwable;)Z
		//   559: ifne +20 -> 579
		//   562: aload 12
		//   564: getstatic 108	com/marchnetworks/common/types/DeviceExceptionTypes:COMMUNICATION_TIMEOUT	Lcom/marchnetworks/common/types/DeviceExceptionTypes;
		//   567: invokevirtual 109	com/marchnetworks/common/transport/datamodel/DeviceException:setDetailedErrorType	(Lcom/marchnetworks/common/types/DeviceExceptionTypes;)V
		//   570: aload 12
		//   572: iconst_1
		//   573: invokevirtual 110	com/marchnetworks/common/transport/datamodel/DeviceException:setCommunicationError	(Z)V
		//   576: aload 12
		//   578: athrow
		//   579: aload 5
		//   581: checkcast 27	javax/xml/ws/BindingProvider
		//   584: astore 13
		//   586: aload 13
		//   588: invokeinterface 113 1 0
		//   593: astore 14
		//   595: aload 14
		//   597: ldc 114
		//   599: invokeinterface 33 2 0
		//   604: checkcast 68	java/util/Map
		//   607: astore 15
		//   609: aload 14
		//   611: ldc 115
		//   613: invokeinterface 33 2 0
		//   618: checkcast 66	java/lang/Integer
		//   621: invokevirtual 67	java/lang/Integer:intValue	()I
		//   624: istore 16
		//   626: iload 16
		//   628: sipush 401
		//   631: if_icmpne +66 -> 697
		//   634: aload_0
		//   635: invokespecial 43	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:isUsingTrustedCommunication	()Z
		//   638: istore 17
		//   640: aload_0
		//   641: getfield 30	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:configuration	Ljava/util/Map;
		//   644: ldc 116
		//   646: invokeinterface 33 2 0
		//   651: ifnonnull +8 -> 659
		//   654: iload 17
		//   656: ifeq +27 -> 683
		//   659: new 79	com/marchnetworks/common/transport/datamodel/DeviceException
		//   662: dup
		//   663: ldc 117
		//   665: aload 11
		//   667: invokespecial 81	com/marchnetworks/common/transport/datamodel/DeviceException:<init>	(Ljava/lang/String;Ljava/lang/Throwable;)V
		//   670: astore 12
		//   672: aload 12
		//   674: getstatic 118	com/marchnetworks/common/types/DeviceExceptionTypes:NOT_AUTHORIZED	Lcom/marchnetworks/common/types/DeviceExceptionTypes;
		//   677: invokevirtual 109	com/marchnetworks/common/transport/datamodel/DeviceException:setDetailedErrorType	(Lcom/marchnetworks/common/types/DeviceExceptionTypes;)V
		//   680: aload 12
		//   682: athrow
		//   683: iload 17
		//   685: ifne +9 -> 694
		//   688: aload_0
		//   689: aload 5
		//   691: invokespecial 119	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:resetSessionForRetry	(Lcom/marchnetworks/device_ws/DeviceServiceSoap;)V
		//   694: goto +134 -> 828
		//   697: iload 16
		//   699: sipush 503
		//   702: if_icmpne +123 -> 825
		//   705: iinc 4 1
		//   708: getstatic 74	com/marchnetworks/common/diagnostics/metrics/MetricsHelper:metrics	Lcom/marchnetworks/command/api/metrics/MetricsCoreService;
		//   711: getstatic 94	com/marchnetworks/common/diagnostics/metrics/MetricsTypes:SOAP_CONNECTION	Lcom/marchnetworks/common/diagnostics/metrics/MetricsTypes;
		//   714: invokevirtual 76	com/marchnetworks/common/diagnostics/metrics/MetricsTypes:getName	()Ljava/lang/String;
		//   717: iload 4
		//   719: i2l
		//   720: invokeinterface 105 4 0
		//   725: iload 4
		//   727: aload_0
		//   728: getfield 25	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:maxRetry	I
		//   731: if_icmplt +31 -> 762
		//   734: new 79	com/marchnetworks/common/transport/datamodel/DeviceException
		//   737: dup
		//   738: ldc 120
		//   740: invokespecial 121	com/marchnetworks/common/transport/datamodel/DeviceException:<init>	(Ljava/lang/String;)V
		//   743: astore 12
		//   745: aload 12
		//   747: getstatic 122	com/marchnetworks/common/types/DeviceExceptionTypes:BUSY_UNAVAILABLE	Lcom/marchnetworks/common/types/DeviceExceptionTypes;
		//   750: invokevirtual 109	com/marchnetworks/common/transport/datamodel/DeviceException:setDetailedErrorType	(Lcom/marchnetworks/common/types/DeviceExceptionTypes;)V
		//   753: aload 12
		//   755: iconst_1
		//   756: invokevirtual 110	com/marchnetworks/common/transport/datamodel/DeviceException:setCommunicationError	(Z)V
		//   759: aload 12
		//   761: athrow
		//   762: aload_0
		//   763: aload 15
		//   765: aconst_null
		//   766: invokeinterface 33 2 0
		//   771: checkcast 123	java/util/List
		//   774: ldc 124
		//   776: invokespecial 125	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:getHeaderIntValue	(Ljava/util/List;Ljava/lang/String;)I
		//   779: istore 17
		//   781: iload 17
		//   783: iconst_m1
		//   784: if_icmpne +13 -> 797
		//   787: new 79	com/marchnetworks/common/transport/datamodel/DeviceException
		//   790: dup
		//   791: ldc 126
		//   793: invokespecial 121	com/marchnetworks/common/transport/datamodel/DeviceException:<init>	(Ljava/lang/String;)V
		//   796: athrow
		//   797: iload 17
		//   799: sipush 1000
		//   802: imul
		//   803: i2l
		//   804: invokestatic 127	java/lang/Thread:sleep	(J)V
		//   807: goto +15 -> 822
		//   810: astore 18
		//   812: getstatic 12	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:LOG	Lorg/slf4j/Logger;
		//   815: ldc -127
		//   817: invokeinterface 130 2 0
		//   822: goto +6 -> 828
		//   825: aload 12
		//   827: athrow
		//   828: goto +42 -> 870
		//   831: astore 10
		//   833: new 79	com/marchnetworks/common/transport/datamodel/DeviceException
		//   836: dup
		//   837: new 13	java/lang/StringBuilder
		//   840: dup
		//   841: invokespecial 14	java/lang/StringBuilder:<init>	()V
		//   844: ldc -125
		//   846: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   849: aload 10
		//   851: invokevirtual 17	java/lang/Exception:getMessage	()Ljava/lang/String;
		//   854: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   857: invokevirtual 18	java/lang/StringBuilder:toString	()Ljava/lang/String;
		//   860: aload 10
		//   862: invokespecial 81	com/marchnetworks/common/transport/datamodel/DeviceException:<init>	(Ljava/lang/String;Ljava/lang/Throwable;)V
		//   865: astore 11
		//   867: aload 11
		//   869: athrow
		//   870: goto -669 -> 201
		//   873: astore 19
		//   875: aconst_null
		//   876: aload 6
		//   878: if_acmpeq +13 -> 891
		//   881: getstatic 6	com/marchnetworks/server/communications/soap/SoapProxyInvocationHandler:PORT_POOL	Lorg/apache/commons/pool/impl/GenericKeyedObjectPool;
		//   884: aload 7
		//   886: aload 6
		//   888: invokevirtual 10	org/apache/commons/pool/impl/GenericKeyedObjectPool:returnObject	(Ljava/lang/Object;Ljava/lang/Object;)V
		//   891: aload 19
		//   893: athrow
		// Line number table:
		//   Java source line #194	-> byte code offset #0
		//   Java source line #196	-> byte code offset #3
		//   Java source line #197	-> byte code offset #6
		//   Java source line #198	-> byte code offset #9
		//   Java source line #201	-> byte code offset #15
		//   Java source line #203	-> byte code offset #27
		//   Java source line #204	-> byte code offset #58
		//   Java source line #207	-> byte code offset #60
		//   Java source line #213	-> byte code offset #102
		//   Java source line #214	-> byte code offset #115
		//   Java source line #215	-> byte code offset #123
		//   Java source line #217	-> byte code offset #137
		//   Java source line #224	-> byte code offset #144
		//   Java source line #218	-> byte code offset #147
		//   Java source line #220	-> byte code offset #149
		//   Java source line #222	-> byte code offset #159
		//   Java source line #223	-> byte code offset #162
		//   Java source line #225	-> byte code offset #195
		//   Java source line #228	-> byte code offset #201
		//   Java source line #231	-> byte code offset #204
		//   Java source line #232	-> byte code offset #207
		//   Java source line #233	-> byte code offset #211
		//   Java source line #235	-> byte code offset #215
		//   Java source line #236	-> byte code offset #222
		//   Java source line #237	-> byte code offset #229
		//   Java source line #239	-> byte code offset #239
		//   Java source line #240	-> byte code offset #251
		//   Java source line #237	-> byte code offset #267
		//   Java source line #244	-> byte code offset #273
		//   Java source line #246	-> byte code offset #287
		//   Java source line #247	-> byte code offset #292
		//   Java source line #251	-> byte code offset #303
		//   Java source line #252	-> byte code offset #310
		//   Java source line #253	-> byte code offset #318
		//   Java source line #254	-> byte code offset #325
		//   Java source line #255	-> byte code offset #330
		//   Java source line #260	-> byte code offset #344
		//   Java source line #261	-> byte code offset #351
		//   Java source line #262	-> byte code offset #375
		//   Java source line #365	-> byte code offset #379
		//   Java source line #366	-> byte code offset #385
		//   Java source line #265	-> byte code offset #398
		//   Java source line #267	-> byte code offset #400
		//   Java source line #269	-> byte code offset #407
		//   Java source line #271	-> byte code offset #423
		//   Java source line #274	-> byte code offset #447
		//   Java source line #276	-> byte code offset #455
		//   Java source line #280	-> byte code offset #458
		//   Java source line #282	-> byte code offset #474
		//   Java source line #284	-> byte code offset #481
		//   Java source line #285	-> byte code offset #489
		//   Java source line #286	-> byte code offset #492
		//   Java source line #288	-> byte code offset #509
		//   Java source line #289	-> byte code offset #518
		//   Java source line #290	-> byte code offset #525
		//   Java source line #291	-> byte code offset #533
		//   Java source line #292	-> byte code offset #539
		//   Java source line #294	-> byte code offset #542
		//   Java source line #297	-> byte code offset #545
		//   Java source line #298	-> byte code offset #562
		//   Java source line #299	-> byte code offset #570
		//   Java source line #300	-> byte code offset #576
		//   Java source line #305	-> byte code offset #579
		//   Java source line #306	-> byte code offset #586
		//   Java source line #308	-> byte code offset #595
		//   Java source line #309	-> byte code offset #609
		//   Java source line #311	-> byte code offset #626
		//   Java source line #312	-> byte code offset #634
		//   Java source line #314	-> byte code offset #640
		//   Java source line #315	-> byte code offset #659
		//   Java source line #316	-> byte code offset #672
		//   Java source line #317	-> byte code offset #680
		//   Java source line #321	-> byte code offset #683
		//   Java source line #322	-> byte code offset #688
		//   Java source line #325	-> byte code offset #694
		//   Java source line #326	-> byte code offset #697
		//   Java source line #328	-> byte code offset #705
		//   Java source line #329	-> byte code offset #708
		//   Java source line #330	-> byte code offset #725
		//   Java source line #331	-> byte code offset #734
		//   Java source line #332	-> byte code offset #745
		//   Java source line #333	-> byte code offset #753
		//   Java source line #334	-> byte code offset #759
		//   Java source line #337	-> byte code offset #762
		//   Java source line #339	-> byte code offset #781
		//   Java source line #340	-> byte code offset #787
		//   Java source line #344	-> byte code offset #797
		//   Java source line #347	-> byte code offset #807
		//   Java source line #345	-> byte code offset #810
		//   Java source line #346	-> byte code offset #812
		//   Java source line #349	-> byte code offset #822
		//   Java source line #351	-> byte code offset #825
		//   Java source line #360	-> byte code offset #828
		//   Java source line #355	-> byte code offset #831
		//   Java source line #358	-> byte code offset #833
		//   Java source line #359	-> byte code offset #867
		//   Java source line #361	-> byte code offset #870
		//   Java source line #365	-> byte code offset #873
		//   Java source line #366	-> byte code offset #881
		// Local variable table:
		//   start	length	slot	name	signature
		//   0	894	0	this	SoapProxyInvocationHandler
		//   0	894	1	proxy	Object
		//   0	894	2	method	java.lang.reflect.Method
		//   0	894	3	args	Object[]
		//   1	725	4	retryAttempts	int
		//   4	686	5	port	DeviceServiceSoap
		//   7	880	6	portHolder	PortHolder
		//   13	872	7	url	String
		//   147	43	8	e	Exception
		//   202	238	8	start	long
		//   205	30	10	argLength	int
		//   398	3	10	e	java.lang.reflect.InvocationTargetException
		//   831	30	10	e	Exception
		//   220	77	11	soapArgs	Object[]
		//   405	261	11	cause	Throwable
		//   865	3	11	dException	com.marchnetworks.common.transport.datamodel.DeviceException
		//   227	54	12	soapClasses	Class<?>[]
		//   421	405	12	dException	com.marchnetworks.common.transport.datamodel.DeviceException
		//   230	38	13	i	int
		//   285	8	13	portMethod	java.lang.reflect.Method
		//   479	76	13	innerCause	Throwable
		//   584	3	13	bindingProvider	BindingProvider
		//   301	44	14	result	Object
		//   593	17	14	resp_ctx	Map<String, Object>
		//   316	3	15	cookie	String
		//   349	27	15	resultObject	Object
		//   607	157	15	headers	Map<String, List<String>>
		//   323	73	16	sessionId	String
		//   624	74	16	code	int
		//   638	46	17	usingTrustedCommunication	boolean
		//   779	19	17	retryValue	int
		//   810	3	18	ie	InterruptedException
		//   873	19	19	localObject1	Object
		// Exception table:
		//   from	to	target	type
		//   102	144	147	java/lang/Exception
		//   204	379	398	java/lang/reflect/InvocationTargetException
		//   797	807	810	java/lang/InterruptedException
		//   204	379	831	java/lang/Exception
		//   102	379	873	finally
		//   398	875	873	finally
	}
}

