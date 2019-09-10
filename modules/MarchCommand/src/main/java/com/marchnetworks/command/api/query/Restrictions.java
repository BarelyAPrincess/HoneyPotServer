package com.marchnetworks.command.api.query;

import java.util.Collection;

public class Restrictions
{
	public static Restriction eq( String name, Object value )
	{
		return new Restriction( name, value, RestrictionType.EQUAL );
	}

	public static Restriction ne( String name, Object value )
	{
		return new Restriction( name, value, RestrictionType.NOT_EQUAL );
	}

	public static Restriction in( String name, Object[] value )
	{
		return new Restriction( name, value, RestrictionType.IN );
	}

	public static Restriction in( String name, Collection<?> value )
	{
		return new Restriction( name, value, RestrictionType.IN );
	}

	public static Restriction isNotEmpty( String name )
	{
		return new Restriction( name, null, RestrictionType.IS_NOT_EMPTY );
	}

	public static Restriction contains( String name, Object value )
	{
		return new Restriction( name, value, RestrictionType.CONTAINS );
	}

	public static Restriction ni( String name, Collection<?> value )
	{
		return new Restriction( name, value, RestrictionType.NOT_IN );
	}

	public static Restriction gt( String name, Number value )
	{
		return new Restriction( name, value, RestrictionType.GREATER_THAN );
	}

	public static Restriction lt( String name, Number value )
	{
		return new Restriction( name, value, RestrictionType.LESS_THAN );
	}
}
