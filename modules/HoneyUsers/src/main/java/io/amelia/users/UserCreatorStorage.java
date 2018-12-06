package io.amelia.users;

import io.amelia.lang.DescriptiveReason;
import io.amelia.lang.StorageException;
import io.amelia.lang.UserException;
import io.amelia.storage.HoneyPath;
import io.amelia.storage.backend.StorageBackend;
import io.amelia.storage.types.TableStorageType;
import io.amelia.support.NodePath;

class UserCreatorStorage extends UserCreator
{
	private final HoneyPath storagePath;

	public UserCreatorStorage( String name, StorageBackend storageBackend, NodePath storagePath, boolean isDefault ) throws StorageException.Error
	{
		super( name, isDefault );

		this.storagePath = storageBackend.getRootPath().resolve( storagePath );


		if ( !this.storagePath.supportsType( TableStorageType.class ); )
		throw new StorageException.Error( "Only the TableStorageType is supported." );
	}

	@Override
	public UserContext create( String uuid ) throws UserException.Error
	{
		return null;
	}

	@Override
	public boolean hasUser( String uuid )
	{
		return false;
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public void load()
	{
		TableStorageType tableStorage = storagePath.getStorageType( TableStorageType.class );

		tableStorage.getRecords().forEach( record -> {
			UserContext userContext = new UserContext( this, record.getString( "uuid" ) );
			userContext.setValues( record );
			HoneyUsers.users.add( userContext );
		} );
	}

	@Override
	public void loginBegin( UserContext userContext, UserPermissible userPermissible, String uuid, Object... credentials )
	{

	}

	@Override
	public void loginFailed( UserContext userContext, DescriptiveReason result )
	{

	}

	@Override
	public void loginSuccess( UserContext userContext )
	{

	}

	@Override
	public void loginSuccessInit( UserContext userContext, PermissibleEntity permissibleEntity )
	{

	}

	@Override
	public void reload( UserContext userContext ) throws UserException.Error
	{

	}

	@Override
	public UserResult resolve( String uuid )
	{
		return null;
	}

	@Override
	public void save( UserContext userContext ) throws UserException.Error
	{

	}
}
