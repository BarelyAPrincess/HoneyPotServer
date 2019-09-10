package com.marchnetworks.command.common;

public final class FastStringTemplate
{
	private StringBuilder sb;

	public FastStringTemplate( String str )
	{
		sb = new StringBuilder( str );
	}

	public FastStringTemplate( StringBuilder str )
	{
		sb = str;
	}

	public FastStringTemplate replace( String a, String b )
	{
		int start = sb.indexOf( a );
		int end = start + a.length();
		b = b != null ? b : "";
		if ( ( start > 0 ) && ( end > 0 ) )
			sb.replace( start, end, b );
		return this;
	}

	public FastStringTemplate replace( String a, Object b )
	{
		int start = sb.indexOf( a );
		int end = start + a.length();
		if ( ( start > 0 ) && ( end > 0 ) )
			sb.replace( start, end, b != null ? b.toString() : "" );
		return this;
	}

	public String extractString( String startTag, String endTag )
	{
		int start = sb.indexOf( startTag );
		int end = sb.indexOf( endTag );
		String string = sb.substring( start + startTag.length(), end );
		sb = sb.delete( start, end + endTag.length() );
		return string;
	}

	public void removeString( String startTag, String endTag )
	{
		int start = sb.indexOf( startTag );
		int end = sb.indexOf( endTag );
		sb = sb.delete( start, end + endTag.length() );
	}

	public String toString()
	{
		return sb.toString();
	}
}
