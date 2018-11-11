package io.amelia.users;

public class GroupEntity implements GroupPrincipal
{
	private final String name;
	private final String uuid;

	public GroupEntity( String uuid, String name )
	{
		this.uuid = uuid;
		this.name = name;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public String uuid()
	{
		return uuid;
	}
}
