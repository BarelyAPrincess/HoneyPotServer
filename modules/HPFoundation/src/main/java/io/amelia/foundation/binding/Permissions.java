package io.amelia.foundation.binding;

public class Permissions
{
	public static final String NAMESPACE_PERMISSIONS = "io.amelia.permissions";

	public static PermissionBinding get()
	{
		return Bindings.getSystemNamespace().getFacadeBinding( NAMESPACE_PERMISSIONS, PermissionBinding.class );
	}


}
