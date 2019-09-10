package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfAudioOutput", propOrder = {"audioOutput"} )
public class ArrayOfAudioOutput
{
	@XmlElement( name = "AudioOutput" )
	protected List<AudioOutput> audioOutput;

	public List<AudioOutput> getAudioOutput()
	{
		if ( audioOutput == null )
		{
			audioOutput = new ArrayList();
		}
		return audioOutput;
	}
}
