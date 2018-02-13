/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support.data.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents an "alias" that a {@link DataSerializable} may be stored as.
 * If this is not present on a {@link DataSerializable} class, it will use the
 * fully qualified name of the class.
 * <p />
 * This value will be stored in the configuration so that the configuration deserialization can determine what type it is.
 * <p />
 * Using this annotation on any other class than a {@link DataSerializable} will have no effect.
 *
 * @see DataSerialization#registerClass(Class, String)
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface SerializableAs
{
	/**
	 * This is the name your class will be stored and retrieved as.
	 * <p />
	 * This name MUST be unique. We recommend using names such as "MyClassThing" instead of "Thing".
	 *
	 * @return Name to serialize the class as.
	 */
	String value();
}
