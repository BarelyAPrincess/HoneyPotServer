/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support.data;

import java.util.Arrays;
import java.util.EnumSet;

public class StackerListener
{
	private StackerListener()
	{

	}

	public enum Flags
	{
		FIRE_ONCE,
		NO_RECURSIVE
	}

	@FunctionalInterface
	public interface OnChildAdd<B>
	{
		void listen( B target );
	}

	@FunctionalInterface
	public interface OnChildRemove<B>
	{
		void listen( B target, B orphan );
	}

	@FunctionalInterface
	public interface OnValueChange<B, T>
	{
		void listen( B target, T oldValue, T newValue );
	}

	@FunctionalInterface
	public interface OnValueRemove<B, T>
	{
		void listen( B target, T oldValue );
	}

	@FunctionalInterface
	public interface OnValueStore<B, T>
	{
		void listen( B target, T newValue );
	}

	static abstract class Container
	{
		final EnumSet<Flags> flags;
		final int type;

		public Container( int type, Flags... flags )
		{
			this.type = type;
			this.flags = EnumSet.copyOf( Arrays.asList( flags ) );
		}

		abstract void call( Object[] objs );
	}
}
