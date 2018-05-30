/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.networking;

import io.amelia.foundation.Kernel;
import io.amelia.foundation.binding.Bindings;

public class Networking
{
	public static final Kernel.Logger L = Kernel.getLogger( Networking.class );

	public static NetworkingService i()
	{
		return Bindings.getSystemNamespace().getFacadeBinding( "", NetworkingService.class );
	}

	public static class HTTP
	{
		private HTTP()
		{
			// Static Access
		}


	}

	public static class TCP
	{
		private TCP()
		{
			// Static Access
		}


	}

	public static class UDP
	{
		private UDP()
		{
			// Static Access
		}


	}
}
