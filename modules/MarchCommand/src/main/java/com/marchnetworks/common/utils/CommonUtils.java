package com.marchnetworks.common.utils;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.command.api.security.CommandAuthenticationDetails;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class CommonUtils
{
	private static final Logger LOG = LoggerFactory.getLogger( CommonUtils.class );

	public static String uncapitalize( String s )
	{
		if ( CommonAppUtils.isNullOrEmptyString( s ) )
			return s;

		char firstCharacter = Character.toLowerCase( s.charAt( 0 ) );
		if ( s.length() == 1 )
			return String.valueOf( firstCharacter );
		return firstCharacter + s.substring( 1 );
	}

	public static CommandAuthenticationDetails getAuthneticationDetails()
	{
		SecurityContext context = SecurityContextHolder.getContext();
		if ( ( context != null ) && ( context.getAuthentication() != null ) && ( context.getAuthentication().getDetails() != null ) && ( ( context.getAuthentication().getDetails() instanceof CommandAuthenticationDetails ) ) )
		{
			CommandAuthenticationDetails sessionDetails = ( CommandAuthenticationDetails ) context.getAuthentication().getDetails();
			return sessionDetails;
		}

		return null;
	}

	public static boolean copyFile( String origin, String destination )
	{
		FileObject originFileObject;
		FileSystemManager fsManager;

		try
		{
			fsManager = VFS.getManager();
			originFileObject = fsManager.resolveFile( origin );
		}
		catch ( FileSystemException e )
		{
			LOG.info( "Failed to find source file at {}. Error details:{}", new Object[] {origin, e.getMessage()} );
			return false;
		}

		try
		{

			if ( originFileObject != null )
			{
				FileObject targetFileObject = fsManager.resolveFile( destination );
				targetFileObject.createFile();
				targetFileObject.copyFrom( originFileObject, new AllFileSelector() );
				targetFileObject.close();
			}
		}
		catch ( FileSystemException e )
		{
			LOG.warn( "Failed to copy data into destination file at {}. Error details:{}", new Object[] {destination, e.getMessage()} );
			return false;
		}

		return true;
	}

	public static Set<String> jsonToStringSet( String json )
	{
		if ( json != null )
			return CoreJsonSerializer.collectionFromJson( json, new TypeToken<LinkedHashSet<String>>()
			{
			} );
		return new LinkedHashSet<String>( 1 );
	}

	public static Set<Long> jsonToLongSet( String json )
	{
		if ( json != null )
			return CoreJsonSerializer.collectionFromJson( json, new TypeToken<LinkedHashSet<Long>>()
			{
			} );
		return new LinkedHashSet<Long>( 1 );
	}

	public static <T> String setToJson( Set<T> targetSet, Set<T> inputSet )
	{
		if ( targetSet == null )
		{
			throw new IllegalStateException( "Target Set can not be null" );
		}
		if ( !targetSet.isEmpty() )
		{
			targetSet.clear();
		}
		if ( ( inputSet == null ) || ( inputSet.isEmpty() ) )
		{
			return null;
		}

		targetSet.addAll( inputSet );
		return CoreJsonSerializer.toJson( targetSet );
	}

	public static <T> String setToJson( Set<T> targetSet, T[] inputSet )
	{
		if ( targetSet == null )
		{
			throw new IllegalStateException( "Target Set can not be null" );
		}
		if ( !targetSet.isEmpty() )
		{
			targetSet.clear();
		}
		if ( ( inputSet == null ) || ( inputSet.length == 0 ) )
		{
			return null;
		}
		Collections.addAll( targetSet, inputSet );
		return CoreJsonSerializer.toJson( targetSet );
	}

	public static <T> String setToJson( Set<T> inputSet )
	{
		if ( ( inputSet == null ) || ( inputSet.isEmpty() ) )
		{
			return null;
		}
		return CoreJsonSerializer.toJson( inputSet );
	}

	public static <T> String arrayToJson( T[] inputArray )
	{
		if ( ( inputArray == null ) || ( inputArray.length == 0 ) )
		{
			return null;
		}
		return CoreJsonSerializer.toJson( inputArray );
	}

	public static String concatenateStrings( String[] array )
	{
		if ( ( array == null ) || ( array.length == 0 ) )
		{
			return null;
		}
		String result = "";
		for ( int i = 0; i < array.length; i++ )
		{
			result = result + array[i];
			if ( i < array.length - 1 )
			{
				result = result + ",";
			}
		}
		return result;
	}

	public static String toDecimalString( String aHexNumber )
	{
		return "" + Integer.parseInt( aHexNumber, 16 );
	}

	public static String toHexString( String aDecimalNumber )
	{
		int decimal = 0;
		try
		{
			decimal = Integer.parseInt( aDecimalNumber );
		}
		catch ( NumberFormatException nfe )
		{
			return null;
		}
		return Integer.toHexString( decimal );
	}

	public static String zip_genFileName( String filePath )
	{
		String fileName = filePath;
		int pos = filePath.lastIndexOf( "/" );
		if ( ( pos > 0 ) && ( pos < filePath.length() ) )
		{
			fileName = filePath.substring( pos + 1 );
		}
		return fileName;
	}

	public static String getFileExtension( String file )
	{
		String result = "";
		int i = file.lastIndexOf( '.' );
		if ( i > 0 )
		{
			result = file.substring( i + 1 );
		}
		return result;
	}

	public static int compareVersions( String version1, String version2 ) throws IllegalArgumentException
	{
		if ( ( CommonAppUtils.isNullOrEmptyString( version1 ) ) || ( CommonAppUtils.isNullOrEmptyString( version2 ) ) )
		{
			throw new IllegalArgumentException( "Version strings to comapre must not be null or empty" );
		}

		StringTokenizer version1St = new StringTokenizer( version1, "." );
		StringTokenizer version2St = new StringTokenizer( version2, "." );

		while ( ( version1St.hasMoreTokens() ) || ( version2St.hasMoreTokens() ) )
		{
			if ( ( version1St.hasMoreTokens() ) && ( !version2St.hasMoreTokens() ) )
				return 1;
			if ( ( !version1St.hasMoreTokens() ) && ( version2St.hasMoreTokens() ) )
			{
				return -1;
			}

			String ver1Token = version1St.nextToken();
			String ver2Token = version2St.nextToken();
			try
			{
				int version1Part = Integer.parseInt( ver1Token );
				int version2Part = Integer.parseInt( ver2Token );
				if ( version1Part > version2Part )
					return 1;
				if ( version1Part < version2Part )
				{
					return -1;
				}
			}
			catch ( NumberFormatException e )
			{
				LOG.debug( "!!!! non-integer version number detected!" );
				int retVal = ver1Token.compareToIgnoreCase( ver2Token );
				if ( retVal != 0 )
				{
					return retVal;
				}
			}
		}

		return 0;
	}

	public static String getVersionPart( String version, int removals )
	{
		String temp = version;
		for ( int i = 0; i < removals; i++ )
		{
			int index = temp.lastIndexOf( "." );
			if ( index == -1 )
				break;
			temp = temp.substring( 0, index );
		}

		return temp;
	}

	public static int checkDeviceModel( String dModel, String modelRange ) throws IllegalArgumentException
	{
		if ( ( CommonAppUtils.isNullOrEmptyString( dModel ) ) || ( CommonAppUtils.isNullOrEmptyString( modelRange ) ) )
		{
			throw new IllegalArgumentException( "model strings to comapre must not be null or empty" );
		}

		if ( modelRange.indexOf( ',' ) > 0 )
		{
			for ( String retval : modelRange.split( "," ) )
			{
				if ( retval.indexOf( '-' ) > 0 )
				{
					String[] retval2 = retval.split( "-" );
					if ( ( Integer.parseInt( dModel ) >= Integer.parseInt( retval2[0] ) ) && ( Integer.parseInt( dModel ) <= Integer.parseInt( retval2[1] ) ) )
					{
						return 1;
					}
				}
				else if ( dModel.equalsIgnoreCase( retval ) )
				{
					return 1;
				}
			}
		}
		else if ( modelRange.indexOf( '-' ) > 0 )
		{
			String[] retval4 = modelRange.split( "-" );
			if ( ( Integer.parseInt( dModel ) >= Integer.parseInt( retval4[0] ) ) && ( Integer.parseInt( dModel ) <= Integer.parseInt( retval4[1] ) ) )
			{
				return 1;
			}
		}
		else if ( dModel.equalsIgnoreCase( modelRange ) )
		{
			return 1;
		}

		return -1;
	}

	public static String getShortUsername( String username )
	{
		int symbol = username.indexOf( '@' );
		if ( symbol != -1 )
		{
			return username.substring( 0, symbol );
		}
		return username;
	}

	public static String getFileEtag( File file )
	{
		StringBuilder eTagHeader = new StringBuilder();
		eTagHeader.append( "W/\"" );
		eTagHeader.append( file.length() );
		eTagHeader.append( "-" );
		eTagHeader.append( file.lastModified() );
		eTagHeader.append( "\"" );
		return eTagHeader.toString();
	}

	public static boolean validateIpAddress( String ipAddress )
	{
		if ( CommonAppUtils.isNullOrEmptyString( ipAddress ) )
		{
			return false;
		}

		if ( ( ipAddress.contains( " " ) ) || ( ipAddress.contains( "," ) ) )
		{
			return false;
		}
		return true;
	}

	public static boolean matchStringPattern( String str, String pattern )
	{
		if ( ( str == null ) || ( pattern == null ) )
		{
			return false;
		}
		boolean matches = true;
		int stringLen = str.length();
		int patternLen = pattern.length();
		if ( stringLen < patternLen )
		{
			return false;
		}

		int stringIndex = 0;
		int patternIndex = 0;
		int wildPatternIndex = -1;
		int wildStringIndex = -1;

		while ( ( matches ) && ( patternIndex < patternLen ) )
		{
			char c = pattern.charAt( patternIndex );
			switch ( c )
			{
				case '?':
					if ( stringIndex < stringLen )
					{
						patternIndex++;
						stringIndex++;
					}
					else
					{
						matches = false;
					}
					break;

				case '*':
					patternIndex++;
					wildPatternIndex = patternIndex;
					wildStringIndex = stringIndex;

					if ( patternIndex == patternLen )
					{
						stringIndex = stringLen;
					}
					break;
				default:
					if ( stringIndex < stringLen )
					{
						if ( c == str.charAt( stringIndex ) )
						{
							patternIndex++;
							stringIndex++;

						}
						else if ( wildPatternIndex >= 0 )
						{
							wildStringIndex++;
							stringIndex = wildStringIndex;
							patternIndex = wildPatternIndex;
						}
						else
						{
							matches = false;
						}
					}
					else
					{
						matches = false;
					}

					break;
			}

			if ( ( patternIndex == patternLen ) && ( stringIndex < stringLen ) && ( wildPatternIndex > 0 ) )
			{
				wildStringIndex++;
				stringIndex = wildStringIndex;
				patternIndex = wildPatternIndex;
			}
		}

		if ( stringIndex < stringLen )
		{
			matches = false;
		}
		return matches;
	}

	public static String convertVersionFormat( String version )
	{
		int subVerDigits = 2;
		int totalSubVerDidits = 6;
		StringBuilder sb = new StringBuilder();
		int iVal = 0;
		int startIndex = 0;
		int endIndex = 0;
		if ( ( version != null ) && ( version.length() > 6 ) )
		{
			version = version.replaceAll( "\\.", "" );

			while ( startIndex <= 6 )
			{
				endIndex = startIndex < 6 ? startIndex + 2 : version.length();
				try
				{
					iVal = Integer.valueOf( version.substring( startIndex, endIndex ) ).intValue();
					sb.append( String.valueOf( iVal ) );
				}
				catch ( NumberFormatException e )
				{
					sb.append( version.substring( startIndex, endIndex ) );
				}

				if ( startIndex < 6 )
				{
					sb.append( "." );
				}
				startIndex += 2;
			}
		}

		return sb.toString();
	}

	public static boolean isReplaceableDevice( String familyID, String modelID, String swVersion )
	{
		if ( ( "257".equalsIgnoreCase( familyID ) ) && ( compareVersions( swVersion, "5.7.10.0107" ) >= 0 ) )
		{
			return true;
		}
		if ( ( "256".equalsIgnoreCase( familyID ) ) && ( ( modelID.equalsIgnoreCase( "2" ) ) || ( modelID.equalsIgnoreCase( "3" ) ) ) && ( compareVersions( swVersion, "4.9.5" ) >= 0 ) )
		{

			return true;
		}

		return false;
	}

	public static boolean isReplaceableModel( String familyID, String modelID )
	{
		if ( ( "257".equalsIgnoreCase( familyID ) ) || ( ( "256".equalsIgnoreCase( familyID ) ) && ( ( "2".equalsIgnoreCase( modelID ) ) || ( "3".equalsIgnoreCase( modelID ) ) ) ) )
		{

			return true;
		}

		return false;
	}
}
