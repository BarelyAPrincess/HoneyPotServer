package com.marchnetworks.common.event.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType( name = "ArrayOfPair" )
public class ArrayOfPair
{
	@XmlTransient
	List<Pair> pairCollection;
	Pair[] pair;

	public ArrayOfPair()
	{
		pairCollection = new ArrayList();
	}

	public void addPair( String aName, String aValue )
	{
		Pair pair = new Pair( aName, aValue );
		pairCollection.add( pair );
		setPair( ( Pair[] ) pairCollection.toArray( new Pair[pairCollection.size()] ) );
	}

	public Pair[] getPair()
	{
		return pair;
	}

	public void setPair( Pair[] pair )
	{
		this.pair = pair;
	}
}
