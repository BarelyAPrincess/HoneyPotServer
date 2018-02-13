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

import java.util.Comparator;

public class CompareChain
{
	private int comparison = 0;
	private Comparator<?> globalComparator = null;

	public CompareChain()
	{

	}

	public CompareChain( Comparator<?> globalComparator )
	{
		this.globalComparator = globalComparator;
	}

	public CompareChain( final Object left, final Object right )
	{
		chain( left, right );
	}

	public CompareChain( final Object left, final Object right, final Comparator<?> comparator )
	{
		chain( left, right, comparator );
	}

	public CompareChain chain( final Object left, final Object right )
	{
		return chain( left, right, null );
	}

	@SuppressWarnings( "unchecked" )
	public CompareChain chain( final Object left, final Object right, final Comparator<?> comparator )
	{
		if ( comparison != 0 )
			return this;

		if ( left == right )
			return this;

		if ( left == null )
		{
			comparison = -1;
			return this;
		}

		if ( right == null )
		{
			comparison = +1;
			return this;
		}

		Comparator<?> finalComparator = comparator == null ? globalComparator : comparator;

		if ( left.getClass().isArray() )
		{
			Long[] la = Arrs.toLongArray( left );
			Long[] ra = Arrs.toLongArray( right );

			if ( la.length > ra.length )
				comparison = +1;
			else if ( ra.length > la.length )
				comparison = -1;
			else
				for ( int i = 0; i < la.length; i++ )
					if ( la[i] > ra[i] )
						comparison = +1;
					else if ( ra[i] > la[i] )
						comparison = -1;
		}
		else if ( finalComparator == null )
			comparison = ( ( Comparator<Object> ) left ).compare( left, right );
		else
			comparison = ( ( Comparator<Object> ) finalComparator ).compare( left, right );

		return this;
	}

	public int result()
	{
		return comparison;
	}
}
