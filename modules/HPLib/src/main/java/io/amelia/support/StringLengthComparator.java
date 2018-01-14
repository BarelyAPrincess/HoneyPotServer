package io.amelia.support;

import java.util.Comparator;

public class StringLengthComparator implements Comparator<String>
{
	private final boolean assendingOrder;

	public StringLengthComparator()
	{
		this( true );
	}

	public StringLengthComparator( boolean assendingOrder )
	{
		this.assendingOrder = assendingOrder;
	}

	@Override
	public int compare( String left, String right )
	{
		if ( left == null )
			return 1;
		if ( right == null )
			return 0;
		if ( left.equals( right ) )
			return 0;

		int l = left.length();
		int r = right.length();

		if ( l == r )
			return assendingOrder ? left.compareTo( right ) : right.compareTo( left );
		else
			return assendingOrder ? Integer.compare( l, r ) : Integer.compare( r, l );
	}
}
