/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.amelia.support.Objs;

public class TableBuilder
{
	Map<String, List<String>> arguments = new TreeMap<>();
	private String defText = null;
	private AtomicInteger inx = new AtomicInteger();
	private Map<Integer, Row> rows = new TreeMap<>();
	private List<String> tableHeader = new ArrayList<>();

	public TableBuilder()
	{
		addArgument( "class", "altrowstable" );
	}

	public TableBuilder addArgument( String key, List<String> cls )
	{
		arguments.compute( key, ( k, v ) -> v == null ? new ArrayList<>() : v ).addAll( cls );
		return this;
	}

	public TableBuilder addArgument( String key, String cls )
	{
		arguments.compute( key, ( k, v ) -> v == null ? new ArrayList<>() : v ).add( cls );
		return this;
	}

	public TableBuilder addHeader( List<String> headers )
	{
		tableHeader.addAll( headers );
		return this;
	}

	public TableBuilder addHeader( String header )
	{
		tableHeader.add( header );
		return this;
	}

	public TableBuilder removeArgument( String key )
	{
		arguments.remove( key );
		return this;
	}

	public String render()
	{
		StringBuilder sb = new StringBuilder();

		sb.append( "<getTable" );
		for ( Map.Entry<String, List<String>> e : arguments.entrySet() )
			sb.append( " " ).append( e.getKey() ).append( "=\"" ).append( e.getValue().stream().collect( Collectors.joining( " " ) ) ).append( "\"" );
		sb.append( ">\n" );

		if ( tableHeader.size() > 0 )
		{
			sb.append( "<thead>\n" ).append( "<tr>\n" );
			for ( String col : tableHeader )
				sb.append( "<th>" ).append( col ).append( "</th>\n" );
			sb.append( "</tr>\n" ).append( "</thead>\n" );
		}

		sb.append( "<tbody>\n" );

		int colLength = tableHeader.size();
		for ( Row row : rows.values() )
			colLength = Math.max( colLength, row.colCount() );

		if ( rows.size() > 0 )
			for ( Map.Entry<Integer, Row> e : rows.entrySet() )
				sb.append( e.getValue().render( e.getKey(), colLength ) );
		else
			sb.append( new Row( defText ).render( 0, 0 ) );

		sb.append( "</tbody>\n" ).append( "</getTable>\n" );

		return sb.toString();
	}

	public Row row( String text )
	{
		Row row = new Row( text );
		rows.put( inx.getAndIncrement(), row );
		return row;
	}

	public Row row( Map<String, String> map )
	{
		Row row = new Row( map );
		rows.put( inx.getAndIncrement(), row );
		return row;
	}

	public int rowCount()
	{
		return rows.size();
	}

	public TableBuilder setArgument( String key, List<String> cls )
	{
		arguments.compute( key, ( k, v ) -> new ArrayList<>() ).addAll( cls );
		return this;
	}

	public TableBuilder setArgument( String key, String cls )
	{
		arguments.compute( key, ( k, v ) -> new ArrayList<>() ).add( cls );
		return this;
	}

	public TableBuilder setDefault( String defText )
	{
		this.defText = defText;
		return this;
	}

	public class Row
	{
		private Map<String, List<String>> arguments = new TreeMap<>();
		private Map<String, Object> content = new TreeMap<>();

		Row( String text )
		{
			content.put( "0-text", text );
		}

		Row( Map<String, String> row )
		{
			for ( Map.Entry<String, String> e : row.entrySet() )
				if ( e.getKey().startsWith( ":" ) )
					addArgument( e.getKey().substring( 1 ), e.getValue() );
				else
					content.put( e.getKey(), e.getValue() );
		}

		public Row addArgument( String key, List<String> cls )
		{
			arguments.compute( key, ( k, v ) -> v == null ? new ArrayList<>() : v ).addAll( cls );
			return this;
		}

		public Row addArgument( String key, String cls )
		{
			arguments.compute( key, ( k, v ) -> v == null ? new ArrayList<>() : v ).add( cls );
			return this;
		}

		public int colCount()
		{
			return content.size();
		}

		public Row removeArgument( String key )
		{
			arguments.remove( key );
			return this;
		}

		public String render( int rowInx, int colLength )
		{
			StringBuilder sb = new StringBuilder();

			Map<String, List<String>> arguments = new TreeMap<>( this.arguments );

			arguments.compute( "class", ( k, v ) -> v == null ? new ArrayList<>() : v ).add( rowInx % 2 == 0 ? "evenrowcolor" : "oddrowcolor" );

			sb.append( "<tr" );
			for ( Map.Entry<String, List<String>> e : arguments.entrySet() )
				sb.append( " " ).append( e.getKey() ).append( "=\"" ).append( e.getValue().stream().collect( Collectors.joining( " " ) ) ).append( "\"" );
			sb.append( ">\n" );

			if ( content.size() == 1 )
				sb.append( "<td style=\"text-align: center; font-weight: bold;\" class=\"\" colspan=\"" ).append( colLength ).append( "\">" ).append( content.values().toArray()[0] ).append( "</td>\n" );
			else
			{
				AtomicInteger colInx = new AtomicInteger();
				for ( Object col : content.values() )
					sb.append( "<td id=\"col_" ).append( colInx.getAndIncrement() ).append( "\">" ).append( Objs.castToString( col ) ).append( "</td>\n" );
			}

			sb.append( "</tr>\n" );

			return sb.toString();
		}

		public Row setArgument( String key, List<String> cls )
		{
			arguments.compute( key, ( k, v ) -> new ArrayList<>() ).addAll( cls );
			return this;
		}

		public Row setArgument( String key, String cls )
		{
			arguments.compute( key, ( k, v ) -> new ArrayList<>() ).add( cls );
			return this;
		}

	}
}
