package com.marchnetworks.management.instrumentation.model;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( {java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD} )
public @interface DeviceCapability
{
	String name();
}

