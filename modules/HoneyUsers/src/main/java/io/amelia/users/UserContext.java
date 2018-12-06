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

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.KeyValueGetterTrait;
import io.amelia.data.KeyValueSetterTrait;
import io.amelia.data.KeyValueTypesTrait;
import io.amelia.data.TypeBase;
import io.amelia.data.parcel.Parcel;
import io.amelia.lang.ParcelableException;
import io.amelia.lang.UserException;
import io.amelia.support.Encrypt;
import io.amelia.support.Voluntary;
import io.amelia.support.WeakReferenceList;
import io.amelia.users.auth.UserCredentials;

/**
 * Provides the starting point for all users and synchronizes them with their specified creator.
 * We aim for memory usage and references to be kept at a minimum.
 *
 * UserCreator (The Backend) -> UserContext (The User Details) -> UserMeta (The User Processed) -> UserInstance (The User Logged In and can have multiple instances)
 */
public class UserContext implements UserPrincipal, Comparable<UserContext>, KeyValueTypesTrait, KeyValueSetterTrait<Object, ParcelableException.Error>, KeyValueGetterTrait<Object, ParcelableException.Error>
{
	private final UserCreator creator;
	private final boolean isUnloadable;
	private final Parcel parcel = Parcel.empty();
	private final String uuid;
	private WeakReferenceList<UserEntity> instances = new WeakReferenceList<>();
	private UserCredentials lastUsedCredentials = null;
	private String name;
	private boolean unloaded = false;

	public UserContext( UserCreator creator, String uuid )
	{
		this( creator, uuid, true );
	}

	public UserContext( UserCreator creator, String uuid, boolean isUnloadable )
	{
		if ( !Encrypt.isUuidValid( uuid ) )
			throw new IllegalArgumentException( "The uuid is not valid!" );

		this.uuid = uuid;
		this.creator = creator;
		this.isUnloadable = isUnloadable;
		this.name = null;
	}

	@Override
	public int compareTo( UserContext other )
	{
		return uuid().compareToIgnoreCase( other.uuid() );
	}

	public UserCreator getCreator()
	{
		return creator;
	}

	public UserEntity getEntity()
	{
		UserEntity instance = new UserEntity( this );
		instances.add( instance );
		return instance;
	}

	@Override
	public Set<String> getKeys()
	{
		return parcel.getKeys();
	}

	public UserCredentials getLastUsedCredentials()
	{
		return lastUsedCredentials;
	}

	public Stream<UserEntity> getUserInstances()
	{
		return instances.stream();
	}

	@Override
	public Voluntary getValue( String key, Function<Object, Object> computeFunction )
	{
		return parcel.getValue( key, computeFunction );
	}

	@Override
	public Voluntary getValue( String key, Supplier<Object> supplier )
	{
		return parcel.getValue( key, supplier );
	}

	@Override
	public Voluntary getValue( @Nonnull String key )
	{
		return parcel.getValue( key );
	}

	@Override
	public Voluntary getValue()
	{
		return parcel.getValue();
	}

	@Override
	public boolean hasValue( String key )
	{
		return parcel.hasValue( key );
	}

	public boolean isUnloadable()
	{
		return isUnloadable;
	}

	@Override
	public String name()
	{
		return name;
	}

	public void save() throws UserException.Error
	{
		creator.save( this );
	}

	public void setLastUsedCredentials( UserCredentials lastUsedCredentials )
	{
		this.lastUsedCredentials = lastUsedCredentials;
	}

	@Override
	public void setValue( String key, Object value ) throws ParcelableException.Error
	{
		parcel.setValue( key, value );
	}

	@Override
	public void setValue( TypeBase type, Object value ) throws ParcelableException.Error
	{
		parcel.setValue( type, value );
	}

	@Override
	public void setValueIfAbsent( TypeBase.TypeWithDefault type ) throws ParcelableException.Error
	{
		parcel.setValue( type );
	}

	@Override
	public void setValueIfAbsent( String key, Object value ) throws ParcelableException.Error
	{
		parcel.setValueIfAbsent( key, value );
	}

	public void unload() throws UserException.Error
	{
		if ( !isUnloadable )
			throw new UserException.Error( this, uuid() + " can't be unloaded." );
		HoneyUsers.users.remove( this );
		unloaded = true;
	}

	@Override
	public String uuid()
	{
		return uuid;
	}

	public void validate() throws UserException.Error
	{
		if ( unloaded )
			throw new UserException.Error( this, uuid() + " has already been unloaded!" );
	}
}
