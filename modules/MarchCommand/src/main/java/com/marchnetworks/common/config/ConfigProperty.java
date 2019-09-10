package com.marchnetworks.common.config;

public enum ConfigProperty
{
	LDAP_SERVER( "ldap_primary", "The host of the LDAP server (eg. marchnetworks.com" ),
	ADMIN_USERNAME( "admin_username", "Admin username" ),
	ADMIN_TYPE( "admin_type", "Admin type" ),
	ADMIN_PASSWORD( "local_admin_password", "Admin password" ),
	LDAP_DOMAIN_NAME( "ldap_domain_name", "ldap_domain_name" ),
	LDAP_USERNAME( "ldap_username", "The LDAP username specified during install." ),
	LDAP_PASSWORD( "ldap_password", "The LDAP password specified during install." ),
	LDAP_SEARCH_DIRECTORY_ROOT( "ldap_search_directory_root", "The search directory root string configured to be used on LDAP search (eg. OU=Kanata,DC=marchnetworks,DC=com" ),
	LDAP_SSL( "ldap_ssl", "Boolean flag to enable LDAP SSL (LDAPS) support. Ensure proper certificate is in truststore for glassfish" ),
	LDAP_METHOD( "ldap_method", "Authentication method to use when connecting to LDAP server." ),
	LDAP_DEBUG( "ldap_debug", "Boolean value that will log complete LDAP trace." ),
	LOGIN_TIMEOUT( "login_timeout", "Amount of time to cache an LDAP password if server is unreachable." ),
	LDAP_ACCOUNTNAME( "ldap_accountname", "Specifying a specific parameter to search on." ),
	LDAP_COMMONNAME( "ldap_commonname", "Specifying a specific parameter to search on." ),
	LDAP_MAIL( "ldap_mail", "Specifying a specific parameter to search on." ),
	LDAP_PHONE( "ldap_phone", "Specifying a specific parameter to search on." ),
	LDAP_TITLE( "ldap_title", "Specifying a specific parameter to search on." ),
	LDAP_MANAGER( "ldap_manager", "Specifying a specific parameter to search on." ),
	LDAP_PRINCIPALNAME( "ldap_principalname", "Specifying a specific parameter to search on." ),
	LDAP_DISTINGUISHEDNAME( "ldap_distinguishedname", "Specifying a specific parameter to search on." ),
	LDAP_CUSTOMFILTER( "ldap_customfilter", "Allows a custom filter to be applied on user searches." ),
	LDAP_ENABLED( "ldap_enabled", "Used to flag whether or not LDAP has been enabled in the management console." ),
	LDAP_ONLY( "ldap_only", "Disables local user creation in the Command Client if it is set to true." ),

	LDAP_DNS_REFRESH_INTERVAL( "ldap_dns_refresh", "The amount of time in seconds before CES refreshes the DNS entries" ),
	LDAP_DISCOVERY_ENABLED( "ldap_discovery_enabled", "Enable the dynamic discovery of AD servers" ),
	LDAP_GLOBAL_CATALOG( "ldap_global_catalog", "Enable LDAP query on Global Catalog" ),
	LDAP_SITE_NAME( "ldap_site_name", "Site Name" ),
	LDAP_TIMEOUT( "ldap_timeout", "Server Connection Timeout" ),
	LDAP_STARTTLS( "ldap_start_tls", "Boolean flag to enable LDAPv3 StartTLS support" ),

	PACKAGE_NUMBER( "package_version", "The Hudson build package number of the installer." ),

	BASE_HTTP_URL( "base_http_url", "Start of the HTTP url for web application up to and including the port (eg. http://yourserver:80)" ),
	HTTP_PORT( "http_port", "The port for client browser HTTP to access (eg. 80)" ),
	HTTPS_PORT( "https_port", "The port for client browser HTTPS to access (eg. 443)" ),
	EXTERNAL_HTML( "external_html", "An external HTML address used by the client" ),

	HTTP_CONTEXT_ROOT( "http_context_root", "The HTTP path that follows the base HTTP URL.  Should start with a / and should not end with a /.  (Eg. /SWMS)" ),

	HTTP_URL_PATTERN_DEVICE( "http_url_pattern_device", "The relative path that a device should use to communicate with the server.  Will follow context root, should start with / (eg. /Device)" ),

	CERT_ALL_HOSTNAMES( "cert_all_hostnames", "All CES hostnames (resolvable and user-entered) to be inserted into the certificate during certificate generation. Separate multiple entries with commas" ),
	CERT_ALL_IPS( "cert_all_ips", "All CES IPs (resolvable and user-entered) to be inserted into the certificate during certificate generation. Separate multiple entries with commas" ),
	CERT_PUBLIC_KEY_LENGTH( "cert_public_key_length", "A custom size for CES's digital certificates public keys. Just observed upon certificate creation (e.g. 1st CES boot). Defaults to 1024 bits." ),

	SERVER_ADDRESS_LIST( "server_address_list", "A user-defined list of CES addresses that should be sent to devices during registration and pushed whenenever it changes" ),

	ALARMS_FEATURE( "alarms_feature", "Boolean flag entry to switch on/off whether CES will activate alarm features and subscribe with devices to get notified about alarm related events" ),
	DEVICE_TIME_SYNC_FEATURE( "time_sync_feature", "Boolean flag entry to switch on/off whether CES advertise its current time to recorders" ),
	DEVICE_LEGACY_REMOTE_ADDRESS_TEST( "legacy_remote_address_test", "Boolean flag to switch on/off legacy remote address test. Legacy test tries to connect out over HTTP only" ),
	DEVICE_OFFLINE_MONITOR_MAX_DISCONNECTION_TIME( "offline_monitor_max_disconnection_time", "The amount of time in minutes before CES raises an alert for an offline device" ),
	DEVICE_OFFLINE_MONITOR_TRIGGER_INTERVAL( "offline_monitor_trigger_interval", "The amount of time in minutes in which CES will check whether an alert should be raised for disconnected devices" ),

	HTTP_CLIENT_MAX_CONNECTIONS_PER_HOST( "http_max_connections_per_host", "The allowed number of simultaneous connections created by the RestfulServiceClient to a given host" ),
	HTTP_CLIENT_SOCKET_CONNECTION_TIMEOUT( "http_socket_connection_timeout", "The time in milliseconds to establish a socket connection, for all RestfulServiceClient connections" ),
	HTTP_CLIENT_MANAGER_CONNECTION_TIMEOUT( "http_manager_connection_timeout", "Timeout in milliseconds to get a new connection from the http connection manager in RestfulServiceClient" ),
	HTTP_CLIENT_SOCKET_DATA_TIMEOUT( "http_socket_data_timeout", "timeout in milliseconds to retrieve or send any data on the socket after a call to send/recv" ),
	HTTP_CLIENT_MAX_RETRIES( "http_max_retries", "The amount of times that the http client will re-attempt to establish a socket connection" ),

	ALERT_PURGE_MAX_AGE( "clean_alert_age", "The number of days for an alert to be considered as garbage and be purged" ),
	ALARM_PURGE_MAX_AGE( "clean_alarms_age", "The number of days for an alarm to be considered as garbage and be purged" ),
	AUDIT_LOG_MAX_AGE( "clean_audit_logs_age", "The number of days for an audit log entry to be considered as garbage and be purged" ),
	IGNORE_ALERT_LIST( "disabled_alert_codes", "An extra entry to disable alert creation/handling for a particular code(s). Separate multiple entries with commas" ),

	CRYPTOGRAPHIC_HASH_FUNCTION( "cryptographic_hash_function", "cryptographic hash function, now support MD5 & SHA-1" ),
	REALM( "realm", "realm for digest and basic authentication" ),

	EVENT_SEND_DELAY( "event_send_delay", "The amount of time (ms) to collect events before sending them to Clients" ),

	CONFIG_APPLY_TIMEOUT( "configuration_apply_timeout", "Timeout in seconds for a Configuration apply job to complete" ),
	FIRMWARE_UPGRADE_TIMEOUT( "firmware_upgrade_timeout", "Timeout in seconds for a Firmware upgrade job to complete" ),
	CONFIG_APPLY_MOBILE_TIMEOUT( "configuration_apply_mobile_timeout", "Timeout in seconds for a Configuration apply job for mobile devices to complete" ),
	FIRMWARE_UPGRADE_MOBILE_TIMEOUT( "firmware_upgrade_mobile_timeout", "Timeout in seconds for a Firmware upgrade job for mobile devices to complete" ),

	DB_STATE( "db_state", "The type of server configured. Internal, External or Legacy" ),
	DB_EXTERNAL_INSTALL( "db_external_install", "If server was installed with external database from the start" ),

	SMTP_SERVER( "Notification_SmtpServer", "eMail SMTP server address" ),
	SMTP_SENDER( "Notification_Sender", "eMail SMTP sender address" ),
	SMTP_USERNAME( "Notification_User", "eMail SMTP authentication user name" ),
	SMTP_PASSWORD( "Notification_Pass", "eMail SMTP authentication user password" ),
	SMTP_AUTHENTICATION( "Notification_Auth", "enable eMail SMTP authentication" ),
	SMTP_ENABLESTARTTLS( "notification_start_tls", "enable eMail SMTP starttls" ),

	TEST_PAGES( "test_pages", "A flag for enabling/disabling test and debug pages" ),
	CLIENT_VERSION( "client_version", "version of the Command Client" ),
	SERVER_VERSION( "server_version", "version of the Command server" ),
	INTERFACE_VERSION( "interface_version", "version of the Command interface. This is used for API synchronization" ),

	AGENT_NOTIFY_TIMEOUT( "agent_svr_notify_timeout_sec", "number of seconds to wait for a server to respond to the notify http message. Default 20" ),
	AGENT_NOTIFY_MAX_TIME( "agent_svr_notify_max_sec", "maximum seconds between any notification attempt to the server. Default 120." ),
	AGENT_NOTIFY_MIN_WAIT_TIME( "agent_svr_notify_min_sec", "minimum wait in seconds between notifications during a retry. Exponential backoff (doubling) is used until reaching the maximum. Default 5." ),
	AGENT_NOTIFY_TEST_FREQUENCY( "agent_svr_test_frequency_sec", "number of seconds between tests of higher priority server addresses, when connected to a lower priority address. Default 120." ),
	AGENT_NOTIFY_REACTIVATE( "agent_svr_notify_reactivate_sec", "number of seconds to wait for the server to create a subscription or check events after a successful notify, before re-activating the notification. Default 60." ),

	DEVICE_TIME_TO_OFFLINE( "device_time_to_offline", "The amount of time before a device will be marked offline. Default 240." );

	private final String m_XmlName;

	private final String m_Description;

	private ConfigProperty( String a_XmlName, String a_Description )
	{
		m_XmlName = a_XmlName;
		m_Description = a_Description;
	}

	public String getXmlName()
	{
		return m_XmlName;
	}

	public String getDescription()
	{
		return m_Description;
	}
}
