/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark methods as being event handler methods
 */
@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
public @interface EventHandler
{
	/**
	 * Define the priority of the event.
	 * <p>
	 * First priority to the last priority executed:
	 * <ol>
	 * <li>LOWEST</li>
	 * <li>LOW</li>
	 * <li>NORMAL</li>
	 * <li>HIGH</li>
	 * <li>HIGHEST</li>
	 * <li>MONITOR</li>
	 * </ol>
	 */
	EventPriority priority() default EventPriority.NORMAL;
	
	/**
	 * Define if the handler ignores a cancelled event.
	 * <p>
	 * If ignoreCancelled is true and the event is cancelled, the method is not called. Otherwise, the method is always called.
	 */
	boolean ignoreCancelled() default false;
}
