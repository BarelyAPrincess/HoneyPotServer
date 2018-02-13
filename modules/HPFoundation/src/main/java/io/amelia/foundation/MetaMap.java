/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.ReportingLevel;
import io.amelia.support.Lists;
import io.amelia.support.Maps;
import io.amelia.support.Objs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MetaMap extends HashMap<String, Object>
{
	public MetaMap()
	{

	}

	public MetaMap( Map<String, Object> oldMap )
	{
		putAll( oldMap );
	}

	public final List<String> getList( String key )
	{
		return getList( key, "|" );
	}

	public final List<String> getList( String key, String regex )
	{
		return getString( key, s -> Arrays.asList( s.split( regex ) ) );
	}

	public final <R> List<R> getList( String key, String regex, Function<String, R> function )
	{
		return Lists.walk( getList( key, regex ), function );
	}

	@SuppressWarnings( "Unchecked" )
	public final MetaMap getMap( Object key )
	{
		Object value = get( key );

		if ( value instanceof Map )
			return Maps.builder( ( Map<Object, Object> ) value ).castTo( String.class, Object.class ).map( MetaMap::new );
		if ( value instanceof List )
			return Maps.builder().increment( ( List<Object> ) value ).castTo( String.class, Object.class ).map( MetaMap::new );
		if ( value instanceof ConfigMap )
			return new MetaMap( ( ( ConfigMap ) value ).values() );
		return null;
	}

	public final String getRequired( String key, String msg ) throws ApplicationException.Runtime
	{
		if ( !containsKey( key ) || getString( key ) == null )
			throw new ApplicationException.Runtime( ReportingLevel.E_STRICT, msg );
		return getString( key );
	}

	public final <R> R getString( String key, Function<String, R> function )
	{
		return containsKey( key ) ? function.apply( getString( key ) ) : null;
	}

	public final String getString( Object key )
	{
		return Objs.castToString( get( key ) );
	}
}
