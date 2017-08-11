package io.amelia.support;

public class LibMath
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
}
