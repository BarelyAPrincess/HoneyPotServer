package io.amelia.foundation.facades;

import io.amelia.foundation.binding.AppBindings;
import io.amelia.foundation.facades.interfaces.PermissionService;

public class Permissions
{
	public static PermissionService get()
	{
		return AppBindings.getFacade( PermissionService.class );
	}


}
