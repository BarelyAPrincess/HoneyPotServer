package com.marchnetworks.command.api.servlet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( {java.lang.annotation.ElementType.METHOD} )
public @interface ManagedAction
{
	public static final String _default = "default";

	String action() default "default";
}
