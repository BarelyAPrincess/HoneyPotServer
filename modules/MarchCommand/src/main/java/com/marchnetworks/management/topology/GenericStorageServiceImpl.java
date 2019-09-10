package com.marchnetworks.management.topology;

import com.marchnetworks.app.events.AppEvent;
import com.marchnetworks.app.events.AppEventType;
import com.marchnetworks.command.api.topology.GenericStorageCoreService;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.topology.GenericStorageException;
import com.marchnetworks.command.common.topology.GenericStorageExceptionType;
import com.marchnetworks.command.common.topology.data.GenericObjectInfo;
import com.marchnetworks.command.common.topology.data.Store;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.command.common.user.data.RightEnum;
import com.marchnetworks.common.cache.Cache;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.topology.dao.GenericStorageDAO;
import com.marchnetworks.management.topology.events.GenericStorageEvent;
import com.marchnetworks.management.topology.events.GenericStorageEventType;
import com.marchnetworks.management.topology.events.GenericStorageUserStoreEvent;
import com.marchnetworks.management.topology.model.GenericStorageEntity;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.EventRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericStorageServiceImpl implements GenericStorageCoreService, EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( GenericStorageServiceImpl.class );

	private static final String DEFAULT_HOME_SECTION = "DEFAULT_HOME_SECTION";

	private static final String LAYOUT_WPF = "B90D8B01-F881-4750-96C6-4D894E2D64DE";

	private static final String USER_PREFERENCES = "B040EC2B-2B44-4FAE-B8AD-C8005FA900B8";

	private static final String[] OBJECTS_ID = {"B040EC2B-2B44-4FAE-B8AD-C8005FA900B8", "B90D8B01-F881-4750-96C6-4D894E2D64DE"};

	private final long MB = 1000000L;
	private final long GLOBAL_LIMIT = 1000000000L;
	private final long USER_LIMIT = 100000000L;
	private final long TOTAL_LIMIT = 2000000000L;
	private final long TOTAL_USER_LIMIT = 1000000000L;
	private final long OBJECT_LIMIT = 100000000L;

	private Cache<String, byte[]> genericStorageCache;

	private UserService userService;
	private GenericStorageDAO genericStorageDAO;
	private EventRegistry eventRegistry;

	public void process( Event event )
	{
		if ( ( event instanceof AppEvent ) )
		{
			AppEvent appEvent = ( AppEvent ) event;
			if ( appEvent.getAppEventType().equals( AppEventType.UNINSTALLED ) )
			{
				String appId = appEvent.getAppID();
				genericStorageDAO.deleteByAppId( appId );
			}
		}
	}

	public String getListenerName()
	{
		return GenericStorageServiceImpl.class.getSimpleName();
	}

	public void setObject( Store store, String objectId, byte[] objectData, String appId ) throws GenericStorageException
	{
		String username = null;
		if ( store == Store.USER )
		{
			username = CommonAppUtils.getUsernameFromSecurityContext();
		}
		checkQuery( store, objectId, null, username );

		if ( objectData.length > 100000000L )
		{
			throw new GenericStorageException( GenericStorageExceptionType.OBJECT_LIMIT_EXCEEDED, "Object size " + objectData.length / 1000000L + " exceeds limit of " + 100L + " MB" );
		}

		long size = genericStorageDAO.getSize( store, username );
		long totalSize = genericStorageDAO.getTotalSize();

		if ( totalSize + objectData.length > 2000000000L )
		{
			throw new GenericStorageException( GenericStorageExceptionType.TOTAL_LIMIT_EXCEEDED, "Total storage exceeded limit of 2000 MB" );
		}

		if ( store == Store.USER )
		{
			if ( size + objectData.length > 100000000L )
			{
				throw new GenericStorageException( GenericStorageExceptionType.USER_LIMIT_EXCEEDED, "User " + username + " exceeded user storage limit of " + 100L + " MB" );
			}
			long totalUserSize = genericStorageDAO.getTotalUserSize();
			if ( totalUserSize + objectData.length > 1000000000L )
			{
				throw new GenericStorageException( GenericStorageExceptionType.USER_LIMIT_EXCEEDED, "User " + username + " exceeded total user storage limit of " + 1000L + " MB" );
			}
		}
		else if ( size + objectData.length > 1000000000L )
		{
			throw new GenericStorageException( GenericStorageExceptionType.GLOBAL_LIMIT_EXCEEDED, "Global storage exceeded limit of 1000 MB" );
		}

		GenericStorageEntity genericStorage = genericStorageDAO.findByIdentifiers( store, objectId, appId, username );

		if ( genericStorage == null )
		{
			genericStorage = new GenericStorageEntity();
			genericStorage.setObjectId( objectId );
			genericStorage.setStore( store );
			if ( store == Store.USER )
			{
				genericStorage.setUserId( username );
			}
			if ( !CommonAppUtils.isNullOrEmptyString( appId ) )
			{
				genericStorage.setAppId( appId );
			}
			genericStorageDAO.create( genericStorage );
		}
		genericStorage.setData( objectData );
		genericStorage.setSize( objectData.length );

		String id = genericStorage.getTagId();
		genericStorageCache.updateObject( id, objectData );

		sendGenericStorageEvent( GenericStorageEventType.UPDATED, store, objectId, appId, objectData.length, username );
	}

	public byte[] getObject( Store store, String objectId, String appId, String userId, boolean isUserRequest ) throws GenericStorageException
	{
		String username = null;
		if ( ( isUserRequest ) && ( store == Store.USER ) )
		{
			username = CommonAppUtils.getUsernameFromSecurityContext();
		}
		checkQuery( store, objectId, userId, username );

		if ( !CommonAppUtils.isNullOrEmptyString( userId ) )
		{
			checkUserRight( username, userId );
			username = userId;
		}

		String id = getObjectId( store, objectId, appId, username );
		byte[] result = ( byte[] ) genericStorageCache.getObject( id );
		if ( result != null )
		{
			return result;
		}

		GenericStorageEntity storage = genericStorageDAO.findByIdentifiers( store, objectId, appId, username );
		if ( storage == null )
		{
			if ( ( !objectId.contains( "DEFAULT_HOME_SECTION" ) ) && ( !Arrays.asList( OBJECTS_ID ).contains( objectId ) ) )
			{
				throw new GenericStorageException( GenericStorageExceptionType.NOT_FOUND, "Generic storage for store:" + store + ", objectId:" + objectId + ", appId:" + appId + ", userId:" + username + " not found when querying" );
			}
			return null;
		}

		genericStorageCache.returnObject( id, storage.getData() );
		return storage.getData();
	}

	public void deleteObject( Store store, String objectId, String appId, String userId, boolean isUserRequest ) throws GenericStorageException
	{
		String username = null;
		if ( ( isUserRequest ) && ( store == Store.USER ) )
		{
			username = CommonAppUtils.getUsernameFromSecurityContext();
		}
		checkQuery( store, objectId, userId, username );

		if ( !CommonAppUtils.isNullOrEmptyString( userId ) )
		{
			checkUserRight( username, userId );
			username = userId;
		}

		Long storageId = genericStorageDAO.findIdByIdentifiers( store, objectId, appId, username );
		if ( storageId == null )
		{
			throw new GenericStorageException( GenericStorageExceptionType.NOT_FOUND, "Generic storage for store:" + store + ", objectId:" + objectId + ", appId:" + appId + ", userId:" + username + " not found when deleting" );
		}
		genericStorageDAO.deleteDetached( storageId );

		String tagId = getObjectId( store, objectId, appId, username );
		genericStorageCache.removeObject( tagId );

		sendGenericStorageEvent( GenericStorageEventType.REMOVED, store, objectId, appId, 0L, username );
	}

	public GenericObjectInfo[] listObjects( Store store, String appId, String userId, boolean isUserRequest ) throws GenericStorageException
	{
		String username = null;
		if ( ( isUserRequest ) && ( store == Store.USER ) )
		{
			username = CommonAppUtils.getUsernameFromSecurityContext();
		}
		checkQuery( store, userId, username );

		if ( !CommonAppUtils.isNullOrEmptyString( userId ) )
		{
			checkUserRight( username, userId );
			username = userId;
		}

		List<GenericStorageEntity> storageObjects = genericStorageDAO.findAllByIdentifiers( store, appId, username );
		Set<String> checkedNames;

		if ( ( isUserRequest ) && ( store == Store.USER ) && ( userId != null ) && ( userId.equals( "*" ) ) )
		{
			String currentUser = CommonAppUtils.getUsernameFromSecurityContext();
			checkedNames = new HashSet();

			for ( GenericStorageEntity storage : storageObjects )
			{
				checkedNames.add( storage.getUserId() );
			}
			for ( Iterator<String> iterator = checkedNames.iterator(); iterator.hasNext(); )
			{
				if ( !checkUserAccess( currentUser, ( String ) iterator.next() ) )
					iterator.remove();
			}

			for ( Iterator<GenericStorageEntity> iterator = storageObjects.iterator(); iterator.hasNext(); )
			{
				GenericStorageEntity storage = iterator.next();
				if ( !checkedNames.contains( storage.getUserId() ) )
				{
					iterator.remove();
				}
			}
		}

		GenericObjectInfo[] result = new GenericObjectInfo[storageObjects.size()];
		for ( int i = 0; i < storageObjects.size(); i++ )
		{
			GenericStorageEntity storageObject = ( GenericStorageEntity ) storageObjects.get( i );
			result[i] = storageObject.toInfoObject();
		}
		return result;
	}

	public String getObjectTag( Store store, String objectId, String appId, String userId, boolean isUserRequest ) throws GenericStorageException
	{
		String username = null;
		if ( ( isUserRequest ) && ( store == Store.USER ) )
		{
			username = CommonAppUtils.getUsernameFromSecurityContext();
		}
		checkQuery( store, objectId, userId, username );

		if ( !CommonAppUtils.isNullOrEmptyString( userId ) )
		{
			username = userId;
		}

		String id = getObjectId( store, objectId, appId, username );
		String tag = genericStorageCache.getTag( id );
		if ( tag == null )
		{
			if ( ( !genericStorageDAO.checkExists( store, objectId, appId, username ) ) && ( !objectId.contains( "DEFAULT_HOME_SECTION" ) ) && ( !Arrays.asList( OBJECTS_ID ).contains( objectId ) ) )
			{
				throw new GenericStorageException( GenericStorageExceptionType.NOT_FOUND, "Generic storage for store:" + store + ", objectId:" + objectId + ", appId:" + appId + ", userId:" + username + " not found when querying" );
			}

			tag = genericStorageCache.createTag( id );
		}
		return tag;
	}

	public void deleteUserStore( String userId )
	{
		List<GenericStorageEntity> objects = genericStorageDAO.findByUserId( userId );
		for ( GenericStorageEntity object : objects )
		{
			genericStorageCache.removeObject( object.getTagId() );

			sendGenericStorageEvent( GenericStorageEventType.REMOVED, Store.USER, object.getObjectId(), object.getAppId(), object.getSize(), userId );
		}

		int deletedCount = genericStorageDAO.deleteByUserId( userId );
		LOG.info( "Deleted " + deletedCount + " generic storage objects for user " + userId );
	}

	private void sendGenericStorageEvent( GenericStorageEventType type, Store store, String objectId, String appId, long size, String username )
	{
		GenericStorageEvent event;

		if ( store == Store.USER )
		{
			event = new GenericStorageUserStoreEvent( type, objectId, appId, size, username );
		}
		else
		{
			event = new GenericStorageEvent( type, store, objectId, appId, size );
		}
		eventRegistry.sendEventAfterTransactionCommits( event );
	}

	private void checkQuery( Store store, String objectId, String providedUserId, String username ) throws GenericStorageException
	{
		if ( CommonAppUtils.isNullOrEmptyString( objectId ) )
		{
			throw new GenericStorageException( GenericStorageExceptionType.NO_OBJECT_ID, "Object id must be specified for generic storage" );
		}
		checkQuery( store, providedUserId, username );
	}

	private void checkQuery( Store store, String providedUserId, String username ) throws GenericStorageException
	{
		if ( ( store == Store.USER ) && ( CommonAppUtils.isNullOrEmptyString( providedUserId ) ) && ( CommonAppUtils.isNullOrEmptyString( username ) ) )
		{
			throw new GenericStorageException( GenericStorageExceptionType.NO_USER_ID, "Request must specify a user for user store" );
		}

		if ( ( store == Store.GLOBAL ) && ( !CommonAppUtils.isNullOrEmptyString( providedUserId ) ) )
		{
			throw new GenericStorageException( GenericStorageExceptionType.QUERY_ERROR, "User id can not be specified for global store" );
		}
	}

	private void checkUserRight( String currentUser, String user ) throws GenericStorageException
	{
		if ( currentUser == null )
		{
			return;
		}

		if ( currentUser.equals( user ) )
		{
			return;
		}

		boolean result = false;
		try
		{
			MemberView member = userService.getUser( currentUser );
			Set<RightEnum> rights = member.getAssembledRights();
			result = rights.contains( RightEnum.MANAGE_USERS );
			if ( ( result ) && ( !user.equals( "*" ) ) )
			{
				result = checkUserAccess( currentUser, user );
			}
		}
		catch ( UserException e )
		{
			LOG.error( "Error while checking user rights for " + currentUser + ", Exception: " + e.getMessage() );
			result = false;
		}

		if ( !result )
		{
			throw new GenericStorageException( GenericStorageExceptionType.USER_RIGHT, "User " + currentUser + " does not have sufficient rights to modify another user" );
		}
	}

	private boolean checkUserAccess( String currentUser, String user )
	{
		if ( currentUser.equals( user ) )
		{
			return true;
		}
		try
		{
			return userService.hasUserAccess( user );
		}
		catch ( UserException e )
		{
			LOG.error( "Error while checking user access of " + currentUser + " for " + user + ", Exception: " + e.getMessage() );
		}
		return false;
	}

	private String getObjectId( Store store, String objectId, String appId, String userId )
	{
		String appIdString = appId;
		if ( CommonAppUtils.isNullOrEmptyString( appIdString ) )
		{
			appIdString = null;
		}
		String userIdString = userId;
		if ( CommonAppUtils.isNullOrEmptyString( userIdString ) )
		{
			userIdString = null;
		}
		return store + "/" + objectId + "/" + appIdString + "/" + userIdString;
	}

	public void setGenericStorageDAO( GenericStorageDAO genericStorageDAO )
	{
		this.genericStorageDAO = genericStorageDAO;
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setGenericStorageCache( Cache<String, byte[]> genericStorageCache )
	{
		this.genericStorageCache = genericStorageCache;
	}
}

