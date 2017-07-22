/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import com.chiorichan.tasks.Timings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.amelia.storage.DatabaseColumnType;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Provides basic methods for database convenience
 */
public class UtilDatabase
{
	private UtilDatabase()
	{

	}

	@SuppressWarnings( "serial" )
	private static class FoundException extends Exception
	{
		int matchingType = 0;

		FoundException( int matchingType )
		{
			this.matchingType = matchingType;
		}

		int getType()
		{
			return matchingType;
		}
	}

	/**
	 * Provides an easy way to catch a matching type without tons of if...then statements
	 */
	private static class TypeCatcher
	{
		Class<?> origType;

		public TypeCatcher( Class<?> origType )
		{
			this.origType = origType;
		}

		public void check( Class<?> clz, int matchingType ) throws FoundException
		{
			if ( origType == clz )
				throw new FoundException( matchingType );
		}
	}

	public static String escape( String str )
	{
		if ( str == null )
			return null;

		if ( str.replaceAll( "[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/? ]", "" ).length() < 1 )
			return str;

		String cleanString = str;
		cleanString = cleanString.replaceAll( "\\\\", "\\\\\\\\" );
		cleanString = cleanString.replaceAll( "\\n", "\\\\n" );
		cleanString = cleanString.replaceAll( "\\r", "\\\\r" );
		cleanString = cleanString.replaceAll( "\\t", "\\\\t" );
		cleanString = cleanString.replaceAll( "\\00", "\\\\0" );
		cleanString = cleanString.replaceAll( "'", "\\\\'" );
		cleanString = cleanString.replaceAll( "\\\"", "\\\\\"" );

		return cleanString;
	}

	public static int getColumnType( Class<?> clz )
	{
		TypeCatcher tc = new TypeCatcher( clz );

		try
		{
			tc.check( Integer.class, Types.INTEGER );
			tc.check( int.class, Types.INTEGER );
			tc.check( Boolean.class, Types.TINYINT );
			tc.check( boolean.class, Types.TINYINT );
			tc.check( float.class, Types.FLOAT );
			tc.check( Float.class, Types.FLOAT );
			tc.check( double.class, Types.DOUBLE );
			tc.check( Double.class, Types.DOUBLE );
			tc.check( String.class, Types.VARCHAR );
		}
		catch ( FoundException e )
		{
			return e.getType();
		}

		return Types.NULL;
	}

	private static <T, K, V> void moveNext( Map<T, Map<K, V>> result, int key )
	{
		if ( !result.containsKey( key ) )
			return;

		Map<K, V> row = result.remove( key );
		Integer nextKey = key + 1;
		if ( result.containsKey( nextKey ) )
			moveNext( result, nextKey );
		result.put( ( T ) nextKey, row );
	}

	private static <T, K, V> void movePrevious( Map<T, Map<K, V>> result, int key )
	{
		if ( !result.containsKey( key ) )
			return;

		Map<K, V> row = result.remove( key );
		Integer nextKey = key - 1;
		if ( result.containsKey( nextKey ) )
			movePrevious( result, nextKey );
		result.put( ( T ) nextKey, row );
	}

	public static DatabaseColumnType objectToColumnType( Object obj )
	{
		if ( obj instanceof Boolean )
			return DatabaseColumnType.BOOLEAN;
		if ( obj instanceof Integer )
			return DatabaseColumnType.INTEGER;
		if ( obj instanceof String )
			if ( ( ( String ) obj ).length() < 256 )
				return DatabaseColumnType.STRING;
			else // if ( ( ( String ) obj ).length() < 65536 )
				return DatabaseColumnType.TEXT;
			/*else if ( ( ( String ) obj ).length() < 16777216 )
				return "MEDIUMTEXT";
			else
				return "LONGTEXT";*/
		return DatabaseColumnType.STRING;
	}

	public static String objectToSqlType( Object obj )
	{
		if ( obj instanceof String )
			if ( ( ( String ) obj ).length() < 256 )
				return "VARCHAR(255)";
			else if ( ( ( String ) obj ).length() < 65536 )
				return "TEXT";
			else if ( ( ( String ) obj ).length() < 16777216 )
				return "MEDIUMTEXT";
			else
				return "LONGTEXT";
		if ( obj instanceof BigDecimal )
			return "DECIMAL(20,6)";
		if ( obj instanceof Float )
			return "REAL(24,2)";
		if ( obj instanceof Double )
			return "DOUBLE(24,2)";
		if ( obj instanceof Boolean )
			return "TINYINT(1)";
		if ( obj instanceof Byte )
			return "TINYINT(1)";
		if ( obj instanceof Short )
			return "SMALLINT(65535)";
		if ( obj instanceof Long )
			return "BIGINT(" + Integer.MAX_VALUE + ")";
		if ( obj instanceof Integer )
			return "INT(" + Integer.MAX_VALUE + ")";
		if ( obj instanceof Date )
			return "DATE";
		if ( obj instanceof Time )
			return "TIME";
		if ( obj instanceof Timestamp )
			return "TIMESTAMP";
		return "BLOB";
	}

	public static String objectToSqlType( Object obj, int size )
	{
		if ( obj instanceof String )
			if ( ( ( String ) obj ).length() < 256 )
				return "VARCHAR(" + ( size < 256 ? size : 255 ) + ")";
			else if ( ( ( String ) obj ).length() < 65536 )
				return "TEXT";
			else if ( ( ( String ) obj ).length() < 16777216 )
				return "MEDIUMTEXT";
			else
				return "LONGTEXT";
		if ( obj instanceof BigDecimal )
			return "DECIMAL(" + size + ",2)";
		if ( obj instanceof Float )
			return "REAL(" + size + ",2)";
		if ( obj instanceof Double )
			return "DOUBLE(" + size + ",2)";
		if ( obj instanceof Boolean )
			return "TINYINT(1)";
		if ( obj instanceof Byte )
			return "TINYINT(1)";
		if ( obj instanceof Short )
			return "SMALLINT(" + size + ")";
		if ( obj instanceof Long )
			return "BIGINT(" + size + ")";
		if ( obj instanceof Integer )
			return "INT(" + size + ")";
		if ( obj instanceof Date )
			return "DATE";
		if ( obj instanceof Time )
			return "TIME";
		if ( obj instanceof Timestamp )
			return "TIMESTAMP";
		return "BLOB";
	}

	public static Map<String, Map<String, Object>> resultToMap( ResultSet rs ) throws SQLException
	{
		LinkedHashMap<String, Map<String, Object>> result = Maps.newLinkedHashMap();
		int x = 0;

		if ( rs == null )
			return result;

		boolean next = rs.isFirst() || rs.next();
		while ( next )
		{
			result.put( Integer.toString( x ), rowToMap( rs ) );
			x++;
			next = rs.next();
		}

		return result;
	}

	public static Set<Map<String, Object>> resultToSet( ResultSet rs ) throws SQLException
	{
		LinkedHashSet<Map<String, Object>> result = Sets.newLinkedHashSet();

		if ( rs == null )
			return result;

		boolean next = rs.isFirst() || rs.next();
		while ( next )
		{
			result.add( rowToMap( rs ) );
			next = rs.next();
		}

		return result;
	}

	public static Map<String, Map<String, String>> resultToStringMap( ResultSet rs ) throws SQLException
	{
		LinkedHashMap<String, Map<String, String>> result = Maps.newLinkedHashMap();
		int x = 0;

		if ( rs == null )
			return result;

		boolean next = rs.isFirst() || rs.next();
		while ( next )
		{
			result.put( "" + x, Objs.castMap( rowToMap( rs ), String.class, String.class ) );
			x++;
			next = rs.next();
		}

		return result;
	}

	public static Set<Map<String, String>> resultToStringSet( ResultSet rs ) throws SQLException
	{
		LinkedHashSet<Map<String, String>> result = Sets.newLinkedHashSet();

		if ( rs == null )
			return result;

		boolean next = rs.isFirst() || rs.next();
		while ( next )
		{
			result.add( rowToStringMap( rs ) );
			next = rs.next();
		}

		return result;
	}

	@Deprecated
	public static int rowCount( ResultSet result )
	{
		try
		{
			if ( result == null )
				return -1;

			if ( result.getType() == ResultSet.TYPE_FORWARD_ONLY )
				throw new IllegalStateException( "The ResultSet type is TYPE_FORWARD_ONLY, using this method to count rows does not work on this type." );

			int row = result.getRow();
			result.last();
			int lastRow = result.getRow();
			result.absolute( row );
			return lastRow;
		}
		catch ( SQLException e )
		{
			e.printStackTrace();
			return -1;
		}
	}

	public static Map<String, Object> rowToMap( ResultSet rs ) throws SQLException
	{
		Map<String, Object> result = Maps.newLinkedHashMap();

		if ( rs == null )
			return null;

		if ( rs.isBeforeFirst() && !rs.next() )
			return null;

		ResultSetMetaData rsmd = rs.getMetaData();

		int numColumns = rsmd.getColumnCount();

		for ( int i = 1; i < numColumns + 1; i++ )
		{
			String columnName = rsmd.getColumnName( i );

			// DatabaseManager.getLogger().info( "Column: " + columnName + " <-> " + rsmd.getColumnTypeName( i ) );

			if ( rsmd.getColumnType( i ) == Types.ARRAY )
				result.put( columnName, rs.getArray( columnName ).getArray() );
			else if ( rsmd.getColumnType( i ) == Types.BIGINT )
				result.put( columnName, rs.getInt( columnName ) );
			else if ( rsmd.getColumnType( i ) == Types.TINYINT )
				result.put( columnName, rs.getInt( columnName ) );
			else if ( rsmd.getColumnType( i ) == Types.BIT )
				result.put( columnName, rs.getInt( columnName ) );
			else if ( rsmd.getColumnType( i ) == Types.BOOLEAN )
				result.put( columnName, rs.getBoolean( columnName ) );
			else if ( rsmd.getColumnTypeName( i ).contains( "BLOB" ) || rsmd.getColumnType( i ) == Types.BINARY )
			{
				// BLOB = Max Length 65,535. Recommended that you use a LONGBLOG.
				byte[] bytes = rs.getBytes( columnName );
				result.put( columnName, bytes );
				/*
				 * try
				 * {
				 * result.put( columnName, new String( bytes, "ISO-8859-1" ) );
				 * }
				 * catch ( UnsupportedEncodingException e )
				 * {
				 * e.printStackTrace();
				 * }
				 */
			}
			else if ( rsmd.getColumnType( i ) == Types.DOUBLE )
				result.put( columnName, rs.getDouble( columnName ) );
			else if ( rsmd.getColumnType( i ) == Types.FLOAT )
				result.put( columnName, rs.getFloat( columnName ) );
			else if ( rsmd.getColumnTypeName( i ).equals( "INT" ) )
				result.put( columnName, rs.getInt( columnName ) );
			else if ( rsmd.getColumnType( i ) == Types.NVARCHAR )
				result.put( columnName, rs.getNString( columnName ) );
			else if ( rsmd.getColumnTypeName( i ).equals( "VARCHAR" ) )
				result.put( columnName, rs.getString( columnName ) );
			else if ( rsmd.getColumnType( i ) == Types.SMALLINT )
				result.put( columnName, rs.getInt( columnName ) );
			else if ( rsmd.getColumnType( i ) == Types.DATE )
				result.put( columnName, rs.getDate( columnName ) );
			else if ( rsmd.getColumnType( i ) == Types.TIMESTAMP )
				result.put( columnName, rs.getTimestamp( columnName ) );
			else
				result.put( columnName, rs.getObject( columnName ) );
		}

		return result;
	}

	public static Map<String, String> rowToStringMap( ResultSet rs ) throws SQLException
	{
		return Objs.castMap( rowToMap( rs ), String.class, String.class );
	}

	public static <K, V> Map<String, Map<K, V>> sortByColumnValue( Map<?, Map<K, V>> orig, String keyName )
	{
		return sortByColumnValue( orig, keyName, String.class );
	}

	public static <T, K, V> Map<T, Map<K, V>> sortByColumnValue( Map<?, Map<K, V>> orig, String keyName, Class<T> keyType )
	{
		return sortByColumnValue( orig, keyName, keyType, SortStrategy.Default );
	}

	public static <T, K, V> Map<T, Map<K, V>> sortByColumnValue( Map<?, Map<K, V>> orig, String keyName, Class<T> keyType, SortStrategy strategy )
	{
		Map<T, Map<K, V>> result = new TreeMap<>();

		if ( ( strategy == SortStrategy.MoveNext || strategy == SortStrategy.MovePrevious ) && keyType != Integer.class )
			throw new IllegalArgumentException( "Sorting Strategy `" + strategy + "` can only be used with keyName type `Integer`, `" + keyType.getName() + "` was specified." );

		if ( orig.size() < 1 )
			return result;

		for ( Entry<?, Map<K, V>> e : orig.entrySet() )
		{
			Map<K, V> row = e.getValue();
			if ( row.containsKey( keyName ) )
				if ( strategy == SortStrategy.Default )
					result.put( Objs.castTo( row.get( keyName ), keyType ), row );
				else if ( strategy == SortStrategy.MoveNext )
				{
					Integer v = Objs.castToInt( row.get( keyName ) );

					if ( result.containsKey( v ) )
						moveNext( result, v );

					result.put( ( T ) v, row );
				}
				else if ( strategy == SortStrategy.MovePrevious )
				{
					Integer v = Objs.castToInt( row.get( keyName ) );

					if ( result.containsKey( v ) )
						movePrevious( result, v );

					result.put( ( T ) v, row );
				}
		}

		return result;
	}

	/**
	 * Translates a data type from an integer (java.sql.Types value) to a string
	 * that represents the corresponding class.
	 *
	 * @param type The java.sql.Types value to convert to its corresponding class.
	 * @return The class that corresponds to the given java.sql.Types
	 * value, or Object.class if the type has no known mapping.
	 */
	public static Class<?> sqlTypeToClass( int type )
	{
		switch ( type )
		{
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
				return String.class;
			case Types.NUMERIC:
			case Types.DECIMAL:
				return BigDecimal.class;
			case Types.BIT:
				return Boolean.class;
			case Types.TINYINT:
				return Byte.class;
			case Types.SMALLINT:
				return Short.class;
			case Types.INTEGER:
				return Integer.class;
			case Types.BIGINT:
				return Long.class;
			case Types.REAL:
			case Types.FLOAT:
				return Float.class;
			case Types.DOUBLE:
				return Double.class;
			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
				return Byte[].class;
			case Types.DATE:
				return Date.class;
			case Types.TIME:
				return Time.class;
			case Types.TIMESTAMP:
				return Timestamp.class;
			default:
				return Object.class;
		}
	}

	public static Object sqlTypeToObject( int type )
	{
		switch ( type )
		{
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
				return "";
			case Types.NUMERIC:
			case Types.DECIMAL:
				return BigDecimal.ZERO;
			case Types.BIT:
				return false;
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			case Types.BIGINT:
				return Integer.MIN_VALUE;
			case Types.REAL:
			case Types.FLOAT:
				return Float.MIN_VALUE;
			case Types.DOUBLE:
				return Double.MIN_VALUE;
			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
				return new Byte[0];
			case Types.DATE:
				return new Date( Timings.epoch() );
			case Types.TIME:
				return new Time( Timings.epoch() );
			case Types.TIMESTAMP:
				return new Timestamp( Timings.epoch() );
			default:
				return new Object();
		}
	}

	public static String toString( PreparedStatement stmt )
	{
		if ( stmt == null )
			return "(stmt is null)";
		return stmt.toString().substring( stmt.toString().indexOf( ": " ) + 2 ).trim();
	}

	/**
	 * Provides a desired Sorting Strategy to utility classes
	 */
	enum SortStrategy
	{
		/**
		 * If the map contains more than one key with the same value, predecessors will be overridden
		 */
		Default,
		/**
		 * Will increment keys to next available index to make room
		 */
		MoveNext,
		/**
		 * Will decrement keys to last available index to make room
		 */
		MovePrevious
	}
}
