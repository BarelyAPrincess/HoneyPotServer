package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfAudioDetails", propOrder = {"audioDetails"} )
public class ArrayOfAudioDetails
{
	@XmlElement( name = "AudioDetails" )
	protected List<AudioDetails> audioDetails;

	public List<AudioDetails> getAudioDetails()
	{
		if ( audioDetails == null )
		{
			audioDetails = new ArrayList();
		}
		return audioDetails;
	}
}
