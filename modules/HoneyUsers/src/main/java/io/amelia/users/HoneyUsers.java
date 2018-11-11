/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.TypeBase;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ConfigException;
import io.amelia.lang.DescriptiveReason;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.UserException;
import io.amelia.storage.backend.StorageBackend;
import io.amelia.support.Encrypt;
import io.amelia.support.Streams;
import io.amelia.support.Strs;

public class HoneyUsers
{
	public static final Kernel.Logger L = Kernel.getLogger( HoneyUsers.class );
	public static final UserCreatorMemory MEMORY = new UserCreatorMemory();
	public static final UserContext USER_NULL;
	public static final UserContext USER_ROOT;
	public static final ReportingLevel[] reportingLevelSeverityArray = new ReportingLevel[] {ReportingLevel.E_ERROR, ReportingLevel.L_SECURITY, ReportingLevel.L_ERROR, ReportingLevel.L_EXPIRED, ReportingLevel.L_DENIED};
	static volatile Set<UserContext> users = new CopyOnWriteArraySet<>();
	private static volatile Set<UserCreator> creators = new CopyOnWriteArraySet<>();
	private static boolean isDebugEnabled = ConfigRegistry.config.getValue( ConfigKeys.DEBUG_ENABLED );

	static
	{
		creators.add( MEMORY );

		try
		{
			ConfigRegistry.config.setValueIfAbsent( ConfigKeys.UUID_NULL );
			ConfigRegistry.config.setValueIfAbsent( ConfigKeys.UUID_ROOT );

			USER_NULL = new UserContext( MEMORY, ConfigRegistry.config.getValue( ConfigKeys.UUID_NULL ), false );
			USER_ROOT = new UserContext( MEMORY, ConfigRegistry.config.getValue( ConfigKeys.UUID_ROOT ), false );
		}
		catch ( ConfigException.Error e )
		{
			throw new ApplicationException.Runtime( e );
		}
	}

	public static void addCreator( String name, StorageBackend storageBackend, boolean isDefault )
	{
		UserCreator creator = new UserCreatorStorage( name, storageBackend, isDefault );
		creators.add( creator );
		creator.load();
	}

	public static UserContext createUser( @Nonnull String uuid ) throws UserException.Error
	{
		return createUser( uuid, getDefaultBackend() );
	}

	public static UserContext createUser( @Nonnull String uuid, @Nonnull UserCreator userCreator ) throws UserException.Error
	{
		if ( !Encrypt.isUuidValid( uuid ) )
			throw new IllegalArgumentException( "UUID is not valid!" );
		if ( !userCreator.isEnabled() )
			throw new UserException.Error( null, DescriptiveReason.FEATURE_DISABLED.getReportingLevel(), DescriptiveReason.FEATURE_DISABLED.getReasonMessage() );
		UserContext userContext = userCreator.create( uuid );
		users.add( userContext );
		return userContext;
	}

	public static String generateUuid()
	{
		String uuid;
		do
			uuid = Encrypt.uuid();
		while ( userExists( uuid ) );
		return uuid;
	}

	public static Optional<UserCreator> getBackend( String name )
	{
		return getCreators().filter( backend -> name.equalsIgnoreCase( backend.name() ) ).findAny();
	}

	public static Stream<UserCreator> getCreators()
	{
		return creators.stream();
	}

	public static UserCreator getDefaultBackend()
	{
		return getCreators().filter( UserCreator::isDefault ).filter( UserCreator::isEnabled ).findAny().orElse( MEMORY );
	}

	public static Stream<UserContext> getUsers()
	{
		return users.stream();
	}

	public static boolean isDebugEnabled()
	{
		return isDebugEnabled;
	}

	public static boolean isNullUser( UserPrincipal userPrincipal )
	{
		return USER_NULL.uuid().equalsIgnoreCase( userPrincipal.uuid() );
	}

	public static boolean isNullUser( String uuid )
	{
		return USER_NULL.uuid().equalsIgnoreCase( uuid );
	}

	public static boolean isRootUser( UserPrincipal userPrincipal )
	{
		return USER_ROOT.uuid().equalsIgnoreCase( userPrincipal.uuid() );
	}

	public static boolean isRootUser( String uuid )
	{
		return USER_ROOT.uuid().equalsIgnoreCase( uuid );
	}

	static void put( UserContext userContext ) throws UserException.Error
	{
		if ( Strs.matchesIgnoreCase( userContext.uuid(), USER_NULL.uuid(), USER_ROOT.uuid() ) )
			throw new UserException.Error( userContext, DescriptiveReason.INTERNAL_ERROR );
		if ( users.stream().anyMatch( user -> user.compareTo( userContext ) == 0 ) )
			return;
		userContext.validate();
		users.add( userContext );
	}

	public static void setDebugEnabled( boolean isDebugEnabled )
	{
		HoneyUsers.isDebugEnabled = isDebugEnabled;
	}

	static void unload( @Nonnull String uuid ) throws UserException.Error
	{
		Streams.forEachWithException( users.stream().filter( user -> uuid.equals( user.uuid() ) ), UserContext::unload );
	}

	static void unload() throws UserException.Error
	{
		// TODO
		Streams.forEachWithException( users.stream(), UserContext::unload );
	}

	public static boolean userExists( @Nonnull String uuid )
	{
		return getUsers().anyMatch( user -> uuid.equals( user.uuid() ) );
	}

	private HoneyUsers()
	{
		// Static Access
	}

	/**
	 * Locates the user from the User Creator.
	 * Does not authenticate.
	 */
	public UserResult getUser( @Nonnull String uuid )
	{
		UserResult userResult = new UserResult( uuid );

		if ( USER_NULL.uuid().equalsIgnoreCase( uuid ) )
		{
			userResult.setUser( USER_NULL );
			userResult.setDescriptiveReason( DescriptiveReason.LOGIN_SUCCESS );
			return userResult;
		}

		if ( USER_ROOT.uuid().equalsIgnoreCase( uuid ) )
		{
			userResult.setUser( USER_ROOT );
			userResult.setDescriptiveReason( DescriptiveReason.LOGIN_SUCCESS );
			return userResult;
		}

		Optional<UserContext> foundResult = getUsers().filter( user -> uuid.equals( user.uuid() ) ).findAny();
		if ( foundResult.isPresent() )
		{
			userResult.setUser( foundResult.get() );
			userResult.setDescriptiveReason( DescriptiveReason.LOGIN_SUCCESS );
			return userResult;
		}

		List<UserResult> pendingUserResults = new ArrayList<>();

		for ( UserCreator creator : creators )
		{
			userResult = creator.resolve( uuid );

			if ( userResult == null )
				continue;
			if ( userResult.getReportingLevel().isSuccess() )
				return userResult;

			if ( isDebugEnabled )
				L.info( "Failure in creator " + creator.getClass().getSimpleName() + ". {descriptionMessage=" + userResult.getDescriptiveReason().getReasonMessage() + "}" );
			if ( isDebugEnabled && userResult.hasException() )
				userResult.getThrowable().printStackTrace();

			pendingUserResults.add( userResult );
		}

		// Sort ReportingLevels based on their position in the reportingLevelSeverityArray.
		pendingUserResults.sort( ( left, right ) -> {
			int leftSeverity = Arrays.binarySearch( reportingLevelSeverityArray, left.getReportingLevel() );
			int rightSeverity = Arrays.binarySearch( reportingLevelSeverityArray, right.getReportingLevel() );
			return Integer.compare( leftSeverity >= 0 ? leftSeverity : Integer.MAX_VALUE, rightSeverity >= 0 ? rightSeverity : Integer.MAX_VALUE );
		} );

		userResult = pendingUserResults.stream().findFirst().orElse( null );

		if ( userResult == null )
		{
			userResult = new UserResult( uuid );
			userResult.setDescriptiveReason( DescriptiveReason.INCORRECT_LOGIN );
			return userResult;
		}

		return userResult;
	}

	public static class ConfigKeys
	{
		public static final TypeBase USERS_BASE = new TypeBase( "users" );
		public static final TypeBase.TypeInteger MAX_LOGINS = new TypeBase.TypeInteger( USERS_BASE, "maxLogins", -1 );
		public static final TypeBase CREATORS = new TypeBase( USERS_BASE, "creators" );
		public static final TypeBase.TypeBoolean DEBUG_ENABLED = new TypeBase.TypeBoolean( USERS_BASE, "debugEnabled", false );

		public static final TypeBase.TypeString UUID_NULL = new TypeBase.TypeString( USERS_BASE, "nullUuid", Encrypt.uuid() );
		public static final TypeBase.TypeString UUID_ROOT = new TypeBase.TypeString( USERS_BASE, "rootUuid", Encrypt.uuid() );
	}
}
