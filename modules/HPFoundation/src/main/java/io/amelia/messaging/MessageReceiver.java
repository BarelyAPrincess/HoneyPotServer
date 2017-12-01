/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.messaging;

/**
 * Interfaces classes that can receive incoming messages from the {@link MessageDispatch}
 */
public interface MessageReceiver
{
	String getDisplayName();

	String getId();

	PermissibleEntity getPermissibleEntity();

	/**
	 * Sends message/objects to any and all CommandSenders currently logged in, referencing to this Account
	 * Addresses the message from CommandSender
	 *
	 * @param sender The CommandSender
	 * @param objs   The message/objects to deliver
	 */
	void sendMessage( MessageSender sender, Object... objs );

	/**
	 * Sends message/objects to any and all CommandSenders currently logged in, referencing to this Account
	 *
	 * @param objs The objects to dispatch
	 */
	void sendMessage( Object... objs );

	boolean validate();
}
