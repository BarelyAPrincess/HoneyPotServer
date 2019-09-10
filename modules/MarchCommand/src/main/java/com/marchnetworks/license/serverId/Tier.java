package com.marchnetworks.license.serverId;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public class Tier implements Comparable<Tier>
{
	protected int m_iTier;
	protected Set<Criterion> m_sCriteria;

	public Tier( int tier, Set<Criterion> criteria )
	{
		m_iTier = tier;
		m_sCriteria = Collections.unmodifiableSet( criteria );
	}

	public int getTier()
	{
		return m_iTier;
	}

	public Criterion findCriterion( String name )
	{
		Iterator<Criterion> criteria = m_sCriteria.iterator();
		while ( criteria.hasNext() )
		{
			Criterion c = ( Criterion ) criteria.next();
			if ( c.getName().equals( name ) )
			{
				return c;
			}
		}
		return null;
	}

	public int compareTo( Tier B )
	{
		return m_iTier - m_iTier;
	}

	public boolean equals( Object obj )
	{
		if ( !( obj instanceof Tier ) )
			return false;
		Tier tB = ( Tier ) obj;

		if ( m_sCriteria.size() != m_sCriteria.size() )
		{
			return false;
		}
		Iterator<Criterion> aCriteria = m_sCriteria.iterator();
		Iterator<Criterion> bCriteria = m_sCriteria.iterator();

		while ( aCriteria.hasNext() )
		{
			Criterion ca = ( Criterion ) aCriteria.next();
			Criterion cb = ( Criterion ) bCriteria.next();

			if ( !ca.isEqual( cb ) )
			{
				return false;
			}
		}
		return true;
	}

	public double computeDifferenceScore( int level, Tier B )
	{
		double score = 0.0D;
		double increment = 1 / level;

		Iterator<Criterion> aCriteria = m_sCriteria.iterator();
		Iterator<Criterion> bCriteria = m_sCriteria.iterator();

		while ( aCriteria.hasNext() )
		{
			Criterion ca = ( Criterion ) aCriteria.next();
			Criterion cb = ( Criterion ) bCriteria.next();

			if ( !ca.equals( cb ) )
				score += increment;
		}
		return score;
	}

	public String toXml()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "\t<tier>\n" );

		for ( Criterion c : m_sCriteria )
		{
			sb.append( "\t\t" );
			sb.append( c.toXmlString() );
			sb.append( '\n' );
		}

		sb.append( "\t</tier>\n" );
		return sb.toString();
	}
}

