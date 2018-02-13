/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class TriEnum<E extends Enum<E>>
{
	private final Set<E> allowed = new HashSet<>();
	private final Set<E> denied = new HashSet<>();

	public TriEnum()
	{

	}

	public TriEnum( E... enums )
	{
		allow( enums );
	}

	public void allow( E... enums )
	{
		for ( E e : enums )
		{
			allowed.add( e );
			denied.remove( e );
		}
	}

	public void deny( E... enums )
	{
		for ( E e : enums )
		{
			allowed.remove( e );
			denied.add( e );
		}
	}

	public boolean isAllowed( E e )
	{
		return allowed.contains( e ) && !denied.contains( e );
	}

	public boolean isDenied( E e )
	{
		return !allowed.contains( e ) && denied.contains( e );
	}

	public boolean isUnset( E e )
	{
		return !allowed.contains( e ) && !denied.contains( e );
	}

	public EnumSet<E> toEnumSet()
	{
		return EnumSet.copyOf( allowed );
	}

	public void unset( E... enums )
	{
		for ( E e : enums )
		{
			allowed.remove( e );
			denied.remove( e );
		}
	}
}
