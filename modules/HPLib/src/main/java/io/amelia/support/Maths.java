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

import java.util.Optional;

public class Maths
{
	/**
	 * Returns the sum of the two parameters, or throws an exception if the resulting sum would
	 * cause an overflow or underflow.
	 *
	 * @throws IllegalArgumentException when overflow or underflow would occur.
	 */
	public static int addOrThrow( int a, int b ) throws IllegalArgumentException
	{
		if ( b == 0 )
			return a;

		if ( b > 0 && a <= ( Integer.MAX_VALUE - b ) )
			return a + b;

		if ( b < 0 && a >= ( Integer.MIN_VALUE - b ) )
			return a + b;

		throw new IllegalArgumentException( "Addition overflow: " + a + " + " + b );
	}

	public static boolean isNumber( String value )
	{
		try
		{
			Integer.parseInt( value );
			return true;
		}
		catch ( NumberFormatException e )
		{
			return false; // String is not a number, auto disqualified
		}
	}

	public static <NumberType extends Number> Optional<NumberType> nonNegative( NumberType... numbers )
	{
		for ( NumberType i : numbers )
			if ( i.intValue() >= 0 )
				return Optional.of( i );
		return Optional.empty();
	}

	public static <NumberType extends Number> Optional<NumberType> nonNegativeOrZero( NumberType... numbers )
	{
		for ( NumberType i : numbers )
			if ( i.intValue() > 0 )
				return Optional.of( i );
		return Optional.empty();
	}

	public static <NumberType extends Number> Optional<NumberType> nonPositive( NumberType... numbers )
	{
		for ( NumberType i : numbers )
			if ( i.intValue() <= 0 )
				return Optional.of( i );
		return Optional.empty();
	}

	public static <NumberType extends Number> Optional<NumberType> nonPositiveOrZero( NumberType... numbers )
	{
		for ( NumberType i : numbers )
			if ( i.intValue() < 0 )
				return Optional.of( i );
		return Optional.empty();
	}

	/**
	 * Returns the first argument that does not equal zero.
	 *
	 * @param numbers Array of numbers to check.
	 *
	 * @return First arg not zero, zero is all were zero.
	 */
	public static <NumberType extends Number> Optional<NumberType> nonZero( NumberType... numbers )
	{
		for ( NumberType i : numbers )
			if ( i.intValue() != 0 )
				return Optional.of( i );
		return Optional.empty();
	}
}
