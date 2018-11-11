package io.amelia.users;

public class UserEntity implements UserPrincipal
{
	private final UserContext userContext;

	UserEntity( UserContext userContext )
	{
		this.userContext = userContext;
	}

	@Override
	public String uuid()
	{
		return userContext.uuid();
	}

	@Override
	public String name()
	{
		return userContext.name();
	}
}
