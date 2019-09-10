package com.marchnetworks.command.api.query;

import com.marchnetworks.command.common.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Criteria
{
	private Class<?> targetClass;
	private List<Restriction> restrictions = new ArrayList( 1 );
	private Map<String, Field> fieldCache = new HashMap();

	public Criteria( Class<?> targetClass )
	{
		this.targetClass = targetClass;
	}

	public Criteria()
	{
	}

	public Criteria add( Restriction restriction )
	{
		restriction.setCriteria( this );
		restrictions.add( restriction );
		return this;
	}

	public boolean match( Object object )
	{
		if ( ( targetClass != null ) && ( !targetClass.isAssignableFrom( object.getClass() ) ) )
			return false;

		for ( Restriction restriction : restrictions )
			if ( !restriction.match( object ) )
				return false;

		return true;
	}

	public void clear()
	{
		restrictions.clear();
	}

	public Class<?> getTargetClass()
	{
		return targetClass;
	}

	public Field getField( String path, String fieldName, Class<?> fieldClass )
	{
		Field cached = ( Field ) fieldCache.get( path );
		if ( cached == null )
		{
			cached = ReflectionUtils.getDeclaredField( fieldName, fieldClass );
			fieldCache.put( path, cached );
		}
		return cached;
	}
}
