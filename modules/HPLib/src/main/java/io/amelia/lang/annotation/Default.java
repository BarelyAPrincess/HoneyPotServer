package io.amelia.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used by the BindingRegistry to fill in missing method parameters with new ones
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.PARAMETER )
public @interface Default
{
	Class<?> value();
}
