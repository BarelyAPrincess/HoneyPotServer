package com.marchnetworks.security.dns;

import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.common.ReflectionUtils;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.shared.config.CommonConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class DnsLookup implements InitializationListener
{
	public static final CountDownLatch ldapListObtained = new CountDownLatch( 1 );

	private static final Logger LOG = LoggerFactory.getLogger( DnsLookup.class );
	private static String EXECUTOR_ID = DnsLookup.class.getSimpleName();
	private static int POOL_SIZE = 1;
	private static int INITIAL_DELAY = 0;
	private static final String SRV_RR = "SRV";
	private static final String[] SRV_RR_ATTR = {"SRV"};

	private static final String LDAP_SCHEME = "ldap://";

	private static final String DNS_CONTEXT_FACTORY = "com.sun.jndi.dns.DnsContextFactory";
	private static final String LDAP_SRV_RECORD_CLASS = "com.sun.jndi.ldap.ServiceLocator$SrvRecord";
	private static final String LDAP_SERVICE_LOCATOR_CLASS = "com.sun.jndi.ldap.ServiceLocator";
	private static final String LDAP_SERVICE_LOCATOR_FUNCTION_NAME = "extractHostports";
	private static String dnsQuery = "";
	private static String[] serverRecords;
	private static String singleLdapServer = "";
	private static final Object serverRecordsLock = new Object();
	private int defaultDnsRefreshInterval;

	public DnsLookup()
	{
		defaultDnsRefreshInterval = 1440;
	}

	private TaskScheduler taskScheduler;

	private CommonConfiguration commonConfiguration;

	public void onAppInitialized()
	{
		boolean isDiscoveryEnabled = commonConfiguration.getBooleanProperty( ConfigProperty.LDAP_DISCOVERY_ENABLED, false );
		if ( isDiscoveryEnabled )
		{
			dnsQuery = prepareDnsQuery();
			int configuredDnsPollingTime = commonConfiguration.getIntProperty( ConfigProperty.LDAP_DNS_REFRESH_INTERVAL, defaultDnsRefreshInterval );

			if ( configuredDnsPollingTime > 0 )
			{
				taskScheduler.scheduleFixedPoolAtFixedRate( new DnsPollingTimer(), EXECUTOR_ID, POOL_SIZE, INITIAL_DELAY, configuredDnsPollingTime * 60, TimeUnit.SECONDS );
			}
		}
		else
		{
			String ldapUrl = commonConfiguration.getProperty( ConfigProperty.LDAP_SERVER );
			if ( ( ldapUrl != null ) && ( ldapUrl.length() > 0 ) )
			{
				singleLdapServer = "ldap://" + ldapUrl;
				ldapListObtained.countDown();
			}
		}
	}

	private String prepareDnsQuery()
	{
		String dnsQuery = null;
		String serverType = "_ldap.";
		String domainName = commonConfiguration.getProperty( ConfigProperty.LDAP_DOMAIN_NAME, "" );
		boolean isGlobalCatalog = commonConfiguration.getBooleanProperty( ConfigProperty.LDAP_GLOBAL_CATALOG, false );
		String siteName = commonConfiguration.getProperty( ConfigProperty.LDAP_SITE_NAME, "" );

		if ( isGlobalCatalog )
		{
			serverType = "_gc.";
		}

		if ( siteName.length() > 0 )
		{
			if ( domainName.length() == 0 )
			{
				LOG.warn( "Can't find a site without a Domain/Forest name" );
			}
			else
			{
				siteName = "." + siteName + "._sites";
			}
		}

		if ( domainName.length() > 0 )
		{
			domainName = "." + domainName;
		}

		dnsQuery = serverType + "_tcp" + siteName + domainName;

		return dnsQuery;
	}

	private void queryDns()
	{
		LOG.debug( "Quering DNS server" );

		Hashtable<String, String> env = new Hashtable();
		env.put( "java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory" );

		try
		{
			DirContext ctx = new InitialDirContext( env );

			Attributes attrs = ctx.getAttributes( dnsQuery, SRV_RR_ATTR );

			Attribute attr;
			if ( ( attrs != null ) && ( ( attr = attrs.get( "SRV" ) ) != null ) )
			{
				int numValues = attr.size();
				List<String> srvRecordsList = new ArrayList();

				for ( int i = 0; i < numValues; i++ )
				{
					String record = ( String ) attr.get( i );

					if ( record.endsWith( "." ) )
					{
						record = record.substring( 0, record.length() - 1 );
					}
					srvRecordsList.add( record );
					LOG.debug( "Found server record:{}", record );
				}
				if ( !srvRecordsList.isEmpty() )
				{
					String[] srvRecords = ( String[] ) srvRecordsList.toArray( new String[srvRecordsList.size()] );

					if ( srvRecords.length > 1 )
					{
						Arrays.sort( srvRecords, new ServerRecordComparator() );
					}

					synchronized ( serverRecordsLock )
					{

						Object[] records = new Object[srvRecords.length];
						int i = 0;
						for ( String serverRecord : srvRecords )
						{
							records[i] = ReflectionUtils.newInstance( "com.sun.jndi.ldap.ServiceLocator$SrvRecord", new Class[] {String.class}, new Object[] {serverRecord} );
							i++;
						}

						Object recordParam = ReflectionUtils.newGenericArray( "com.sun.jndi.ldap.ServiceLocator$SrvRecord", records );
						Object result = ReflectionUtils.callMethod( "com.sun.jndi.ldap.ServiceLocator", "extractHostports", new Object[] {recordParam} );
						String[] hostports = ( String[] ) result;

						serverRecords = ( String[] ) Arrays.copyOf( hostports, hostports.length );
					}

					ldapListObtained.countDown();
				}
			}
		}
		catch ( NamingException e )
		{
			LOG.error( "Error quering DNS server", e );
		}
	}

	public static String getLdapProviderUrl()
	{
		if ( singleLdapServer.length() > 0 )
		{
			return singleLdapServer;
		}

		StringBuilder url = new StringBuilder();
		synchronized ( serverRecordsLock )
		{
			for ( int k = 0; k < serverRecords.length; k++ )
			{
				url.append( "ldap://" + serverRecords[k] );
				if ( k != serverRecords.length )
				{
					url.append( " " );
				}
			}
		}

		return url.toString();
	}

	private class ServerRecordComparator implements Comparator<String>
	{
		private ServerRecordComparator()
		{
		}

		public int compare( String srv1, String srv2 )
		{
			StringTokenizer token1 = new StringTokenizer( srv1, " " );
			if ( token1.countTokens() != 4 )
			{
				return 1;
			}
			StringTokenizer token2 = new StringTokenizer( srv2, " " );
			if ( token2.countTokens() != 4 )
			{
				return -1;
			}

			int srv1Priority = Integer.parseInt( token1.nextToken() );
			int srv1Weight = Integer.parseInt( token1.nextToken() );
			int srv2Priority = Integer.parseInt( token2.nextToken() );
			int srv2Weight = Integer.parseInt( token2.nextToken() );

			if ( srv1Priority > srv2Priority )
				return 1;
			if ( srv1Priority < srv2Priority )
				return -1;
			if ( ( srv1Weight == 0 ) && ( srv2Weight != 0 ) )
				return -1;
			if ( ( srv1Weight != 0 ) && ( srv2Weight == 0 ) )
			{
				return 1;
			}

			return 0;
		}
	}

	private class DnsPollingTimer implements Runnable
	{
		private DnsPollingTimer()
		{
		}

		public void run()
		{
			DnsLookup.this.queryDns();
		}
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public void setCommonConfiguration( CommonConfiguration commonConfiguration )
	{
		this.commonConfiguration = commonConfiguration;
	}

	public void setDefaultDnsRefreshInterval( int defaultDnsRefreshInterval )
	{
		this.defaultDnsRefreshInterval = defaultDnsRefreshInterval;
	}
}

