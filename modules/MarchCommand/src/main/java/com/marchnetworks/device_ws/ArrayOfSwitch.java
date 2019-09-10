package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfSwitch", propOrder = {"_switch"} )
public class ArrayOfSwitch
{
	@XmlElement( name = "Switch" )
	protected List<Switch> _switch;

	public List<Switch> getSwitch()
	{
		if ( _switch == null )
		{
			_switch = new ArrayList();
		}
		return _switch;
	}
}
