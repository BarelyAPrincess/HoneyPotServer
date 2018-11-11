package io.amelia.users;

import io.amelia.events.EventHandler;
import io.amelia.lang.DescriptiveReason;
import io.amelia.lang.ParcelableException;
import io.amelia.lang.UserException;
import io.amelia.support.DateAndTime;

class UserCreatorMemory extends UserCreator
{
	public UserCreatorMemory()
	{
		super( "memory", HoneyUsers.getCreators().noneMatch( UserCreator::isDefault ) );
	}

	@Override
	public UserContext create( String uuid ) throws UserException.Error
	{
		UserContext context = new UserContext( this, uuid );
		try
		{
			context.setValue( "data", DateAndTime.epoch() );
		}
		catch ( ParcelableException.Error e )
		{
			throw new UserException.Error( context, e );
		}
		return context;
	}

	@Override
	public boolean hasUser( String uuid )
	{
		return HoneyUsers.isNullUser( uuid ) || HoneyUsers.isRootUser( uuid );
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public void load()
	{
		// Do Nothing
	}

	@Override
	public void loginBegin( UserContext userContext, UserPermissible userPermissible, String acctId, Object... credentials )
	{
		// Do Nothing
	}

	@Override
	public void loginFailed( UserContext userContext, DescriptiveReason result )
	{
		// Do Nothing
	}

	@Override
	public void loginSuccess( UserContext userContext )
	{
		// Do Nothing
	}

	@Override
	public void loginSuccessInit( UserContext userContext, PermissibleEntity permissibleEntity )
	{
		if ( userContext.getCreator() == this && HoneyUsers.isRootUser( userContext ) )
		{
			entity.addPermission( PermissionDefault.OP.getNode(), true, null );
			entity.setVirtual( true );
			// meta.registerAttachment( ApplicationTerminal.terminal() );
		}

		if ( userContext.getCreator() == this && HoneyUsers.isNullUser( userContext ) )
			entity.setVirtual( true );
	}

	@EventHandler
	public void onPermissibleEntityEvent( PermissibleEntityEvent event )
	{
		// XXX Prevent the root user from losing it's OP permissions
		if ( event.getAction() == Action.PERMISSIONS_CHANGED )
			if ( AccountType.isRootAccount( event.getEntity() ) )
			{
				event.getEntity().addPermission( PermissionDefault.OP.getNode(), true, null );
				event.getEntity().setVirtual( true );
			}
	}

	@Override
	public void reload( UserContext userContext )
	{
		// Do Nothing
	}

	@Override
	public UserResult resolve( String uuid )
	{
		return null;
	}

	@Override
	public void save( UserContext userContext )
	{
		// Do Nothing
	}
}
