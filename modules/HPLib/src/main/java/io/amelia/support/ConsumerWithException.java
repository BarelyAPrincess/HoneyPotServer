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

import java.util.Objects;

@FunctionalInterface
public interface ConsumerWithException<T, E extends Exception>
{
	/**
	 * Performs this operation on the given argument.
	 *
	 * @param t the input argument
	 */
	void accept( T t ) throws E;

	/**
	 * Returns a composed {@code Consumer} that performs, in sequence, this
	 * operation followed by the {@code after} operation. If performing either
	 * operation throws an exception, it is relayed to the caller of the
	 * composed operation.  If performing this operation throws an exception,
	 * the {@code after} operation will not be performed.
	 *
	 * @param after the operation to perform after this operation
	 * @return a composed {@code Consumer} that performs in sequence this
	 * operation followed by the {@code after} operation
	 * @throws NullPointerException if {@code after} is null
	 */
	default ConsumerWithException<T, E> andThen( ConsumerWithException<? super T, E> after )
	{
		Objects.requireNonNull( after );
		return ( T t ) -> {
			accept( t );
			after.accept( t );
		};
	}
}