package io.amelia.foundation.binding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It's recommended to use this annotation to identify what methods and fields provide for which namespace when possible.
 */
@Target( {ElementType.FIELD, ElementType.METHOD} )
@Retention( RetentionPolicy.RUNTIME )
public @interface ProvidesBinding
{
	String value();
}
