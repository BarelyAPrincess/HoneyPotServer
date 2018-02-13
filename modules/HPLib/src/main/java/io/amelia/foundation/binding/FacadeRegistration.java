/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.binding;

import java.util.TreeSet;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
import io.amelia.support.Lists;
import io.amelia.support.Objs;

public class FacadeRegistration<T extends FacadeBinding>
{
	private final TreeSet<Entry<T>> registrations = new TreeSet<>();
	private final Class<T> serviceClass;
	private boolean isStrict;

	public FacadeRegistration( Class<T> serviceClass )
	{
		this.serviceClass = serviceClass;
	}

	public boolean add( Entry<T> registration )
	{
		if ( isStrict && registrations.size() > 0 )
			throw new BindingException.Ignorable( "The facade registration for \"" + serviceClass.getSimpleName() + "\" is strict, there for, only allowing for singular registration." );

		return registrations.add( registration );
	}

	public Class<T> getBindingClass()
	{
		return serviceClass;
	}

	public Entry getFacadeRegistration( FacadePriority facadePriority )
	{
		return Lists.compute( registrations, reg -> reg.getPriority() == facadePriority );
	}

	public <R extends FacadeBinding> R getHighestPriority()
	{
		return Objs.ifPresentGet( registrations.last(), entry -> ( R ) entry.getBinding() );
	}

	public FacadeBinding getLowestPriority()
	{
		return Objs.ifPresentGet( registrations.first(), Entry::getBinding );
	}

	public boolean isPriorityRegistered( FacadePriority priority )
	{
		return getFacadeRegistration( priority ) != null;
	}

	public boolean isStrict()
	{
		return isStrict;
	}

	public boolean remove( FacadePriority facadePriority )
	{
		return registrations.removeIf( reg -> reg.getPriority() == facadePriority );
	}

	public boolean setStrict( boolean isStrict )
	{
		if ( isStrict && registrations.size() > 1 )
			return false;

		this.isStrict = isStrict;
		return true;
	}

	public static class Entry<T extends FacadeBinding> implements Comparable<Entry<?>>
	{
		private final FacadePriority priority;
		private final Supplier<T> supplier;
		private T instance = null;

		public Entry( @Nonnull Supplier<T> supplier, @Nonnull FacadePriority priority )
		{
			this.supplier = supplier;
			this.priority = priority;
		}

		@Override
		public int compareTo( Entry<?> other )
		{
			if ( priority.ordinal() == other.getPriority().ordinal() )
				return 0;
			else
				return priority.ordinal() < other.getPriority().ordinal() ? 1 : -1;
		}

		public void destroy()
		{
			try
			{
				if ( instance != null )
					instance.destroy();
			}
			catch ( ApplicationException.Error e )
			{
				Kernel.handleExceptions( e );
			}
			finally
			{
				instance = null;
			}
		}

		public T getBinding()
		{
			if ( instance == null )
				instance = supplier.get();
			return instance;
		}

		public FacadePriority getPriority()
		{
			return priority;
		}
	}
}
