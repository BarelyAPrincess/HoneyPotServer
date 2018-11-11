/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.networking.messages;

import java.util.function.Supplier;

public class Messages
{
	public static final Supplier<ApplicationInfoMessage> MESSAGE_APPLICATION_INFO = ApplicationInfoMessage::new;

	private Messages()
	{
		// Static Class
	}
}
