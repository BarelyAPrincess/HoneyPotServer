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

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Used to compare semantic versioning strings with support for optional release stage and/or build number.
 * e.g., 1.2.3-alpha.1+B001
 *
 * @see "[[http://semver.org/]]"
 */
public class Version implements Comparable<Version>
{
	private int build;
	private int major;
	private int minor;
	private int patch;
	private String releaseHash;
	private int releaseNumber;
	private ReleaseStage releaseStage;

	public Version( int major, int minor, int patch )
	{
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.releaseStage = null;
		this.build = 0;
	}

	public Version( int major, int minor, int patch, String releaseStage, int build )
	{
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		setReleaseStage( releaseStage );
		this.build = build;
	}

	public Version( String version )
	{
		Objs.notNull( version );

		if ( version.contains( "+" ) )
		{
			String buildStr = version.substring( version.indexOf( "+" ) + 1 );
			if ( !buildStr.matches( "[BHbh]?[0-9]+" ) )
				throw new IllegalArgumentException( "Illegal build format. Got \"" + buildStr + "\", expect \"B0123\" or \"H9956d8c\"" );
			if ( Strs.startsWithIgnoreCase( buildStr, "B", "H" ) )
				buildStr = buildStr.substring( 1 );
			if ( Strs.startsWithIgnoreCase( buildStr, "B" ) )
				build = Integer.parseInt( buildStr );
			if ( Strs.startsWithIgnoreCase( buildStr, "H" ) )
				releaseHash = buildStr;
			version = version.substring( 0, version.indexOf( "+" ) );
		}
		else
			build = 0;

		if ( version.contains( "-" ) )
		{
			String releaseNames = Arrays.stream( ReleaseStage.values() ).map( ReleaseStage::name ).map( String::toLowerCase ).collect( Collectors.joining( "|" ) );
			String preRelease = version.substring( version.indexOf( "-" ) + 1 );
			if ( !preRelease.matches( "(" + releaseNames + ")(\\.[0-9]+)?" ) )
				throw new IllegalArgumentException( "Illegal release format. Got \"" + preRelease + "\", expect either [" + releaseNames + "] followed by an optional period and a number." );
			setReleaseStage( preRelease );
			version = version.substring( 0, version.indexOf( "-" ) );
		}
		else
			releaseStage = null;

		if ( !version.matches( "[0-9]+(\\.[0-9]+)*" ) )
			throw new IllegalArgumentException( "Invalid version format" );

		String[] parts = version.split( "\\." );
		major = Integer.parseInt( parts[0] );
		if ( parts.length > 1 )
			minor = Integer.parseInt( parts[1] );
		if ( parts.length > 2 )
			patch = Integer.parseInt( parts[2] );
	}

	public boolean compareTo( @Nonnull String regex )
	{
		return toString().matches( regex );
	}

	public boolean compareTo( @Nonnull Operator operator, @Nonnull Version that )
	{
		Objs.notNull( that );
		if ( operator == Operator.LATER )
			return compareTo( that ) > 0;
		if ( operator == Operator.LATER_OR_SAME )
			return compareTo( that ) >= 0;
		if ( operator == Operator.EARLIER )
			return compareTo( that ) < 0;
		if ( operator == Operator.EARLIER_OR_SAME )
			return compareTo( that ) <= 0;
		return operator == Operator.SAME && compareTo( that ) == 0;
	}

	@Override
	public int compareTo( @Nonnull Version that )
	{
		Objs.notNull( that );

		/* Compare major, minor, and patch version numbers. Other than equal is returned. */
		int majorResult = Integer.compare( major, that.major );
		if ( majorResult != 0 )
			return majorResult;
		int minorResult = Integer.compare( minor, that.minor );
		if ( minorResult != 0 )
			return minorResult;
		int patchResult = Integer.compare( patch, that.patch );
		if ( patchResult != 0 )
			return patchResult;

		/* Compare pre-release versions */
		int releaseResult = releaseStage.compareTo( that.releaseStage );
		if ( releaseResult != 0 )
			return releaseResult;
		int releaseNumberResult = Integer.compare( releaseNumber, that.releaseNumber );
		if ( releaseNumberResult != 0 )
			return releaseNumberResult;

		/* Final the build number */
		return Integer.compare( build, that.build );
	}

	@Override
	public boolean equals( Object that )
	{
		if ( this == that )
			return true;
		if ( that == null )
			return false;
		if ( this.getClass() != that.getClass() )
			return false;
		return this.compareTo( ( Version ) that ) == 0;
	}

	@Override
	public String toString()
	{
		return major + "." + minor + "." + patch + ( releaseStage == null ? "" : "-" + releaseStage ) + ( build > 0 ? "+B" + build : "" );
	}

	public int getBuild()
	{
		return build;
	}

	public int getMajor()
	{
		return major;
	}

	public int getMinor()
	{
		return minor;
	}

	public int getPatch()
	{
		return patch;
	}

	public String getPreRelease()
	{
		return releaseStage.name() + ( releaseNumber > 0 ? "." + releaseNumber : "" );
	}

	public ReleaseStage getReleaseStage()
	{
		return releaseStage;
	}

	private void setReleaseStage( String releaseStage )
	{
		this.releaseStage = ReleaseStage.valueOf( releaseStage.contains( "." ) ? releaseStage.substring( 0, releaseStage.indexOf( "." ) ) : releaseStage );
		releaseNumber = releaseStage.contains( "." ) ? Integer.parseInt( releaseStage.substring( releaseStage.indexOf( "." ) + 1 ) ) : 0;
	}

	public boolean isAlpha()
	{
		return releaseStage == ReleaseStage.ALPHA;
	}

	public boolean isBeta()
	{
		return releaseStage == ReleaseStage.BETA;
	}

	public boolean isRC()
	{
		return releaseStage == ReleaseStage.RC;
	}

	public boolean isStable()
	{
		return releaseStage == ReleaseStage.STABLE;
	}

	public enum Operator
	{
		SAME,
		EARLIER,
		EARLIER_OR_SAME,
		LATER,
		LATER_OR_SAME
	}

	public enum ReleaseStage
	{
		DEV,
		GIT,
		TRAVIS,
		JENKINS,
		ALPHA,
		BETA,
		RC,
		STABLE
	}
}
