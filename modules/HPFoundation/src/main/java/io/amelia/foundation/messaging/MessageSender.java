/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.messaging;

/**
 * Represents entities with the ability to send messages thru the {@link MessageDispatch}
 */
public interface MessageSender
{
	String getDisplayName();
	
	String getId();
	
	PermissibleEntity getPermissibleEntity();
}
