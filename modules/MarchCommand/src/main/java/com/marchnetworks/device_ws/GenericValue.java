package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "GenericValue" )
@XmlSeeAlso( {GenericDouble.class, GenericString.class, GenericInt64.class, GenericInt32.class, GenericParameterDeleteAction.class, GenericNull.class, GenericBoolean.class} )
public class GenericValue
{
}
