/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents the current error reporting level
 */
public enum ReportingLevel
{
	E_ALL( 0xff, false ),
	L_ERROR( 0xf6, false ),
	L_DENIED( 0xf5, false ),
	L_EXPIRED( 0xf4, false ),
	L_SECURITY( 0xf3, false ),
	L_PERMISSION( 0xf2, false ),
	L_SUCCESS( 0xf1, true ),
	L_DEFAULT( 0xf0, true ),
	E_UNHANDLED( 0x10, false ),
	E_DEPRECATED( 0x09, true ),
	E_IGNORABLE( 0x08, true ),
	E_STRICT( 0x07, false ),
	E_USER_NOTICE( 0x06, true ),
	E_USER_WARNING( 0x05, true ),
	E_USER_ERROR( 0x04, false ),
	E_NOTICE( 0x03, true ),
	E_PARSE( 0x02, false ),
	E_WARNING( 0x01, true ),
	E_ERROR( 0x00, false );

	private static final List<ReportingLevel> enabledErrorLevels = new ArrayList<>( Arrays.asList( parse( "E_ALL ~E_NOTICE ~E_STRICT ~E_DEPRECATED" ) ) );

	public static boolean disableErrorLevel( ReportingLevel... level )
	{
		return enabledErrorLevels.removeAll( Arrays.asList( parse( level ) ) );
	}

	public static boolean enableErrorLevel( ReportingLevel... level )
	{
		return enabledErrorLevels.addAll( Arrays.asList( parse( level ) ) );
	}

	public static boolean enableErrorLevelOnly( ReportingLevel... level )
	{
		enabledErrorLevels.clear();
		return enableErrorLevel( level );
	}

	public static List<ReportingLevel> getEnabledErrorLevels()
	{
		return Collections.unmodifiableList( enabledErrorLevels );
	}

	public static boolean isEnabledLevel( ReportingLevel level )
	{
		return enabledErrorLevels.contains( level );
	}

	public static ReportingLevel[] parse( int level )
	{
		for ( ReportingLevel er : values() )
			if ( er.level == level )
				return new ReportingLevel[] {er};
		return parse( E_ALL );
	}

	public static ReportingLevel[] parse( ReportingLevel... level )
	{
		List<ReportingLevel> levels = new ArrayList<>();
		for ( ReportingLevel er : level )
			levels.addAll( Arrays.asList( parse( er ) ) );
		return levels.toArray( new ReportingLevel[0] );
	}

	public static ReportingLevel[] parse( ReportingLevel level )
	{
		switch ( level )
		{
			case E_ALL:
				return ReportingLevel.values();
			case E_IGNORABLE:
				List<ReportingLevel> levels = new ArrayList<>();
				for ( ReportingLevel l : ReportingLevel.values() )
					if ( l.isIgnorable() )
						levels.add( l );
				return levels.toArray( new ReportingLevel[0] );
			default:
				return new ReportingLevel[] {level};
		}
	}

	public static ReportingLevel[] parse( String level )
	{
		List<ReportingLevel> levels = new ArrayList<>();
		level = level.replaceAll( "&", "" );
		for ( String s : level.split( " " ) )
			if ( s != null )
				if ( s.startsWith( "~" ) || s.startsWith( "!" ) )
					for ( ReportingLevel er : values() )
					{
						if ( er.name().equalsIgnoreCase( s.substring( 1 ) ) )
							levels.removeAll( Arrays.asList( parse( er ) ) );
					}
				else
					for ( ReportingLevel er : values() )
						if ( er.name().equalsIgnoreCase( s ) )
							levels.addAll( Arrays.asList( parse( er ) ) );

		return levels.toArray( new ReportingLevel[0] );
	}

	final int level;

	final boolean ignorable;

	ReportingLevel( int level, boolean ignorable )
	{
		this.level = level;
		this.ignorable = ignorable;
	}

	public int intValue()
	{
		return level;
	}

	public boolean isEnabled()
	{
		return isEnabledLevel( this );
	}

	public boolean isIgnorable()
	{
		return ignorable;
	}

	public boolean isSuccess()
	{
		return this == ReportingLevel.L_SUCCESS;
	}
}
