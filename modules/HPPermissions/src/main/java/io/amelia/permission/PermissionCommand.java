/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission;

import io.amelia.permission.commands.EntityCommands;
import io.amelia.permission.commands.GroupCommands;
import io.amelia.permission.commands.PermissionCommands;
import io.amelia.permission.commands.PromotionCommands;
import io.amelia.permission.commands.ReferenceCommands;
import io.amelia.permission.commands.UtilityCommands;

public class PermissionCommand extends AdvancedCommand
{
	public PermissionCommand()
	{
		super( "pex" );
		setAliases( "perms" );
		
		register( new GroupCommands() );
		register( new PromotionCommands() );
		register( new EntityCommands() );
		register( new UtilityCommands() );
		register( new ReferenceCommands() );
		register( new PermissionCommands() );
	}
}
