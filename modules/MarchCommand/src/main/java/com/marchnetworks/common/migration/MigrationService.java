package com.marchnetworks.common.migration;

import com.marchnetworks.command.api.migration.MigrationDAOImpl;
import com.marchnetworks.command.api.migration.MigrationDAOImpl.ConstraintType;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.EncoderView;
import com.marchnetworks.command.common.device.data.VideoEncoderView;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.ProfileView;
import com.marchnetworks.command.common.user.data.RightEnum;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.system.parameter.model.ParameterSettingDAO;
import com.marchnetworks.common.system.parameter.model.ParameterSettingEntity;
import com.marchnetworks.common.utils.ServerUtils;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.shared.config.CommonConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MigrationService
{
	private static final String PARAMETER_DATABASE_VERSION = "database_version";
	private static final Logger LOG = LoggerFactory.getLogger( MigrationService.class );

	private CoreMigrationDAO coreMigrationDAO;
	private UserService userService;
	private ParameterSettingDAO parameterSettingDAO;
	private CommonConfiguration configuration;

	@Transactional( propagation = Propagation.REQUIRES_NEW )
	public void setVersion( DatabaseVersion databaseVersion )
	{
		ParameterSettingEntity parameterSetting = ( ParameterSettingEntity ) parameterSettingDAO.findById( "database_version" );
		if ( parameterSetting == null )
		{
			parameterSetting = new ParameterSettingEntity();
			parameterSetting.setParameterName( "database_version" );
			parameterSettingDAO.create( parameterSetting );
		}
		parameterSetting.setParameterValue( databaseVersion.name() );
	}

	@Transactional( propagation = Propagation.REQUIRES_NEW )
	public DatabaseVersion getVersion()
	{
		if ( isDatabaseEmpty() )
		{
			LOG.info( "The database is empty, no migration to perform" );
			ParameterSettingEntity parameterSetting = new ParameterSettingEntity();
			parameterSetting.setParameterName( "database_version" );
			parameterSetting.setParameterValue( Migration.TARGET_DATABASE_VERSION.name() );
			parameterSettingDAO.create( parameterSetting );
			return Migration.TARGET_DATABASE_VERSION;
		}

		ParameterSettingEntity parameterSetting = ( ParameterSettingEntity ) parameterSettingDAO.findById( "database_version" );
		if ( parameterSetting != null )
		{
			return DatabaseVersion.valueOf( parameterSetting.getParameterValue() );
		}
		return null;
	}

	public boolean isDatabaseEmpty()
	{
		return parameterSettingDAO.isParameterSettingEmpty();
	}

	@Transactional( propagation = Propagation.REQUIRES_NEW )
	public void migrateToVersion( DatabaseVersion desiredVersion )
	{
		if ( desiredVersion == DatabaseVersion.VERSION_1_6 )
		{
			migrateTo_1_6();
		}
		else if ( desiredVersion == DatabaseVersion.VERSION_1_7 )
		{
			migrateTo_1_7();
		}
		else if ( desiredVersion == DatabaseVersion.VERSION_1_8 )
		{
			migrateTo_1_8();
		}
		else if ( desiredVersion == DatabaseVersion.VERSION_1_9 )
		{
			migrateTo_1_9();
		}
		else if ( desiredVersion == DatabaseVersion.VERSION_1_10 )
		{
			migrateTo_1_10();
		}
		else if ( desiredVersion == DatabaseVersion.VERSION_1_10_1 )
		{
			migrateTo_1_10_1();
		}
		else if ( desiredVersion == DatabaseVersion.VERSION_1_11_0 )
		{
			migrateTo_1_11_0();
		}
		else if ( desiredVersion == DatabaseVersion.VERSION_2_0 )
		{
			migrateTo_2_0();
		}
		else if ( desiredVersion == DatabaseVersion.VERSION_2_1 )
		{
			migrateTo_2_1();
		}
		else if ( desiredVersion == DatabaseVersion.VERSION_2_5 )
		{
			migrateTo_2_5();
		}
		else if ( desiredVersion == DatabaseVersion.VERSION_2_6 )
		{
			migrateTo_2_6();
		}
	}

	private void migrateTo_1_6()
	{
		String constraint = coreMigrationDAO.findFirstConstraintName( "fileobject_table", MigrationDAOImpl.ConstraintType.UNIQUE_CONSTRAINT );

		coreMigrationDAO.dropConstraint( "fileobject_table", constraint, MigrationDAOImpl.ConstraintType.UNIQUE_CONSTRAINT );
		coreMigrationDAO.dropColumns( "fileobject_table", new String[] {"filepath"} );

		coreMigrationDAO.initializeColumn( "fileobject_table", "category", "FIRMWARE" );
	}

	private void migrateTo_1_7()
	{
		coreMigrationDAO.updateLoginCacheUsernames();

		coreMigrationDAO.dropColumns( "app", new String[] {"name", "developer", "description"} );

		List<Object[]> clientObjects = coreMigrationDAO.findAllRowsFromTable( "client_objects" );
		for ( Object[] clientObject : clientObjects )
		{
			coreMigrationDAO.updateGenericStorage( clientObject );
		}
		coreMigrationDAO.dropTables( new String[] {"client_objects"} );

		coreMigrationDAO.createTable( "AUDIT_LOGS_OLD", new String[] {"REMOTE_ADDRESS", "USER_NAME", "DATE_AND_TIME", "CALLER_CLASS", "CALLER_METHOD", "TOP_OBJECT_NAME", "AUDIT_LOG_BODY", "ROOT_DEVICE_NAME", "LEAF_DEVICE_NAME", "REQUEST_TYPE"}, new String[] {"NVARCHAR(255)", "NVARCHAR(255)", "DATETIME", "NVARCHAR(255)", "NVARCHAR(255)", "NVARCHAR(255)", "VARBINARY(MAX)", "NVARCHAR(255)", "NVARCHAR(255)", "NVARCHAR(255)"} );

		coreMigrationDAO.migrateOldAuditLogs();

		coreMigrationDAO.dropColumns( "audit_logs", new String[] {"DATE_AND_TIME", "CALLER_CLASS", "CALLER_METHOD", "TOP_OBJECT_NAME", "AUDIT_LOG_BODY", "ROOT_DEVICE_NAME", "LEAF_DEVICE_NAME", "REQUEST_TYPE"} );

		coreMigrationDAO.deleteRows( "audit_logs", null );

		coreMigrationDAO.explicitColumnConversion( "audit_logs", "REMOTE_ADDRESS", "int" );
		coreMigrationDAO.explicitColumnConversion( "audit_logs", "USER_NAME", "int" );

		coreMigrationDAO.updateChannelResourceNames();
	}

	private void migrateTo_1_8()
	{
		if ( userService.listAllProfiles() != null )
		{
			for ( ProfileView profile : userService.listAllProfiles() )
			{
				if ( ( profile.getProfileRights() != null ) && ( profile.getProfileRights().contains( RightEnum.ARCHIVE_VIDEO ) ) )
				{
					userService.addRightToProfile( profile.getName(), RightEnum.EXPORT_MP4 );
				}
			}
		}

		List<Object[]> encoders = coreMigrationDAO.findItemsToMigrate( "encoder", null, new String[] {"media_type", "channel"} );

		if ( !encoders.isEmpty() )
		{
			EncoderView placeHolder = new VideoEncoderView();
			EncoderView[] placeHolderArray = new EncoderView[1];
			placeHolderArray[0] = placeHolder;
			String json = CoreJsonSerializer.toJson( placeHolderArray );
			byte[] placeholderBytes = CommonAppUtils.encodeStringToBytes( json );

			Set<Long> channelIds = new HashSet();
			for ( Object[] encoder : encoders )
			{
				String mediaType = ( String ) encoder[0];
				BigDecimal channel = ( BigDecimal ) encoder[1];

				if ( mediaType.equals( "video" ) )
				{
					Long channelLong = Long.valueOf( channel.longValue() );
					if ( !channelIds.contains( channelLong ) )
					{
						channelIds.add( channelLong );
						String whereClause = "id = " + channel;
						coreMigrationDAO.updateTable( "channel", "encoders_string", placeholderBytes, whereClause );
					}
				}
			}

			coreMigrationDAO.dropTables( new String[] {"video_encoder", "audio_encoder", "encoder"} );
		}
	}

	private void migrateTo_1_9()
	{
		coreMigrationDAO.updateVarcharColumns( "channel", "assoc_ids", "4000", "NULL" );
		coreMigrationDAO.updateVarcharColumns( "device", "device_event_subscription_prefixes", "4000", "NULL" );

		coreMigrationDAO.dropColumns( "parameter_setting", new String[] {"value_type"} );
	}

	private void migrateTo_1_10()
	{
		coreMigrationDAO.dropConstraint( "deviceConfig", "FK6DD2F118A5F7510B", MigrationDAOImpl.ConstraintType.FOREIGN_KEY );
		coreMigrationDAO.dropConstraint( "deviceConfig", "FK6DD2F118B65A1067", MigrationDAOImpl.ConstraintType.FOREIGN_KEY );
		coreMigrationDAO.dropColumns( "deviceConfig", new String[] {"FK_FILE_STORAGE_ID"} );

		coreMigrationDAO.updateTable( "deviceconfig", "assign_state", "UNASSOCIATED", "assign_state = 'FAILED_FIRMWARE'" );
	}

	private void migrateTo_1_10_1()
	{
		coreMigrationDAO.initializeColumn( "firmware", "FAILURE_RETRY_COUNT", "0" );
	}

	private void migrateTo_1_11_0()
	{
		if ( userService.listAllProfiles() != null )
		{
			for ( ProfileView profile : userService.listAllProfiles() )
			{
				if ( profile.getName().equals( "Guard" ) )
				{
					profile.setSimplifiedUI( true );
					try
					{
						userService.updateProfile( profile, false );
						LOG.info( "Updated profile guard setting simplifiedUI to true" );
					}
					catch ( UserException e )
					{
						LOG.error( "Unable to update guard profile setting simplifiedUI to true", e );
					}
				}

				if ( profile.hasRight( RightEnum.ARCHIVE_VIDEO.name() ) )
				{
					userService.addRightToProfile( profile.getName(), RightEnum.EXPORT_LOCAL );
					userService.addRightToProfile( profile.getName(), RightEnum.MANAGE_CASE_MANAGEMENT );
					LOG.info( "Added rights {} and {} to profile {}", new Object[] {RightEnum.EXPORT_LOCAL, RightEnum.MANAGE_CASE_MANAGEMENT, profile.getName()} );
				}
			}
		}
	}

	private void migrateTo_2_0()
	{
		if ( userService.listAllProfiles() != null )
		{
			for ( ProfileView profile : userService.listAllProfiles() )
			{
				if ( profile.hasRight( RightEnum.ARCHIVE_VIDEO.name() ) )
				{
					userService.addRightToProfile( profile.getName(), RightEnum.EXPORT_NATIVE );
					LOG.info( "Added right {} to profile {}", RightEnum.EXPORT_NATIVE.name(), profile.getName() );
				}
			}
		}

		coreMigrationDAO.appendStringToColumn( "device", "address", ":443", "address not like '%:%'" );

		coreMigrationDAO.explicitColumnConversion( "FILEPROTERTY_TABLE", "PROPERTYVALUE", "varchar(max)" );
		coreMigrationDAO.explicitColumnConversion( "FILEPROTERTY_TABLE", "PROPERTYVALUE", "varbinary(max)" );
	}

	private void migrateTo_2_1()
	{
		List<String> hostnames = configuration.getPropertyList( "cert_extra_hostname" );
		hostnames.addAll( ServerUtils.getServerHostnames() );
		configuration.setProperty( ConfigProperty.CERT_ALL_HOSTNAMES, hostnames );

		List<String> ipAddresses = configuration.getPropertyList( "cert_extra_ip" );
		ipAddresses.addAll( ServerUtils.getServerAddresses() );
		configuration.setProperty( ConfigProperty.CERT_ALL_IPS, ipAddresses );

		List<String> allAddresses = new ArrayList( hostnames );
		allAddresses.addAll( ipAddresses );
		configuration.setProperty( ConfigProperty.SERVER_ADDRESS_LIST, allAddresses );

		coreMigrationDAO.dropColumns( "LOGINCACHE", new String[] {"GROUPS"} );

		coreMigrationDAO.modifyColumnType( "ARCHIVER_ASSOCIATION", "PRIMARY_ASSOCIATED_DEVICEID", "VARBINARY(MAX)" );
	}

	private void migrateTo_2_5()
	{
		coreMigrationDAO.migrateLoginCache();

		coreMigrationDAO.migrateMemberViews();

		coreMigrationDAO.migrateProfileRights();

		coreMigrationDAO.dropConstraint( "MEMBER", "FK87557E9AE0FB0A2E", MigrationDAOImpl.ConstraintType.FOREIGN_KEY );

		if ( coreMigrationDAO.columnExists( "MEMBER", "GROUPS" ) )
		{
			coreMigrationDAO.updateVarcharColumns( "MEMBER", "GROUPS", "1000", "NULL" );
		}

		coreMigrationDAO.migrateTerritoryToSchedules();

		coreMigrationDAO.updateTable( "NOTIFICATION", "NOTIFICATION_GROUP", "SEARCHLIGHT_NOTIFICATION", "NOTIFICATION_GROUP = 'BUSINESS_RULES'" );

		coreMigrationDAO.updateVarcharColumns( "CASE_EVIDENCE", "NAME", "255", "NULL" );
		coreMigrationDAO.updateVarcharColumns( "CASE_EVIDENCE", "MEMBER", "255", "NULL" );
		coreMigrationDAO.modifyColumnType( "CASE_EVIDENCE", "TIME_CREATED", "numeric(19)" );

		coreMigrationDAO.updateVarcharColumns( "CASE_NODE", "NAME", "255", "NULL" );
		coreMigrationDAO.updateVarcharColumns( "CASE_NODE", "TYPE", "255", "NULL" );
		coreMigrationDAO.modifyColumnType( "CASE_NODE", "TIME_CREATED", "numeric(19)" );

		coreMigrationDAO.updateVarcharColumns( "NOTIFICATION", "RECIPIENTS", "4000", "NULL" );
	}

	private void migrateTo_2_6()
	{
		coreMigrationDAO.updateTable( "DEVICE", "NOTIFY_INTERVAL", Integer.valueOf( 180 ), "PARENT_DEVICE is NULL" );
	}

	public void setcoreMigrationDAO( CoreMigrationDAO coreMigrationDAO )
	{
		this.coreMigrationDAO = coreMigrationDAO;
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}

	public void setParameterSettingDAO( ParameterSettingDAO parameterSettingDAO )
	{
		this.parameterSettingDAO = parameterSettingDAO;
	}

	public void setConfiguration( CommonConfiguration configuration )
	{
		this.configuration = configuration;
	}
}
