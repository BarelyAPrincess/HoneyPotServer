/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

@FunctionalInterface
public interface SupplierWithException<T, E extends Exception>
{
	/**
	 * Gets a result.
	 *
	 * @return a result
	 */
	T get() throws E;
}
