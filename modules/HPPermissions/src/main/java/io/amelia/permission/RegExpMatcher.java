/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExpMatcher
{
	public static final String RAW_REGEX_CHAR = "$";
	protected static HashMap<String, Pattern> patternCache = new HashMap<String, Pattern>();
	protected static Pattern rangeExpression = Pattern.compile( "(\\d+)-(\\d+)" );

	public static String prepareRegexp( String expression )
	{
		if ( expression.startsWith( "-" ) )
			expression = expression.substring( 1 );

		if ( expression.startsWith( "#" ) )
			expression = expression.substring( 1 );

		boolean rawRegexp = expression.startsWith( RAW_REGEX_CHAR );
		if ( rawRegexp )
			expression = expression.substring( 1 );

		String regexp = rawRegexp ? expression : expression.replace( ".", "\\." ).replace( "*", "(.*)" );

		try
		{
			Matcher rangeMatcher = rangeExpression.matcher( regexp );
			while ( rangeMatcher.find() )
			{
				StringBuilder range = new StringBuilder();
				int from = Integer.parseInt( rangeMatcher.group( 1 ) );
				int to = Integer.parseInt( rangeMatcher.group( 2 ) );

				if ( from > to )
				{
					int temp = from;
					from = to;
					to = temp;
				} // swap them

				range.append( "(" );

				for ( int i = from; i <= to; i++ )
				{
					range.append( i );
					if ( i < to )
						range.append( "|" );
				}

				range.append( ")" );

				regexp = regexp.replace( rangeMatcher.group( 0 ), range.toString() );
			}
		}
		catch ( Throwable e )
		{
			// Ignore
		}

		return regexp;
	}

	protected Pattern createPattern( String expression )
	{
		try
		{
			return Pattern.compile( prepareRegexp( expression ), Pattern.CASE_INSENSITIVE );
		}
		catch ( PatternSyntaxException e )
		{
			return Pattern.compile( Pattern.quote( expression ), Pattern.CASE_INSENSITIVE );
		}
	}

	public boolean isMatches( Permission expression, String permission )
	{
		return isMatches( expression.getNamespace(), permission );
	}

	public boolean isMatches( String expression, String permission )
	{
		Pattern permissionMatcher = patternCache.get( expression );

		if ( permissionMatcher == null )
			patternCache.put( expression, permissionMatcher = createPattern( expression ) );

		return permissionMatcher.matcher( permission ).matches();
	}
}
