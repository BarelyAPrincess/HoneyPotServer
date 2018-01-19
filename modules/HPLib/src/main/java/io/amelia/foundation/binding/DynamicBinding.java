package io.amelia.foundation.binding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to label a method as having a dynamic value inside BindingResolvers.
 * Meaning the returned value will not be saved in the bindings and will be resolved each time.
 */
@Target( {ElementType.METHOD, ElementType.FIELD} )
@Retention( RetentionPolicy.RUNTIME )
public @interface DynamicBinding
{

}
