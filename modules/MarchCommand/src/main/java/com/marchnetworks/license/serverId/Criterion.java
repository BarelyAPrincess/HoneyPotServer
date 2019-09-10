package com.marchnetworks.license.serverId;

import com.marchnetworks.license.exception.ServerIdGenerateException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class Criterion implements Comparable<Criterion>
{
	protected String m_sName;

	public Criterion( String name )
	{
		m_sName = name;
	}

	public String getName()
	{
		return m_sName;
	}

	public abstract void generate() throws ServerIdGenerateException;

	public abstract boolean isLoaded();

	protected abstract String getValue();

	public abstract boolean fromValue( String paramString );

	public String toXmlString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( '<' );
		sb.append( m_sName );
		sb.append( '>' );
		sb.append( getValue() );
		sb.append( "</" );
		sb.append( m_sName );
		sb.append( '>' );
		return sb.toString();
	}

	public abstract boolean isEqual( Criterion paramCriterion );

	public boolean isSameName( Criterion c )
	{
		return m_sName.equals( m_sName );
	}

	public int compareTo( Criterion B )
	{
		return m_sName.compareTo( B.getName() );
	}

	public boolean equals( Object o )
	{
		if ( !( o instanceof Criterion ) )
			return false;
		return isSameName( ( Criterion ) o );
	}

	protected static String executeCommand( String cmd )
	{
		StringBuilder sb = new StringBuilder();
		String[] cmds = {"cmd.exe", "/C", cmd};
		String s = "";

		try
		{
			Process p = Runtime.getRuntime().exec( cmds );
			BufferedReader input = new BufferedReader( new InputStreamReader( p.getInputStream() ) );

			while ( ( s = input.readLine() ) != null )
			{
				if ( !s.equals( "" ) )
				{
					sb.append( s );
					sb.append( '\n' );
				}
			}
			p.destroy();
		}
		catch ( IOException localIOException )
		{
		}

		return sb.toString().trim();
	}

	protected String checkAndTrimOutput( String input )
	{
		String[] lots = input.split( "\n" );

		if ( lots.length != 2 )
		{
			return "";
		}
		return lots[1];
	}
}
