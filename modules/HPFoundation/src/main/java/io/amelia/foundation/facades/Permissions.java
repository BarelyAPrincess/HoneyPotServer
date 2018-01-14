package io.amelia.foundation.facades;

public class Permissions
{
	public static PermissionService get()
	{
		return Facades.getFacade( PermissionService.class );
	}


}
