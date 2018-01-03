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
